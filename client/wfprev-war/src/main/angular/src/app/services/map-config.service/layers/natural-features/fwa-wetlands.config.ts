import { LayerSettings } from "src/app/components/models";

export function FwaWetlandsLayerConfig(ls: LayerSettings) {
    return {
        serviceUrl: `${ls.openmaps}/geo/pub/WHSE_BASEMAPPING.FWA_WETLANDS_POLY/ows`,
        id: "fwa-wetlands",
        title: "FWA Wetlands",
        visible: true,
        type: "wms",
        isQueryable: true,
        version: "1.1.1",
        transparent: true,
        layerName: "pub:WHSE_BASEMAPPING.FWA_WETLANDS_POLY",
        geometryAttribute: "GEOMETRY",
        minScale: 140000
    }
}