import { layerSettings } from '.';

export function FirePerimetersLayerConfig(ls: layerSettings, token?: string) {

  return {
    serviceUrl: ls.geoserverApiBaseUrl + "/ows",
    id: "fire-perimeters",
    title: "Fire Perimeters",
    visible: false,
    type: "wms",
    isQueryable: false,
    combiningClass: "wfBoundary",
    layerName: "IN_CURRENT_FIRE_POLYGONS_SVW",
    header: {
      Authorization: `Bearer ${token}`
    }
  }
}