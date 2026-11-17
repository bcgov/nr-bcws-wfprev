import { LayerSettings } from "src/app/components/models";

export function HumanInterfaceBufferLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LAND_AND_NATURAL_RESOURCE.PROT_WUI_HMN_INTRFCE_BUFFR_SP/ows`,
    id: "human-interface-buffer",
    title: "Human Interface Buffer",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_LAND_AND_NATURAL_RESOURCE.PROT_WUI_HMN_INTRFCE_BUFFR_SP",
    geometryAttribute: "SHAPE",
    minScale: 2500000
  }

}