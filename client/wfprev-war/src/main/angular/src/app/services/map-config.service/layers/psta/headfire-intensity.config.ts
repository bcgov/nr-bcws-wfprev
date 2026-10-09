import { LayerSettings } from "src/app/components/models";

export function HeadfireIntensityLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LAND_AND_NATURAL_RESOURCE.PROT_PSTA_HEAD_FIRE_INTNSTY_SP/ows`,
    id: "headfire-intensity",
    title: "Headfire Intensity",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_LAND_AND_NATURAL_RESOURCE.PROT_PSTA_HEAD_FIRE_INTNSTY_SP",
    geometryAttribute: "SHAPE",
    minScale: 150000
  }

}