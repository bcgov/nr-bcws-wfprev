import { LayerSettings } from "src/app/components/models";

export function RecreationPolygonsLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_FOREST_TENURE.FTEN_RECREATION_POLY_SVW/ows`,
    id: "recreation-polygons",
    title: "Recreation Polygons",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_FOREST_TENURE.FTEN_RECREATION_POLY_SVW",
    geometryAttribute: "GEOMETRY",
    minScale: 250000
  }

}