import { Injectable } from '@angular/core';
import * as L from 'leaflet';
import { miniMapConfig } from './map-config.service/map.config';
import { MapService } from './map.service';

// SMK maps allow zooming to 19 (see MapService.patch); keep the limit the mini maps have always had
const MAX_ZOOM = 18;
// A mini map's container, and the class that hides it while the map is created (styles.scss)
export const MINI_MAP_CLASS = 'wf-mini-map';
export const LOADING_CLASS = 'wf-mini-map-loading';

export interface MiniMap {
  smk: any;
  map: L.Map;
  /**
   * SMK's own copy of Leaflet, not the app's `leaflet` module. Build everything drawn on the map with it: the two
   * copies number their layers separately, so their ids can clash on one map, and neither accepts the other's
   * LatLngBounds.
   */
  leaflet: typeof L;
}

/**
 * What a mini map shows: an area, fitted with `padding` pixels to spare on each side, or a point at a zoom level.
 * Plain [lat, lng] numbers, which both copies of Leaflet accept.
 */
export type MiniMapView =
  | { bounds: L.LatLngBoundsLiteral; padding?: number }
  | { center: L.LatLngTuple; zoom: number };

/** Moves the map to the view at once. An animated move looks like the map loading zoomed out, then zooming in. */
export function showView(map: L.Map, view: MiniMapView): void {
  if ('bounds' in view) {
    const padding = view.padding ?? 0;
    map.fitBounds(view.bounds, { padding: [padding, padding], animate: false });
  } else {
    map.setView(view.center, view.zoom, { animate: false });
  }
}

/**
 * The area that covers every one of the GeoJSON geometries, or undefined when there are none. Geometries Leaflet
 * can't read are left out.
 */
export function boundsOf(geometries: any[]): L.LatLngBoundsLiteral | undefined {
  const bounds = L.latLngBounds([]);
  for (const geometry of geometries) {
    try {
      if (geometry) {
        bounds.extend(L.geoJSON(geometry).getBounds());
      }
    } catch {
      // Not GeoJSON Leaflet can draw either
    }
  }
  return bounds.isValid()
    ? [[bounds.getSouth(), bounds.getWest()], [bounds.getNorth(), bounds.getEast()]]
    : undefined;
}

/**
 * The small maps on the project details and fiscal pages and in the spatial file viewer. Each is an SMK map, like
 * the main map, with the approved basemaps and SMK's basemap switcher and zoom buttons.
 */
@Injectable({ providedIn: 'root' })
export class MiniMapService {
  private created = 0;

  constructor(private readonly mapService: MapService) { }

  /**
   * Creates a mini map in the container, showing the view. The view can be a promise, for a page still loading what
   * the map shows: the map is created meanwhile, and kept hidden until it shows the view, so it never appears on
   * SMK's starting view of all of BC first. It then fades in (styles.scss). Resolves once the map is showing.
   */
  async create(container: HTMLElement, view?: MiniMapView | Promise<MiniMapView | undefined>): Promise<MiniMap> {
    container.classList.add(MINI_MAP_CLASS, LOADING_CLASS);
    try {
      const [smk, start] = await Promise.all([
        this.mapService.initSMK({
          // SMK's default id is the number of maps it has, which repeats once a map is destroyed
          id: `mini-map-${++this.created}`,
          containerSel: container,
          config: [miniMapConfig()],
        }),
        // A view that fails to load leaves the map on BC, rather than losing the map
        Promise.resolve(view).catch(() => undefined),
      ]);
      const map: L.Map = smk.$viewer.map;
      map.setMaxZoom(MAX_ZOOM);

      // The project details map is created in a tab that may not be showing, outside the page, which the next two
      // undo. There Leaflet can't see SMK's styles, so it pins the map to position: relative, which overrides SMK's
      // absolute positioning and leaves the map with no height once shown.
      map.getContainer().style.removeProperty('position');
      // And the map has no size until the tab opens. Sizing it then can move it outside the bounds MapService.patch
      // gives every SMK map, and Leaflet's pan back inside them outlasts the page's own fit to its boundary, leaving
      // the map off target. The mini maps never had those bounds.
      map.setMaxBounds(null as unknown as L.LatLngBounds);

      if (start) {
        showView(map, start);
      }
      return { smk, map, leaflet: (globalThis as any).L };
    } finally {
      container.classList.remove(LOADING_CLASS);
    }
  }

  /** Removes the map, and with it the vector basemap's WebGL context. */
  destroy(miniMap: MiniMap | undefined): void {
    try {
      miniMap?.smk.destroy();
    } catch (error) {
      console.error('Error occurred during SMK destruction:', error);
    }
  }
}
