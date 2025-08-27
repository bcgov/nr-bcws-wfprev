import { MapServicesConfig } from "src/app/interfaces/application-config";
import { MapConfigService, MapServices } from ".";


export function LayerDisplayConfig(mapServices: MapServices) {
    return [
        {
            id: 'ministry-of-forests-districts',
            isVisible: false,
            alwaysShowLegend: false,
        },
        {
            id: 'ministry-of-forests-regions',
            isVisible: false,
            alwaysShowLegend: false,
        },
        {
            id: 'wildfire-org-unit-fire-centre',
            isVisible: false,
            alwaysShowLegend: false,
        },
        {
            id: 'fire-perimeters',
            isVisible: false,
            class: 'smk-inline-legend',
        },
        {
            id: 'active-wildfires-out-of-control',
            isVisible: false,
            alwaysShowLegend: false,
        },
        {
            id: 'active-wildfires-holding',
            isVisible: false,
            alwaysShowLegend: false,
        },
        {
            id: 'active-wildfires-under-control',
            isVisible: false,
            alwaysShowLegend: false,
        },
        {
            id: 'active-wildfires-out',
            isVisible: false,
            alwaysShowLegend: false,
        },

    ];
}