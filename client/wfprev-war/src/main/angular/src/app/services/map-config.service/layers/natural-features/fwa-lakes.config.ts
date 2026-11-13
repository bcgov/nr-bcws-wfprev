import { LayerSettings } from "src/app/components/models";

export function FwaLakesLayerConfig(ls: LayerSettings) {
    return {
        serviceUrl: `${ls.openmaps}/geo/pub/WHSE_BASEMAPPING.FWA_LAKES_POLY/ows`,
        id: "fwa-lakes",
        title: "FWA Lakes",
        visible: true,
        type: "wms",
        isQueryable: true,
        version: "1.1.1",
        transparent: true,
        layerName: "pub:WHSE_BASEMAPPING.FWA_LAKES_POLY",
        geometryAttribute: "GEOMETRY",
        minScale: 140000
    }
}