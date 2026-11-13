import { LayerSettings } from "src/app/components/models";

export function BctsBusinessAreaLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_FOREST_TENURE.BCTS_OPERATING_AREAS_SP/ows`,
    id: "bcts-business-area",
    title: "BCTS Business Area",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_FOREST_TENURE.BCTS_OPERATING_AREAS_SP",
    geometryAttribute: "SHAPE",
    minScale: 2500000
  }
}