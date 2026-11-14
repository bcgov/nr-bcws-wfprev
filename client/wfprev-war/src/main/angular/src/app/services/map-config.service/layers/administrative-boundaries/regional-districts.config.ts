import { LayerSettings } from "src/app/components/models";

export function RegionalDistrictsLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LEGAL_ADMIN_BOUNDARIES.ABMS_REGIONAL_DISTRICTS_SP/ows`,
    id: "regional-districts",
    title: "Regional Districts",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_LEGAL_ADMIN_BOUNDARIES.ABMS_REGIONAL_DISTRICTS_SP",
    geometryAttribute: "SHAPE",
    minScale: 12000000
  }
}