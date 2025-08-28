import { layerSettings } from ".";

export function MinistryOfForestsDistrictsLayerConfig(ls: layerSettings, token?: string) {
  return {
    serviceUrl: ls.geoserverApiBaseUrl + "/ows",
    id: "ministry-of-forests-districts",
    title: "Ministry of Forests Districts",
    visible: false,
    type: "wms",
    isQueryable: false,
    combiningClass: "wfBoundary",
    layerName: "ADM_NR_DISTRICTS_SPG",
    header: {
      Authorization: `Bearer ${token}`
    }
  }

}