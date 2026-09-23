import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { BC_BOUNDS, FiscalYearColors } from 'src/app/utils/constants';
import { TokenService } from './token.service';
import { StyleSpecification } from 'maplibre-gl';
import * as L from 'leaflet';
import { AppConfigService } from './app-config.service';
import { SmkService } from './smk.service';
import { baseMapsToolConfig } from './map-config.service/map.config';
import '@maplibre/maplibre-gl-leaflet';

@Injectable({ providedIn: 'root' })
export class MapService {
  private mapIndex: number = 0;
  private containerId?: string;
  private smkInstance: any = null;
  private readonly apiBaseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api`;
  private mapContainer: HTMLElement | null = null;
  private patched?: Promise<void>;
  // The element the SMK map was created in, the token its layers were created with, and the off-screen holder
  // the map waits in while the map page isn't shown (see detachSMK)
  private smkFrame: HTMLElement | null = null;
  private smkToken?: string | null;
  private parking: HTMLElement | null = null;

  constructor(private readonly tokenService: TokenService,
    private readonly appConfigService: AppConfigService,
    private readonly smkService: SmkService
  ) { }

  getMapIndex(): number {
    return this.mapIndex;
  }

  setMapIndex(index: number): void {
    this.mapIndex = index;
  }

  clearSMKInstance(): void {
    this.smkInstance = null;
  }

  setContainerId(id: string) {
    this.containerId = id;
    this.mapContainer = document.getElementById(id);
  }

  createMaplibreGLLayer(options: any): any {
    return (L as any).maplibreGL(options);
  }

  /**
   * Creates an SMK map, with our patches installed. Used for the main map and the mini maps (MiniMapService).
   *
   * SMK creates maps one at a time, each waiting on the one before it (SMK.BOOT), so once one map fails to
   * initialize every later one fails with it. Earlier failures are ignored, so that each map stands on its own.
   */
  async initSMK(option: any): Promise<any> {
    await this.patch();
    const SMK = (globalThis as any)['SMK'];
    SMK.BOOT = (SMK.BOOT ?? Promise.resolve()).catch(() => undefined);
    return SMK.INIT({
      baseUrl: this.smkService.baseUrl,
      ...option,
    });
  }

  async createSMK(option: any): Promise<any> {
    try {
      // Ensure option.config exists and is an array
      if (!option.config) {
        option.config = [];
      } else if (!Array.isArray(option.config)) {
        throw new TypeError('option.config must be an array');
      }

      // Push the configuration
      option.config.push({
        tools: [
          baseMapsToolConfig(),
          {
            type: 'bespoke',
            instance: 'full-extent',
            title: 'Zoom to Full Extent',
            enabled: true,
            position: 'actionbar',
            showTitle: false,
            showPanel: false,
            icon: 'zoom_out_map',
            order: 3,
          },
        ],
      });

      // Initialize SMK
      const smk = await this.initSMK(option);

      // only show Ministry of Forests Regions layer by default
      const viewer = smk?.$viewer;
      const layers = viewer?.displayContext?.layers;

      if (layers) {
        layers.setItemVisible('ministry-of-forests-regions', true);
        layers.setItemVisible('ministry-of-forests-districts', false);
        layers.setItemVisible('wildfire-org-unit-fire-centre', false);
        layers.setItemVisible('fire-perimeters', false);
        layers.setItemVisible('active-wildfires-out-of-control', false);
        layers.setItemVisible('active-wildfires-holding', false);
        layers.setItemVisible('active-wildfires-under-control', false);
        layers.setItemVisible('active-wildfires-out', false);
        await viewer.updateLayersVisible?.();
      }

      this.smkInstance = smk;
      this.smkFrame = typeof option.containerSel === 'string' ? document.querySelector(option.containerSel) : option.containerSel;
      this.smkToken = this.tokenService.getOauthToken?.();

      return smk;
    } catch (error) {
      console.error('Error occurred during SMK initialization:', error);
      throw error;
    }
  }

  /**
   * Keeps the SMK map alive when the map page is left. Its frame (the element SMK was created in) waits in an
   * off-screen holder, at its current size, so the next visit can reattach it instead of creating a new map.
   */
  detachSMK(): void {
    const frame = this.smkFrame;
    if (!this.smkInstance || !frame) return;

    if (!this.parking) {
      this.parking = document.createElement('div');
      Object.assign(this.parking.style, { position: 'fixed', left: '-10000px', top: '0', visibility: 'hidden', pointerEvents: 'none' });
      document.body.appendChild(this.parking);
    }

    // A zero-sized map would confuse SMK and Leaflet while it waits; keep the size it was shown at
    const { width, height } = frame.getBoundingClientRect();
    Object.assign(frame.style, { width: `${width}px`, height: `${height}px` });
    // Other pages also have an element with this id
    frame.dataset['parkedId'] = frame.id;
    frame.removeAttribute('id');
    this.parking.appendChild(frame);
  }

  /**
   * Puts a parked SMK map back in the map page, in place of the page's empty map container, and returns the SMK
   * instance. Returns null, so the caller creates a new map, if there is nothing parked, or if the auth token has
   * changed since the map's layers were created with it.
   */
  reattachSMK(container: HTMLElement): any {
    const frame = this.smkFrame;
    if (!this.smkInstance || !frame || !this.parking || frame.parentElement !== this.parking) return null;

    if (this.smkToken !== this.tokenService.getOauthToken?.()) {
      try {
        this.smkInstance.destroy?.();
      } catch (error) {
        console.error('Error occurred during SMK destruction:', error);
      }
      frame.remove();
      this.clearSMKInstance();
      this.smkFrame = null;
      return null;
    }

    container.replaceWith(frame);
    frame.id = frame.dataset['parkedId'] ?? container.id;
    delete frame.dataset['parkedId'];
    frame.style.width = '';
    frame.style.height = '';
    this.mapContainer = frame;
    this.smkInstance.$viewer?.map?.invalidateSize({ animate: false });
    return this.smkInstance;
  }

  /** Loads SMK and installs our patches to it, once per session. Every map waits on the same promise. */
  public patch(): Promise<void> {
    this.patched ??= this.installPatches().catch(error => {
      console.error('Error occurred during patching:', error);
      this.patched = undefined;
      throw error;
    });
    return this.patched;
  }

  private async installPatches(): Promise<void> {
    const SMK = await this.smkService.load();

    // SMK fades each new map in over a second, and SMK.INIT doesn't resolve until the fade ends, so every map took
    // a second longer to be ready. The fade is the only jQuery animation there is: jQuery comes with smk.js and the
    // app doesn't use it. With jQuery's animations off, a map shows as soon as it's ready.
    const jQuery = (globalThis as any).jQuery;
    if (jQuery?.fx) {
      jQuery.fx.off = true;
    }

    SMK.HANDLER.set('BespokeTool--full-extent', 'triggered', (smk: any, tool: any) => {
      const viewer = smk?.$viewer;
      if (!viewer) return;
      const bounds = BC_BOUNDS;
      viewer.map.fitBounds(bounds, { animate: true });
    });

    this.installAuthenticatedLegendPatch(SMK);
    this.installCurrentFireYearPatch(SMK);

    // SMK waits 200ms before showing or hiding layers, so that a run of visibility changes is handled at once. Every
    // map waited for it as it was created, and so did each layer turned on in the layers panel. Changes made together
    // are still handled at once. (SMK reads a delay of 0 as none given.)
    const refreshLayers = SMK.TYPE.Viewer.leaflet.prototype.refreshLayers;
    if (typeof refreshLayers === 'function') {
      SMK.TYPE.Viewer.leaflet.prototype.refreshLayers = function (delay?: number) {
        return refreshLayers.call(this, delay || 1);
      };
    }

    // Patch the SMK Viewer functionality
    SMK.TYPE.Viewer.leaflet.prototype.mapResized = () => {
      const prototype = SMK.TYPE.Viewer.leaflet.prototype;
      setTimeout(() => {
        prototype.map.invalidateSize({ animate: false });
      }, 500);
    };

    const oldInit = SMK.TYPE.Viewer.leaflet.prototype.initialize;
    SMK.TYPE.Viewer.leaflet.prototype.initialize = function (smk: any) {
      // Call the existing initializer
      oldInit.apply(this, arguments);

      // Set the maximum bounds that can be panned to.
      const L = window['L'];
      const maxBounds = L.latLngBounds([
        L.latLng(90, -180),
        L.latLng(0, -90),
      ]);
      this.map.setMaxBounds(maxBounds);
      this.map.setMaxZoom(19);
    };
  }

  async destroySMK(): Promise<void> {
    const smk = this.smkInstance;
    if (!smk) return;

    try {
      if (typeof smk.destroy === 'function') {
        smk.destroy();
        await Promise.resolve();
      }
    } catch (error) {
      console.error('Error occurred during SMK destruction:', error);
    } finally {
      this.clearSMKInstance();
      if (this.mapContainer) {
        this.mapContainer.innerHTML = '';
        delete (this.mapContainer as any)._leaflet_id;
      }
    }
  }

  clone<T>(obj: T): T {
    return structuredClone(obj);
  }

  getSMKInstance() {
    return this.smkInstance;
  }

  getCurrentFireYear(d: Date = new Date()): number {
    // Fire year: Apr 1 → Mar 31
    const y = d.getFullYear();
    const m = d.getMonth();
    return m < 3 ? y - 1 : y; // Jan/Feb/Mar => previous year
  }

  /**
   * Keeps only the current fire year's fires on the wildfire layers (`currentFireYearOnly` in their config), which
   * the wfnews API can't filter by year. SMK fetches a vector layer's data as it creates the layer, when the layer is
   * first shown, so the data is fetched and filtered then, and a layer that's never turned on is never downloaded.
   */
  installCurrentFireYearPatch(SMK: any) {
    const VectorLeaflet = SMK.TYPE?.Layer?.['vector']?.['leaflet'];
    const create = VectorLeaflet?.create;
    if (typeof create !== 'function' || VectorLeaflet['__fireYearPatched']) return;
    VectorLeaflet['__fireYearPatched'] = true;

    const service = this;
    VectorLeaflet.create = async function (this: any, layers: any[], ...rest: any[]) {
      const config = layers?.[0]?.config;
      if (config?.currentFireYearOnly && config.dataUrl && !config.dataUrl.startsWith('blob:')) {
        config.dataUrl = await service.currentFireYearDataUrl(config.dataUrl, config.header);
      }
      return create.call(this, layers, ...rest);
    };
  }

  /**
   * A blob URL of the GeoJSON at the URL, keeping only the current fire year's features. The URL itself if the GeoJSON
   * can't be read, so the layer still shows every fire rather than none.
   */
  async currentFireYearDataUrl(url: string, headers?: Record<string, string>): Promise<string> {
    try {
      const res = await fetch(url, { headers: headers ?? {} });
      if (!res.ok) return url;

      const featureCollection = await res.json();
      if (!Array.isArray(featureCollection?.features)) return url;

      const currentFireYear = this.getCurrentFireYear();
      const features = featureCollection.features.filter((f: any) => {
        const props = f?.properties || f?.attributes || f;
        const yearValue = props?.fire_year ?? props?.FIRE_YEAR ?? props?.fireYear;
        const yearNumber = typeof yearValue === 'string' ? Number.parseInt(yearValue, 10) : yearValue;
        return yearNumber === currentFireYear;
      });

      const blob = new Blob([JSON.stringify({ ...featureCollection, features })], { type: 'application/json' });
      return URL.createObjectURL(blob);
    } catch {
      return url;
    }
  }

  installAuthenticatedLegendPatch(SMK: any) {
    const WmsLeaflet = SMK.TYPE?.Layer?.['wms']?.['leaflet'];
    if (!WmsLeaflet?.prototype) return;

    // Ensure we only patch once
    if (WmsLeaflet.prototype["__authLegendPatched"]) return;
    WmsLeaflet.prototype["__authLegendPatched"] = true;

    // Keep a reference to SMK’s original legend initializer
    const originalInitLegends = WmsLeaflet.prototype.initLegends;

    // Override SMK’s legend initializer
    WmsLeaflet.prototype.initLegends = function () {
      const J = (globalThis as any).jQuery || (globalThis as any).$;

      // Build a base GetLegendGraphic request
      const svc = this.config.serviceUrl || '';
      const base = svc.includes('?') ? svc : (svc + '?');

      // If jQuery is available, use it to build the querystring
      const query = J
        ? J.param({
          SERVICE: 'WMS',
          VERSION: '1.1.1',
          REQUEST: 'GetLegendGraphic',
          FORMAT: 'image/png',
          TRANSPARENT: 'true',
          LAYER: this.config.layerName,
          STYLE: this.config.styleName || undefined,
        })
        : [
          'SERVICE=WMS',
          'VERSION=1.1.1',
          'REQUEST=GetLegendGraphic',
          'FORMAT=image/png',
          'TRANSPARENT=true',
          `LAYER=${encodeURIComponent(this.config.layerName)}`,
          this.config.styleName ? `STYLE=${encodeURIComponent(this.config.styleName)}` : '',
        ]
          .filter(Boolean)
          .join('&');

      const url = base.endsWith('?') ? base + query : base + '&' + query;
      const headers = this.config.header || {}; // includes API key / auth headers

      // Fetch the legend graphic with headers
      return fetch(url, {
        method: 'GET',
        headers,
        mode: 'cors',
        credentials: 'omit',
      })
        .then((res) => {
          if (!res.ok) throw new Error(`Legend HTTP ${res.status}`);
          return res.blob();
        })
        // Convert the blob into a data URL (so SMK can use it inline)
        .then(
          (blob) =>
            new Promise<string>((resolve, reject) => {
              try {
                const reader = new FileReader();
                reader.onload = () => resolve(reader.result as string);
                reader.onerror = () => reject(new Error('Failed to read legend blob'));
                reader.readAsDataURL(blob);
              } catch (e: any) {
                reject(new Error(e?.message || 'Unknown error while reading blob'));
              }
            })
        )
        // Load into an <img> to measure width/height
        .then(
          (dataUrl: string) =>
            new Promise<any[]>((resolve, reject) => {
              try {
                const img = new Image();
                img.onload = () =>
                  resolve([
                    {
                      url: dataUrl,
                      width: img.width,
                      height: img.height,
                      ...this.config.legend,
                    },
                  ]);
                img.onerror = () => reject(new Error('Failed to load legend image'));
                img.src = dataUrl;
              } catch (e: any) {
                reject(new Error(e?.message || 'Unknown error while loading image'));
              }
            })
        )
        .catch((err) => {
          console.warn('Authenticated legend fetch failed:', err);

          // If our patch fails, fall back to SMK’s original legend logic
          if (typeof originalInitLegends === 'function') {
            try {
              return originalInitLegends.call(this);
            } catch {
              /* ignore */
            }
          }
          return [];
        });
    };
  }

  makeOnlyRegionsVisible(option: any) {
    // IDs of layers that should remain visible
    const visibleLayerIds = new Set(['ministry-of-forests-regions']);


    // Recursive helper to apply visibility rules
    const applyVisibility = (node: any) => {
      if (!node) return;

      // If this node is an array, process each element
      if (Array.isArray(node)) {
        for (const child of node) {
          applyVisibility(child);
        }
        return;
      }

      // If this node is a group/folder with children
      const children = node.layers ?? node.entries;
      if (Array.isArray(children)) {
        // Hide the group itself and recurse into children
        node.visible = false;
        node.isVisible = false; // some configs use this for UI
        for (const child of children) {
          applyVisibility(child);
        }
        return;
      }

      // If this node is a leaf layer
      if (node.id) {
        const shouldBeVisible = visibleLayerIds.has(node.id);
        node.visible = shouldBeVisible;   // drives the map engine
        node.isVisible = shouldBeVisible; // keeps UI in sync
      }
    };

    // SMK configs can put layers in different places
    if (Array.isArray(option.layers)) {
      applyVisibility(option.layers);
    }
    if (Array.isArray(option.config)) {
      for (const block of option.config) {
        if (Array.isArray(block?.layers)) applyVisibility(block.layers);
        if (Array.isArray(block?.entries)) applyVisibility(block.entries);
      }
    }
  }

  private ensurePane(map: L.Map, paneName: string, zIndex = 400): void {
    let pane = map.getPane(paneName);
    if (!pane) {
      pane = map.createPane(paneName);
      pane.style.zIndex = String(zIndex);
    }
  }

  private toQueryString(filters: any): string {
    let httpParams = new HttpParams();
    if (filters) {
      for (const key in filters) {
        const value = filters[key];
        if (Array.isArray(value)) {
          value.forEach(v => (httpParams = httpParams.append(key, v)));
        } else if (value != null && value !== '') {
          httpParams = httpParams.set(key, value);
        }
      }
    }
    const query = httpParams.toString();
    return query ? `?${query}` : '';
  }

  createProjectBoundaryLayer(map: L.Map, filters: any): L.Layer {
    this.ensurePane(map, 'pane-project-boundary-gl', 401);

    const tiles = `${this.apiBaseUrl}/tiles/project_boundary/{z}/{x}/{y}.mvt${this.toQueryString(filters)}`;

    const style: StyleSpecification = {
      version: 8,
      sources: {
        projectBoundary: { type: 'vector', tiles: [tiles] }
      },
      layers: [
        { id: 'project-boundary-fill', type: 'fill', source: 'projectBoundary', 'source-layer': 'project_boundary', paint: { 'fill-opacity': 0.1 },  minzoom: 10 },
        { id: 'project-boundary-line', type: 'line', source: 'projectBoundary', 'source-layer': 'project_boundary', paint: { 'line-color': '#000', 'line-width': 2 }, minzoom: 10 }
      ]
    };

    const token = this.tokenService.getOauthToken?.();
    return this.createMaplibreGLLayer({
      style,
      pane: 'pane-project-boundary-gl',
      transformRequest: (url: string) =>
        token && url.startsWith(this.apiBaseUrl)
          ? { url, headers: { Authorization: `Bearer ${token}` } }
          : { url }
    });
  }

  createActivityBoundaryLayer(map: L.Map, filters: any, currentFiscalYear: number): L.Layer {
    this.ensurePane(map, 'pane-activity-boundary-gl', 400);

    const tiles = `${this.apiBaseUrl}/tiles/activity_boundary/{z}/{x}/{y}.mvt${this.toQueryString(filters)}`;

    const style: StyleSpecification = {
      version: 8,
      sources: {
        activityBoundary: { type: 'vector', tiles: [tiles] }
      },
      layers: [
        { id: 'activity-boundary-fill', type: 'fill', source: 'activityBoundary', 'source-layer': 'activity_boundary', paint: { 'fill-opacity': 0.1 }, minzoom: 10 },
        {
          id: 'activity-boundary-line',
          type: 'line',
          source: 'activityBoundary',
          'source-layer': 'activity_boundary',
          minzoom: 10,
          paint: {
            'line-color': [
              'case',
              ['<', ['to-number', ['get', 'fiscal_year']], currentFiscalYear], FiscalYearColors.past,
              ['>', ['to-number', ['get', 'fiscal_year']], currentFiscalYear], FiscalYearColors.future,
              FiscalYearColors.present
            ],
            'line-width': 2
          }
        }
      ]
    };

    const token = this.tokenService.getOauthToken?.();
    return this.createMaplibreGLLayer({
      style,
      pane: 'pane-activity-boundary-gl',
      transformRequest: (url: string) =>
        token && url.startsWith(this.apiBaseUrl)
          ? { url, headers: { Authorization: `Bearer ${token}` } }
          : { url }
    });
  }

  async addLayersToExistingSMKInstance(mapState: any): Promise<void> {
    const viewer = this.smkInstance?.$viewer;
    if (!viewer || !Array.isArray(mapState?.layers)) return;

    const SMK = (globalThis as any)['SMK'];

    // Register each layer into viewer.layerId and viewer.layerIds
    for (const layerConfig of mapState.layers) {
      try {
        viewer.addLayer(layerConfig);
      } catch (err) {
        console.error(`Failed to add layer ${layerConfig?.id}:`, err);
      }
    }

    // Replace the empty displayContext.layers with a new one including our layers  
    const layerItems = mapState.layers.map((l: any) => ({
      id: l.id,
      type: 'layer',
      isVisible: l.id === 'ministry-of-forests-regions',
      isEnabled: true,
      title: l.title ?? l.id,
    }));

    viewer.displayContext.layers = new SMK.TYPE.LayerDisplayContext(
      layerItems,
      viewer.layerId
    );

    // Wire up the changedVisibility callback like SMK does internally
    viewer.displayContext.layers.changedVisibility(() => {
      viewer.changedLayerVisibility();
    });

    // Apply visibility and render. SMK creates a layer on the map when it's first shown (updateLayersVisible), so
    // the hidden layers cost nothing until they're turned on.
    const dc = viewer.displayContext?.layers;
    if (dc) {
      dc.setItemVisible('ministry-of-forests-regions', true);
      dc.setItemVisible('ministry-of-forests-districts', false);
      dc.setItemVisible('wildfire-org-unit-fire-centre', false);
      dc.setItemVisible('fire-perimeters', false);
      dc.setItemVisible('active-wildfires-out-of-control', false);
      dc.setItemVisible('active-wildfires-holding', false);
      dc.setItemVisible('active-wildfires-under-control', false);
      dc.setItemVisible('active-wildfires-out', false);
    }

    await viewer.updateLayersVisible?.();
  }


}