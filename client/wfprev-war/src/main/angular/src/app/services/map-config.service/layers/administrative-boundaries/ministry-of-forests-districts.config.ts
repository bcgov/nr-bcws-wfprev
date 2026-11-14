import { LayerSettings } from "src/app/components/models";

export function MinistryOfForestsDistrictsLayerConfig(ls: LayerSettings, authHeader?: Record<string, string>) {
  return {
    serviceUrl: ls.geoserverApiBaseUrl + "/ows",
    id: "ministry-of-forests-districts",
    title: "Natural Resource Districts",
    visible: false,
    type: "wms",
    isQueryable: false,
    combiningClass: "wfBoundary",
    layerName: "ADM_NR_DISTRICTS_SPG",
    header: authHeader
  }

}