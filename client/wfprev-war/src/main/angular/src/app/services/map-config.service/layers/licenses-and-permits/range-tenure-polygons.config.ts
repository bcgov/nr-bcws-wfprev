import { LayerSettings } from "src/app/components/models";

export function RangeTenurePolygonsLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LEGAL_ADMIN_BOUNDARIES.ABMS_MUNICIPALITIES_SP/ows`,
    id: "range-tenure-polygons",
    title: "Range Tenure Polygons",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_LEGAL_ADMIN_BOUNDARIES.ABMS_MUNICIPALITIES_SP",
    geometryAttribute: "SHAPE",
    minScale: 10000000
  }

}
