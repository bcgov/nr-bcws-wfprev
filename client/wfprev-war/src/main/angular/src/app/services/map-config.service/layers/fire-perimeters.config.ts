import { LayerSettings } from "src/app/components/models";

export function FirePerimetersLayerConfig(ls: LayerSettings, authHeader?: Record<string, string>) {
  
  return {
    serviceUrl: ls.geoserverApiBaseUrl + "/ows",
    id: "fire-perimeters",
    title: "Current Wildfire Perimeters",
    visible: false,
    type: "wms",
    isQueryable: false,
    combiningClass: "wfBoundary",
    layerName: "IN_CURRENT_FIRE_POLYGONS_SVW",
    header: authHeader
  }
}