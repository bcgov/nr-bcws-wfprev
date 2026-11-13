import { LayerSettings } from "src/app/components/models";

export function FwaStreamsLayerConfig(ls: LayerSettings) {
    return {
        serviceUrl: `${ls.openmaps}/geo/pub/WHSE_BASEMAPPING.FWA_STREAM_NETWORKS_SP/ows`,
        id: "fwa-streams",
        title: "FWA Streams",
        visible: true,
        type: "wms",
        isQueryable: true,
        version: "1.1.1",
        transparent: true,
        layerName: "pub:WHSE_BASEMAPPING.FWA_STREAM_NETWORKS_SP",
        geometryAttribute: "GEOMETRY",
        minScale: 70000
    }
}