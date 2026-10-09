import { LayerSettings } from "src/app/components/models";

export function BCParksAndProtectedAreasLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_TANTALIS.TA_PARK_ECORES_PA_SVW/ows`,
    id: "bc-parks-and-protected-areas",
    title: "BC Parks and Protected Areas",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_TANTALIS.TA_PARK_ECORES_PA_SVW",
    geometryAttribute: "SHAPE",
    minScale: 12000000
  }

}