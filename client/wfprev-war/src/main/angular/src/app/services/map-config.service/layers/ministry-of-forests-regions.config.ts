import { layerSettings } from ".";

export function MinistryOfForestsRegionsLayerConfig(ls: layerSettings, token?: string) {
    return {
    serviceUrl: ls.geoserverBaseUrl + "/ows",
    id: "ministry-of-forests-regions",
    title: "Ministry of Forests Regions",
    visible: true,
    type: "wms",
    isQueryable: false,
    combiningClass: "wfBoundary",
    layerName: "ADM_NR_REGIONS_SPG",
    header: {
      Authorization: `Bearer ${token}`
    } 
  }
}