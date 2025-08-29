import { layerSettings } from '.';

export function FirePerimetersLayerConfig(ls: layerSettings, authHeader?: Record<string, string>) {
  
  return {
    serviceUrl: ls.geoserverApiBaseUrl + "/ows",
    id: "fire-perimeters",
    title: "Fire Perimeters",
    visible: false,
    type: "wms",
    isQueryable: false,
    combiningClass: "wfBoundary",
    layerName: "IN_CURRENT_FIRE_POLYGONS_SVW",
    header: authHeader
  }
}