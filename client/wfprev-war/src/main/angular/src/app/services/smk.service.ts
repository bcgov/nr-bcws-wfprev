import { Injectable } from '@angular/core';

/**
 * Loads SMK (Simple Map Kit) once per session. smk.js itself is a global script (angular.json), but SMK only
 * loads its Leaflet viewer, Leaflet plugins and basemap registry on first use. This loads them through SMK's own
 * module loader (window.include, which SMK.INIT calls internally), so no map has to be created to get them and
 * none of SMK.INIT's timers are waited on.
 */
@Injectable({ providedIn: 'root' })
export class SmkService {
  readonly baseUrl = `${globalThis.location.protocol}//${globalThis.location.host}/assets/smk/`;
  private loaded?: Promise<any>;

  /** Resolves with the SMK global once its Leaflet viewer modules are loaded. Every caller shares one load. */
  load(): Promise<any> {
    this.loaded ??= this.loadModules().catch(error => {
      // Let the next caller try again
      this.loaded = undefined;
      throw error;
    });
    return this.loaded;
  }

  /** Starts loading SMK while the browser is idle, so the first map doesn't wait for it. */
  preload(): void {
    const start = () => this.load().catch(error => console.warn('SMK preload failed', error));
    if (typeof globalThis.requestIdleCallback === 'function') {
      globalThis.requestIdleCallback(start, { timeout: 5000 });
    } else {
      setTimeout(start, 2000);
    }
  }

  private async loadModules(): Promise<any> {
    const include = (globalThis as any).include;
    include.option({ baseUrl: `${this.baseUrl}assets/src/` });
    await include('smk-map');
    await include('viewer-leaflet');
    return (globalThis as any).SMK;
  }
}
