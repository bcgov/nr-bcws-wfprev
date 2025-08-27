import { layerSettings } from ".";

export function WildfireOrgUnitFireCentreLayerConfig(ls: layerSettings, token?: string) {
    return {
    serviceUrl: ls.geoserverBaseUrl + "/ows",
    id: "wildfire-org-unit-fire-centre",
    title: "BCWS Fire Centres",
    visible: false,
    type: "wms",
    isQueryable: false,
    combiningClass: "wfBoundary",
    layerName: "WILDFIRE_ORG_UNIT_FIRE_CENTRE",
    header: {
      Authorization: `Bearer ${token}`
    } 
  }

}