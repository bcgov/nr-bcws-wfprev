import { LayerSettings } from "src/app/components/models";

export function WildfireOrgUnitFireCentreLayerConfig(ls: LayerSettings, authHeader?: Record<string, string>) {
    return {
    serviceUrl: ls.geoserverApiBaseUrl + "/ows",
    id: "wildfire-org-unit-fire-centre",
    title: "BC Wildfire Service Fire Centre Boundaries",
    visible: false,
    type: "wms",
    isQueryable: false,
    combiningClass: "wfBoundary",
    layerName: "WILDFIRE_ORG_UNIT_FIRE_CENTRE",
    header: authHeader
  }

}