import { LayerSettings } from "src/app/components/models";

export function SpottingImpactConfig(ls: LayerSettings) {
    return {
        serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LAND_AND_NATURAL_RESOURCE.PROT_PSTA_SPOTTING_IMPACT_SP/ows`,
        id: "spotting-impact",
        title: "Spotting Impact",
        visible: true,
        type: "wms",
        isQueryable: true,
        version: "1.1.1",
        transparent: true,
        layerName: "pub:WHSE_LAND_AND_NATURAL_RESOURCE.PROT_PSTA_SPOTTING_IMPACT_SP",
        geometryAttribute: "SHAPE"
    }

}