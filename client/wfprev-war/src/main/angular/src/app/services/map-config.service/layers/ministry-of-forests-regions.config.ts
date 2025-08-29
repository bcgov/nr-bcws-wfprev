import { layerSettings } from ".";

export function MinistryOfForestsRegionsLayerConfig(ls: layerSettings, authHeader?: Record<string, string>) {
  return {
    serviceUrl: ls.geoserverApiBaseUrl + "/ows",
    id: "ministry-of-forests-regions",
    title: "Ministry of Forests Regions",
    visible: true,
    type: "wms",
    isQueryable: false,
    combiningClass: "wfBoundary",
    layerName: "ADM_NR_REGIONS_SPG",
    header: authHeader
  }
}