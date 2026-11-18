import { LayerSettings } from "src/app/components/models";

export function TimberProcessingFacilitiesLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_IMAGERY_AND_BASE_MAPS.GSR_TMBR_PRCSSING_FAC_SV/ows`,
    id: "timber-processing-facilities",
    title: "Timber Processing Facilities",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_IMAGERY_AND_BASE_MAPS.GSR_TMBR_PRCSSING_FAC_SV",
    geometryAttribute: "SHAPE",
    minScale: 10000000
  }
}