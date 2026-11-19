import { LayerSettings } from "src/app/components/models";

export function TransmissionLinesLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_BASEMAPPING.GBA_TRANSMISSION_LINES_SP/ows`,
    id: "transmission-lines",
    title: "Transmission Lines",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_BASEMAPPING.GBA_TRANSMISSION_LINES_SP",
    geometryAttribute: "SHAPE",
    minScale: 2000000
  }
}