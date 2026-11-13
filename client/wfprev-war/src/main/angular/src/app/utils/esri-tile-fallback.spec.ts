import L from 'leaflet';
import { EsriTileFallback, enlargeRect, installEsriTileFallback } from './esri-tile-fallback';

const SERVICE = 'https://tiles.example/MapServer/';

/** A tilemap block answer: every tile held, except those `missing` says are not. */
function block(z: number, top: number, left: number, missing: (z: number, y: number, x: number) => boolean) {
  const data: number[] = [];
  for (let row = 0; row < 32; row++)
    for (let col = 0; col < 32; col++) data.push(missing(z, top + row, left + col) ? 0 : 1);
  return { valid: true, location: { left, top, width: 32, height: 32 }, data };
}

/** A fetch that answers tilemap blocks from `missing`, and tiles with `image`. */
function stubFetch(missing: (z: number, y: number, x: number) => boolean, image?: Blob) {
  return spyOn(window, 'fetch').and.callFake(((url: string) => {
    const m = /tilemap\/(\d+)\/(\d+)\/(\d+)\//.exec(url);
    if (m) return Promise.resolve(new Response(JSON.stringify(block(+m[1], +m[2], +m[3], missing))));
    if (image) return Promise.resolve(new Response(image));
    return Promise.resolve(new Response('', { status: 404 }));
  }) as any);
}

/** A 256px tile in four colours: red top left, green top right, blue bottom left, white bottom right. */
function quadrants(): Promise<Blob> {
  const canvas = document.createElement('canvas');
  canvas.width = canvas.height = 256;
  const ctx = canvas.getContext('2d')!;
  [['#ff0000', 0, 0], ['#00ff00', 128, 0], ['#0000ff', 0, 128], ['#ffffff', 128, 128]].forEach(([colour, x, y]) => {
    ctx.fillStyle = colour as string;
    ctx.fillRect(x as number, y as number, 128, 128);
  });
  return new Promise(resolve => canvas.toBlob(b => resolve(b!)));
}

/** The colour at the middle of an image, or of the image at a url, as [r, g, b]. */
async function middle(from: string | HTMLImageElement): Promise<number[]> {
  const image = typeof from === 'string' ? new Image() : from;
  if (typeof from === 'string') image.src = from;
  await image.decode();
  const canvas = document.createElement('canvas');
  canvas.width = canvas.height = image.width;
  const ctx = canvas.getContext('2d')!;
  ctx.drawImage(image, 0, 0);
  return Array.from(ctx.getImageData(image.width / 2, image.height / 2, 1, 1).data.slice(0, 3));
}

async function until(test: () => any): Promise<void> {
  for (let i = 0; i < 200 && !test(); i++) await new Promise(r => setTimeout(r, 5));
}

describe('esri tile fallback', () => {
  const identity = (z: number) => z;

  describe('enlargeRect', () => {
    it('takes the quarter of the parent under the tile, one pixel past each edge', () => {
      expect(enlargeRect(256, 1, 1, 0)).toEqual([127, -1, 130, 130, -2, -2, 260, 260]);
      expect(enlargeRect(256, 1, 0, 1)).toEqual([-1, 127, 130, 130, -2, -2, 260, 260]);
    });

    it('still fills the tile exactly, however many levels up', () => {
      for (const k of [1, 2, 3, 8]) {
        const [, , , , dx, dy, dw, dh] = enlargeRect(256, k, 5, 9);
        expect(dx).toBe(-(1 << k));
        expect(dy).toBe(-(1 << k));
        expect(dh).toBe(dw);
        expect(dw + 2 * dx).toBe(256);
      }
    });
  });

  describe('hasTile', () => {
    it('reads a tile from the tilemap block that holds it', async () => {
      const fetch = stubFetch((z, y, x) => y === 40 && x === 70);
      const fallback = new EsriTileFallback();

      expect(await fallback.hasTile(SERVICE, '', 18, 40, 70)).toBeFalse();
      expect(await fallback.hasTile(SERVICE, '', 18, 40, 71)).toBeTrue();
      expect(fetch.calls.argsFor(0)[0]).toBe(`${SERVICE}tilemap/18/32/64/32/32`);
    });

    it('asks one block for the tiles beside each other', async () => {
      const fetch = stubFetch(() => false);
      const fallback = new EsriTileFallback();

      await Promise.all([fallback.hasTile(SERVICE, '', 18, 40, 70), fallback.hasTile(SERVICE, '', 18, 41, 71)]);

      expect(fetch).toHaveBeenCalledTimes(1);
    });

    it('does not say when the service will not answer', async () => {
      spyOn(window, 'fetch').and.returnValues(
        Promise.resolve(new Response('', { status: 404 })),
        Promise.resolve(new Response(JSON.stringify({ error: { code: 400 } }))),
        Promise.reject(new Error('offline')),
      );
      const fallback = new EsriTileFallback();

      expect(await fallback.hasTile(SERVICE, '', 18, 0, 0)).toBeNull();
      expect(await fallback.hasTile(SERVICE, '', 18, 0, 32)).toBeNull();
      expect(await fallback.hasTile(SERVICE, '', 18, 0, 64)).toBeNull();
    });

    it('does not say for a tile outside the block it answered', async () => {
      spyOn(window, 'fetch').and.resolveTo(
        new Response(JSON.stringify({ valid: true, location: { left: 0, top: 0, width: 1, height: 1 }, data: [1] })),
      );

      expect(await new EsriTileFallback().hasTile(SERVICE, '', 18, 5, 5)).toBeNull();
    });

    it('passes the token on', async () => {
      const fetch = stubFetch(() => false);

      await new EsriTileFallback().hasTile(SERVICE, '?token=abc', 3, 0, 0);

      expect(fetch.calls.argsFor(0)[0]).toBe(`${SERVICE}tilemap/3/0/0/32/32?token=abc`);
    });
  });

  describe('levelsUp', () => {
    it('counts the levels up to the deepest the cache holds', async () => {
      stubFetch(z => z > 17);

      expect(await new EsriTileFallback().levelsUp(SERVICE, '', { z: 19, x: 600, y: 300 }, identity)).toBe(2);
    });

    it('is 0 where the cache holds the tile', async () => {
      stubFetch(() => false);

      expect(await new EsriTileFallback().levelsUp(SERVICE, '', { z: 19, x: 600, y: 300 }, identity)).toBe(0);
    });

    it('is 0 where the service has no level to ask', async () => {
      stubFetch(() => true);

      expect(await new EsriTileFallback().levelsUp(SERVICE, '', { z: 19, x: 600, y: 300 }, z => (z === 19 ? 19 : undefined))).toBe(0);
    });

    it('asks each level at its own service level', async () => {
      const fetch = stubFetch(z => z > 5);

      await new EsriTileFallback().levelsUp(SERVICE, '', { z: 10, x: 0, y: 0 }, z => z - 2);

      expect(fetch.calls.allArgs().map(a => a[0])).toEqual([8, 7, 6, 5].map(z => `${SERVICE}tilemap/${z}/0/0/32/32`));
    });
  });

  describe('enlarge', () => {
    it('enlarges the part of the ancestor under the tile', async () => {
      stubFetch(() => false, await quadrants());
      const fallback = new EsriTileFallback();

      // At one level down, the tile at column 1, row 0 is under the parent's top right quarter
      expect(await middle(await fallback.enlarge(`${SERVICE}tile/0/0/0`, 1, { z: 1, x: 1, y: 0 }))).toEqual([0, 255, 0]);
      expect(await middle(await fallback.enlarge(`${SERVICE}tile/0/0/0`, 1, { z: 1, x: 0, y: 1 }))).toEqual([0, 0, 255]);
    });

    it('fetches a shared ancestor once', async () => {
      const fetch = stubFetch(() => false, await quadrants());
      const fallback = new EsriTileFallback();

      await Promise.all([0, 1, 2, 3].map(x => fallback.enlarge(`${SERVICE}tile/0/0/0`, 2, { z: 2, x, y: 0 })));

      expect(fetch).toHaveBeenCalledTimes(1);
    });

    it('asks a failed ancestor again next time', async () => {
      const image = await quadrants();
      const fetch = spyOn(window, 'fetch').and.returnValues(
        Promise.resolve(new Response('', { status: 503 })),
        Promise.resolve(new Response(image)),
      );
      const fallback = new EsriTileFallback();

      await expectAsync(fallback.enlarge(`${SERVICE}tile/0/0/0`, 1, { z: 1, x: 0, y: 0 })).toBeRejected();
      await expectAsync(fallback.enlarge(`${SERVICE}tile/0/0/0`, 1, { z: 1, x: 0, y: 0 })).toBeResolved();
      expect(fetch).toHaveBeenCalledTimes(2);
    });
  });

  describe('installEsriTileFallback', () => {
    let TiledMapLayer: any;
    let originalCreateTile: jasmine.Spy;
    let leaflet: any;

    /** A TiledMapLayer as esri-leaflet leaves it once it has the service levels. */
    function layer(props: any = {}) {
      return Object.assign(Object.create(TiledMapLayer.prototype), {
        options: { url: SERVICE },
        tileUrl: `${SERVICE}tile/{z}/{y}/{x}`,
        _lodMap: Object.fromEntries(Array.from({ length: 24 }, (_, z) => [z, z])),
        _wfTilemap: true,
        _tileZoom: 19,
        getTileUrl(c: any) {
          return `${SERVICE}tile/${c.z}/${c.y}/${c.x}`;
        },
        _getZoomForUrl() {
          return this._tileZoom;
        },
        _getSubdomain: () => '',
        _tileOnLoad: jasmine.createSpy('_tileOnLoad'),
        _tileOnError: jasmine.createSpy('_tileOnError'),
        once: jasmine.createSpy('once'),
        ...props,
      });
    }

    beforeEach(() => {
      originalCreateTile = jasmine.createSpy('createTile').and.returnValue('original tile');
      TiledMapLayer = {
        prototype: {
          createTile: originalCreateTile,
          metadata(this: any, callback: any, context: any) {
            callback.call(context, null, this.answer);
            return this;
          },
        },
      };
      leaflet = { DomEvent: L.DomEvent, Util: L.Util, esri: { TiledMapLayer } };
      expect(installEsriTileFallback(leaflet)).toBeTrue();
    });

    it('patches the layer only once', () => {
      const createTile = TiledMapLayer.prototype.createTile;

      expect(installEsriTileFallback(leaflet)).toBeTrue();
      expect(TiledMapLayer.prototype.createTile).toBe(createTile);
    });

    it('does nothing without esri-leaflet', () => {
      expect(installEsriTileFallback({})).toBeFalse();
      expect(installEsriTileFallback(undefined)).toBeFalse();
    });

    it('notes from the service metadata whether it has a tilemap, and still answers the layer', () => {
      const answered = jasmine.createSpy('answered');
      const context = {};
      const imagery = layer({ _wfTilemap: undefined, answer: { capabilities: 'Map,Query,Data,Tilemap' } });
      const other = layer({ _wfTilemap: undefined, answer: { capabilities: 'Map,Query,Data' } });

      imagery.metadata(answered, context);
      other.metadata(answered, context);

      expect(imagery._wfTilemap).toBeTrue();
      expect(other._wfTilemap).toBeFalse();
      expect(answered.calls.argsFor(0)).toEqual([null, imagery.answer]);
      expect(answered.calls.first().object).toBe(context);
    });

    it('asks the tiles as before when the service has no tilemap', () => {
      const plain = layer({ _wfTilemap: false });
      const done = () => {};

      expect(plain.createTile({ z: 19, x: 1, y: 2 }, done)).toBe('original tile');
      expect(originalCreateTile).toHaveBeenCalledOnceWith({ z: 19, x: 1, y: 2 }, done);
    });

    it('asks a tile the cache holds as before', async () => {
      stubFetch(() => false);
      const tile: HTMLImageElement = layer().createTile({ z: 19, x: 600, y: 300 }, () => {});

      await until(() => tile.getAttribute('src'));

      expect(tile.src).toBe(`${SERVICE}tile/19/300/600`);
    });

    it('draws the deepest level the cache holds where it stops short', async () => {
      stubFetch(z => z > 17, await quadrants());
      // At two levels down, column 3, row 0 is under the top right quarter of its ancestor
      const tile: HTMLImageElement = layer().createTile({ z: 19, x: 3, y: 0 }, () => {});

      await until(() => tile.getAttribute('src'));

      expect(tile.src).toMatch(/^blob:/);
      expect(await middle(tile)).toEqual([0, 255, 0]);
    });

    it('fetches the ancestor at its own url', async () => {
      const fetch = stubFetch(z => z > 17, await quadrants());
      const tile: HTMLImageElement = layer().createTile({ z: 19, x: 600, y: 300 }, () => {});

      await until(() => tile.getAttribute('src'));

      expect(fetch.calls.allArgs().map(a => a[0])).toContain(`${SERVICE}tile/17/75/150`);
    });

    it('asks the tile as before when the fallback fails', async () => {
      spyOn(window, 'fetch').and.callFake(((url: string) =>
        url.includes('tilemap')
          ? Promise.resolve(new Response(JSON.stringify(block(19, 288, 576, () => true))))
          : Promise.resolve(new Response('', { status: 503 }))) as any);
      const tile: HTMLImageElement = layer().createTile({ z: 19, x: 600, y: 300 }, () => {});

      await until(() => tile.getAttribute('src'));

      expect(tile.src).toBe(`${SERVICE}tile/19/300/600`);
    });

    it('waits for the service levels, as esri-leaflet does', async () => {
      stubFetch(() => false);
      const waiting = layer({ _lodMap: undefined });
      const tile: HTMLImageElement = waiting.createTile({ z: 19, x: 600, y: 300 }, () => {});

      expect(waiting.once).toHaveBeenCalledOnceWith('lodmap', jasmine.any(Function), waiting);
      expect(tile.getAttribute('src')).toBeNull();

      waiting._lodMap = layer()._lodMap;
      waiting.once.calls.mostRecent().args[1].call(waiting);
      await until(() => tile.getAttribute('src'));

      expect(tile.src).toBe(`${SERVICE}tile/19/300/600`);
    });

    it('leaves a tile Leaflet dropped before it loaded', async () => {
      stubFetch(() => false);
      const tile: HTMLImageElement = layer().createTile({ z: 19, x: 600, y: 300 }, () => {});
      tile.src = L.Util.emptyImageUrl;

      await new Promise(r => setTimeout(r, 50));

      expect(tile.src).toBe(L.Util.emptyImageUrl);
    });

    it('tells Leaflet when the drawn tile loads, and lets its url go', async () => {
      stubFetch(z => z > 17, await quadrants());
      const revoke = spyOn(URL, 'revokeObjectURL').and.callThrough();
      const done = jasmine.createSpy('done');
      const tiled = layer();
      const tile: HTMLImageElement = tiled.createTile({ z: 19, x: 600, y: 300 }, done);

      await until(() => tiled._tileOnLoad.calls.count() || tiled._tileOnError.calls.count());

      expect(tiled._tileOnLoad).toHaveBeenCalledOnceWith(done, tile, jasmine.anything());
      await until(() => revoke.calls.count());
      expect(revoke).toHaveBeenCalledOnceWith(tile.src);
    });
  });
});
