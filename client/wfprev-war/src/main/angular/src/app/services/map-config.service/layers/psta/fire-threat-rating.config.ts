import { LayerSettings } from "src/app/components/models";

export function FireThreatRatingLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LAND_AND_NATURAL_RESOURCE.PROT_PSTA_FIRE_THREAT_RTG_SP/ows`,
    id: "fire-threat-rating",
    title: "Fire Threat Rating",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_LAND_AND_NATURAL_RESOURCE.PROT_PSTA_FIRE_THREAT_RTG_SP",
    geometryAttribute: "SHAPE"
  }

}