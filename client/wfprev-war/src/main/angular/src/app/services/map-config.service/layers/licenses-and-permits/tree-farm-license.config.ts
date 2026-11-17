import { LayerSettings } from "src/app/components/models";

export function TreeFarmLicenseLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_ADMIN_BOUNDARIES.FADM_TFL_ALL_SP/ows`,
    id: "tree-farm-license",
    title: "Tree Farm License",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_ADMIN_BOUNDARIES.FADM_TFL_ALL_SP",
    geometryAttribute: "SHAPE",
    minScale: 2500000
  }

}
