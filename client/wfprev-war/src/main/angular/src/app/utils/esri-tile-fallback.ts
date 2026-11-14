/**
 * Draws the deepest level an Esri tile cache holds at each place, enlarged, where the cache stops short.
 *
 * Past that level the cache answers 200 with a grey "Map data not yet available" tile. The service's tilemap says
 * which tiles exist, as the ArcGIS JS API reads it. This is the Leaflet counterpart of SMK v2's MapLibre fallback
 * (smk-wildfire esri-tile-fallback.ts), for SMK 1.0.39's esri-tiled-map basemaps (World Imagery, World Hillshade).
 */

// The tilemap is asked in blocks of this many tiles a side, so the tiles beside each other share one request
const BLOCK = 32;
// Further up, the part of the tile to enlarge is under one pixel
const MAX_STEPS = 8;
// Decoded ancestor tiles kept, and tilemap blocks kept
const MAX_ANCESTORS = 64;
const MAX_BLOCKS = 512;

export interface TileAddress {
  z: number;
  x: number;
  y: number;
}

/** drawImage arguments: source x, y, width, height, then destination x, y, width, height. */
export type DrawRect = [number, number, number, number, number, number, number, number];

/**
 * The drawImage arguments for the part of an ancestor `k` levels up that lies under the tile at x, y, at full size.
 * One source pixel past each edge, so the edges blend into the next tile with no seam.
 */
export function enlargeRect(width: number, k: number, x: number, y: number): DrawRect {
  const size = width >> k;
  const scale = 1 << k;
  const mask = scale - 1;
  return [(x & mask) * size - 1, (y & mask) * size - 1, size + 2, size + 2, -scale, -scale, (size + 2) * scale, (size + 2) * scale];
}

/** Keeps at most `max` entries, dropping the oldest. */
function remember<T>(cache: Map<string, T>, key: string, value: T, max: number): T {
  cache.set(key, value);
  if (cache.size > max) cache.delete(cache.keys().next().value!);
  return value;
}

export class EsriTileFallback {
  private readonly blocks = new Map<string, Promise<any>>();
  private readonly ancestors = new Map<string, Promise<ImageBitmap>>();

  /**
   * Whether the cache at `service` (its MapServer url, ending in a slash) holds the tile at level z, row y, column x:
   * true or false from the tilemap, null when the tilemap has no answer, so the tile is asked as before.
   */
  hasTile(service: string, query: string, z: number, y: number, x: number): Promise<boolean | null> {
    const url = `${service}tilemap/${z}/${y - (y % BLOCK)}/${x - (x % BLOCK)}/${BLOCK}/${BLOCK}${query}`;
    const block =
      this.blocks.get(url) ??
      remember(this.blocks, url, fetch(url).then(r => (r.ok ? r.json() : null)).catch(() => null), MAX_BLOCKS);

    return block.then((b: any) => {
      const at = b?.location;
      if (!b?.valid || !Array.isArray(b.data) || !at) return null;
      const row = y - at.top;
      const col = x - at.left;
      if (row < 0 || col < 0 || row >= at.height || col >= at.width) return null;
      return b.data[row * at.width + col] === 1;
    });
  }

  /**
   * How many levels up from `tile` the cache holds its place: 0 when it holds the tile itself or will not say.
   * `level` gives the service level for a map zoom, undefined where the service has none.
   */
  async levelsUp(service: string, query: string, tile: TileAddress, level: (zoom: number) => number | undefined): Promise<number> {
    for (let k = 0; k <= MAX_STEPS && tile.z - k >= 0; k++) {
      const z = level(tile.z - k);
      if (z === undefined) return 0;
      if ((await this.hasTile(service, query, z, tile.y >> k, tile.x >> k)) !== false) return k;
    }
    return 0;
  }

  /** The image for a tile, drawn from the part of its ancestor `k` levels up, enlarged, as an object url. */
  async enlarge(ancestorUrl: string, k: number, tile: TileAddress): Promise<string> {
    const whole = await this.ancestor(ancestorUrl);
    const canvas = document.createElement('canvas');
    canvas.width = canvas.height = whole.width;
    canvas.getContext('2d')!.drawImage(whole, ...enlargeRect(whole.width, k, tile.x, tile.y));
    const blob = await new Promise<Blob | null>(resolve => canvas.toBlob(resolve));
    if (!blob) throw new Error('Could not draw the tile from ' + ancestorUrl);
    return URL.createObjectURL(blob);
  }

  /** One fetch and decode for all the tiles that enlarge the same ancestor. */
  private ancestor(url: string): Promise<ImageBitmap> {
    let image = this.ancestors.get(url);
    if (!image) {
      image = fetch(url)
        .then(r => {
          if (!r.ok) throw new Error('HTTP ' + r.status + ' for ' + url);
          return r.blob();
        })
        .then(b => createImageBitmap(b));
      remember(this.ancestors, url, image, MAX_ANCESTORS);
      image.catch(() => this.ancestors.delete(url));
    }
    return image;
  }
}

/**
 * Makes esri-leaflet's TiledMapLayer draw the deepest level its cache holds, enlarged, instead of the grey
 * "not yet available" tile, on services whose metadata lists Tilemap. Other services, and every tile the cache
 * holds, load as before. Patches the class once; false when there is no esri-leaflet to patch.
 */
export function installEsriTileFallback(leaflet: any): boolean {
  const TiledMapLayer = leaflet?.esri?.TiledMapLayer;
  const proto = TiledMapLayer?.prototype;
  if (!proto?.createTile || !proto?.metadata) return false;
  if (TiledMapLayer['__tileFallbackPatched']) return true;
  TiledMapLayer['__tileFallbackPatched'] = true;

  const fallback = new EsriTileFallback();

  // The layer asks for its service metadata once, when first added, to map map zooms to service levels.
  // Note from the same answer whether the service has a tilemap.
  const metadata = proto.metadata;
  proto.metadata = function (this: any, callback: (...args: any[]) => any, context?: any) {
    const layer = this;
    return metadata.call(
      this,
      function (this: any, error: any, meta: any) {
        if (!error) layer._wfTilemap = /\bTilemap\b/.test(meta?.capabilities || '');
        return callback.call(this, error, meta);
      },
      context,
    );
  };

  // As esri-leaflet 3.0.19's createTile, except that the src waits for the tilemap's answer
  const createTile = proto.createTile;
  proto.createTile = function (this: any, coords: TileAddress, done: any) {
    if (this._wfTilemap === false) return createTile.call(this, coords, done);

    const tile = document.createElement('img');
    leaflet.DomEvent.on(tile, 'load', leaflet.Util.bind(this._tileOnLoad, this, done, tile));
    leaflet.DomEvent.on(tile, 'error', leaflet.Util.bind(this._tileOnError, this, done, tile));
    if (this.options.crossOrigin) tile.crossOrigin = '';
    tile.alt = '';

    const load = () =>
      resolveTile(leaflet, this, fallback, coords).then(src => {
        // Leaflet gives a tile it drops before it loads an empty src: leave that one be
        if (tile.getAttribute('src')) {
          if (src.startsWith('blob:')) URL.revokeObjectURL(src);
          return;
        }
        if (src.startsWith('blob:')) tile.addEventListener('load', () => URL.revokeObjectURL(src), { once: true });
        tile.src = src;
      });

    if (this._lodMap === 'crs' || (this._lodMap && this._lodMap[this._getZoomForUrl()] !== undefined)) load();
    else this.once('lodmap', load, this);
    return tile;
  };

  return true;
}

/** The src for a tile: the tile itself, or its ancestor enlarged where the cache stops short of it. */
async function resolveTile(leaflet: any, layer: any, fallback: EsriTileFallback, coords: TileAddress): Promise<string> {
  const direct = layer.getTileUrl(coords);
  const lodMap = layer._lodMap;
  if (!layer._wfTilemap || !lodMap || lodMap === 'crs') return direct;

  const query = layer.options.token ? `?token=${layer.options.token}` : '';
  try {
    const k = await fallback.levelsUp(layer.options.url, query, coords, zoom => lodMap[zoom]);
    if (!k) return direct;

    const up = { z: coords.z - k, x: coords.x >> k, y: coords.y >> k };
    const ancestorUrl = leaflet.Util.template(
      layer.tileUrl,
      leaflet.Util.extend({ s: layer._getSubdomain(up), x: up.x, y: up.y, z: lodMap[up.z] }, layer.options),
    );
    return await fallback.enlarge(ancestorUrl, k, coords);
  } catch {
    return direct;
  }
}
