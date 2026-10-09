import { LayerSettings } from "src/app/components/models";

export function FireStartDensityAllLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LAND_AND_NATURAL_RESOURCE.PROT_PSTA_FIRE_STRT_DENSITY_SP/ows`,
    id: "fire-start-density-all",
    title: "Fire Start Density All",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_LAND_AND_NATURAL_RESOURCE.PROT_PSTA_FIRE_STRT_DENSITY_SP",
    geometryAttribute: "SHAPE",
    minScale: 150000
  }

}