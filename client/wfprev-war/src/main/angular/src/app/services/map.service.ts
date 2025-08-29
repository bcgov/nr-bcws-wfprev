import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class MapService {
  private mapIndex: number = 0;
  baseMapIds: string[] = [];
  private readonly smkBaseUrl = `${window.location.protocol}//${window.location.host}/assets/smk/`;
  private smkInstance: any = null;

  getMapIndex(): number {
    return this.mapIndex;
  }

  setMapIndex(index: number): void {
    this.mapIndex = index;
  }

  clearSMKInstance(): void {
    this.smkInstance = null;
  }

  async createSMK(option: any): Promise<any> {
    const SMK = (window as any)['SMK'];
    const mapService = this;

    await this.patch();

    try {
      // Ensure option.config exists and is an array
      if (!option.config) {
        option.config = [];
      } else if (!Array.isArray(option.config)) {
        throw new Error('option.config is not an array');
      }

      // Push the configuration
      option.config.push({
        tools: [{ type: 'baseMaps' }],
      });

      // force regions only to be visible on load
      this.makeOnlyRegionsVisible(option);

      // return only fires for current year - no filter for fire_year in news /features endpoint
      await this.filterWildfireLayersByCurrentYear(option);

      // Initialize SMK
      const smk = await SMK.INIT({
        baseUrl: mapService.smkBaseUrl,
        ...option,
      });

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

      return smk;
    } catch (error) {
      console.error('Error occurred during SMK initialization:', error);
      throw error;
    }
  }

  public async patch(): Promise<any> {
    try {
      const mapService = this;
      const SMK = (window as any)['SMK'];

      console.log('start patching SMK');

      // Create a DIV for a temporary map.
      // This map is used to ensure that SMK is completely loaded before monkey-patching
      const temp = document.createElement('div');
      temp.style.display = 'none';
      temp.style.visibility = 'hidden';
      temp.style.position = 'absolute';
      temp.style.left = '-5000px';
      temp.style.top = '-5000px';
      temp.style.right = '-4000px';
      temp.style.bottom = '-4000px';
      document.body.appendChild(temp);

      console.log('patching');

      // Await the initialization of SMK
      const smk = await SMK.INIT({
        id: 999,
        containerSel: temp,
        baseUrl: mapService.smkBaseUrl,
        config: 'show-tool=bespoke',
      });

      this.installAuthenticatedLegendPatch(SMK);

      this.defineOpenStreetMapLayer();
      smk.destroy();
      temp?.parentElement?.removeChild(temp);

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

      console.log('done patching SMK');

      // Return a resolved promise explicitly for compatibility
      return Promise.resolve();
    } catch (error) {
      console.error('Error occurred during patching:', error);
      throw error; // Re-throw the error to propagate it to the caller
    }
  }

  clone(o: any) {
    return JSON.parse(JSON.stringify(o));
  }

  defineOpenStreetMapLayer() {
    const osmUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
    const L = window['L'];
    const osm = L.tileLayer(osmUrl, {
      maxZoom: 19,
    });
    this.baseMapIds.push('openstreetmap');
    (window as any)['SMK'].TYPE.Viewer.prototype.basemap['openstreetmap'] = {
      title: 'OpenStreetMap',
      create() {
        return [osm];
      }
    };
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

  async filterWildfireLayersByCurrentYear(option: any) {
    const currentFireYear = this.getCurrentFireYear();

    // Only apply filtering to these wildfire layer IDs
    const wildfireLayerIds = new Set([
      'active-wildfires-out-of-control',
      'active-wildfires-holding',
      'active-wildfires-under-control',
      'active-wildfires-out',
    ]);

    const configBlocks = Array.isArray(option?.config) ? option.config : [];

    // Walk through each config block that might contain layers
    await Promise.all(
      configBlocks.map(async (block: any) => {
        if (!Array.isArray(block?.layers)) return;

        await Promise.all(
          block.layers.map(async (layer: any) => {
            // Skip non-vector, non-wildfire, or misconfigured layers
            if (!layer || layer.type !== 'vector' || !wildfireLayerIds.has(layer.id) || !layer.dataUrl) return;

            try {
              // Fetch the original GeoJSON, keeping headers (e.g. API key)
              const res = await fetch(layer.dataUrl, { headers: layer.header || {} });
              if (!res.ok) return; // fallback: leave layer untouched

              const featureCollection = await res.json();
              const features = Array.isArray(featureCollection?.features) ? featureCollection.features : null;
              if (!features) return;

              // Keep only features matching the current fire year
              const filteredFeatures = features.filter((f: any) => {
                const props = f?.properties || f?.attributes || f;
                const yearValue = props?.fire_year ?? props?.FIRE_YEAR ?? props?.fireYear;
                const yearNumber = typeof yearValue === 'string' ? parseInt(yearValue, 10) : yearValue;
                return Number.isFinite(yearNumber) && yearNumber === currentFireYear;
              });

              // Replace original features with the filtered set
              const filteredFeatureCollection = { ...featureCollection, features: filteredFeatures };

              // Convert to blob URL so SMK will reload from it
              const blob = new Blob([JSON.stringify(filteredFeatureCollection)], { type: 'application/json' });
              layer.dataUrl = URL.createObjectURL(blob);

              // No need for headers when using blob URLs
              delete layer.header;
            } catch {
              // On any error, leave the original layer untouched
            }
          })
        );
      })
    );
  }

  installAuthenticatedLegendPatch(SMK: any) {
    const WmsLeaflet = SMK.TYPE?.Layer?.['wms']?.['leaflet'];
    if (!WmsLeaflet?.prototype) return;

    // Ensure we only patch once
    if ((WmsLeaflet.prototype as any).__authLegendPatched) return;
    (WmsLeaflet.prototype as any).__authLegendPatched = true;

    // Keep a reference to SMK’s original legend initializer
    const originalInitLegends = WmsLeaflet.prototype.initLegends;

    // Override SMK’s legend initializer
    WmsLeaflet.prototype.initLegends = function () {
      const J = (window as any).jQuery || (window as any).$;

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
                reader.onerror = reject;
                reader.readAsDataURL(blob);
              } catch (e) {
                reject(e);
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
                      ...(this.config.legend || {}),
                    },
                  ]);
                img.onerror = reject;
                img.src = dataUrl;
              } catch (e) {
                reject(e);
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
      node.forEach(applyVisibility);
      return;
    }

    // If this node is a group/folder with children
    const children = node.layers ?? node.entries;
    if (Array.isArray(children)) {
      // Hide the group itself and recurse into children
      node.visible = false;
      node.isVisible = false; // some configs use this for UI
      children.forEach(applyVisibility);
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
    option.config.forEach((block: any) => {
      if (Array.isArray(block?.layers)) applyVisibility(block.layers);
      if (Array.isArray(block?.entries)) applyVisibility(block.entries);
    });
  }
}

}