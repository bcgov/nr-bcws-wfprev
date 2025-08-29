import { layerSettings } from ".";

export function WildfireOrgUnitFireCentreLayerConfig(ls: layerSettings, authHeader?: Record<string, string>) {
    return {
    serviceUrl: ls.geoserverApiBaseUrl + "/ows",
    id: "wildfire-org-unit-fire-centre",
    title: "BCWS Fire Centres",
    visible: false,
    type: "wms",
    isQueryable: false,
    combiningClass: "wfBoundary",
    layerName: "WILDFIRE_ORG_UNIT_FIRE_CENTRE",
    header: authHeader
  }

}