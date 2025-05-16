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

  createSMK(option: any) {
    const SMK = (window as any)['SMK'];
    const mapService = this;

    return this.patch()
      .then(() => {
        try {
          // Ensure option.config exists and is an array
          if (!option.config) {
            option.config = [];
          } else if (!Array.isArray(option.config)) {
            throw new Error('option.config is not an array');
          }

          // Push the configuration
          option.config.push({
            tools: [
              {
                type: 'baseMaps',
              }
            ],
          });

          // Initialize SMK
          return SMK.INIT({
            baseUrl: mapService.smkBaseUrl,
            ...option,
          }).then((smk: any) =>{
            this.smkInstance = smk;
            return smk;
          });
        } catch (error) {
          console.error('Error occurred during SMK initialization:', error);
          throw error;
        }
      })
      .catch((error: any) => {
        console.error('Error occurred during patching:', error);
        throw error; // Re-throw the error to propagate it to the caller
      });
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

  defineOpenStreetMapLayer(){
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
}