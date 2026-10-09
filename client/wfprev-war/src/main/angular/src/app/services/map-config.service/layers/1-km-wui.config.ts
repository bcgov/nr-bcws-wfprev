import { LayerSettings } from "src/app/components/models";

export function OneKMWUILayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LAND_AND_NATURAL_RESOURCE.PROT_WUI_RISK_CLASS_SP/ows`,
    id: "1-km-wui",
    title: "1 km WUI",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_LAND_AND_NATURAL_RESOURCE.PROT_WUI_RISK_CLASS_SP",
    geometryAttribute: "SHAPE",
    minScale: 2500000
  }

}