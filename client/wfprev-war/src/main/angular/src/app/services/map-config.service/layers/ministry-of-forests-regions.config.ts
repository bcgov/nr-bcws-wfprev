import { LayerSettings } from "src/app/components/models";

export function MinistryOfForestsRegionsLayerConfig(ls: LayerSettings, authHeader?: Record<string, string>) {
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