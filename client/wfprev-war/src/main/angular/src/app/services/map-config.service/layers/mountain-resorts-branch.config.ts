import { LayerSettings } from "src/app/components/models";

export function MountainResortsBranchLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/REG_LEGAL_AND_ADMIN_BOUNDARIES.REC_TENURE_ALPINE_SKI_AREAS_SP/ows`,
    id: "mountain-resorts-branch",
    title: "Mountain Resorts Branch",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:REG_LEGAL_AND_ADMIN_BOUNDARIES.REC_TENURE_ALPINE_SKI_AREAS_SP",
    geometryAttribute: "SHAPE"
  }

}