import { LayerSettings } from "src/app/components/models";

export function ConservancyAreaLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_TANTALIS.TA_CONSERVANCY_AREAS_SVW/ows`,
    id: "conservancy-area",
    title: "Conservancy Area",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_TANTALIS.TA_CONSERVANCY_AREAS_SVW",
    geometryAttribute: "SHAPE"
  }

}