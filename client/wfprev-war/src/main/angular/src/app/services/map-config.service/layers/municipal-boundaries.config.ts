import { LayerSettings } from "src/app/components/models";

export function MunicipalBoundariesLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LEGAL_ADMIN_BOUNDARIES.ABMS_MUNICIPALITIES_SP/ows`,
    id: "municipal-boundaries",
    title: "Municipal Boundaries",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_LEGAL_ADMIN_BOUNDARIES.ABMS_MUNICIPALITIES_SP",
    geometryAttribute: "SHAPE",
    minScale: 500000
  }

}