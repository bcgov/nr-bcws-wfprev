export function LayerDisplayConfig() {
    return [
        {
            id: 'ministry-of-forests-districts',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'ministry-of-forests-regions',
            isVisible: true,
            alwaysShowLegend: true,
        },
        {
            id: 'wildfire-org-unit-fire-centre',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'fire-perimeters',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'active-wildfires-out-of-control',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'active-wildfires-holding',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'active-wildfires-under-control',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'active-wildfires-out',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'legacy-fuel-treatments',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'risk-reduction-projects-activities',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'bc-parks-and-protected-areas',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'psta',
            type: 'folder',
            title: 'PSTA',
            isVisible: false,
            isExpanded: true,
            items: [
                {
                    id: 'spotting-impact',
                    isVisible: false,
                    alwaysShowLegend: true,
                },
                {
                    id: 'headfire-intensity',
                    isVisible: false,
                    alwaysShowLegend: true,
                },
                {
                    id: 'fire-threat-rating',
                    isVisible: false,
                    alwaysShowLegend: true,
                },
                {
                    id: 'fire-start-density-all',
                    isVisible: false,
                    alwaysShowLegend: true,
                },
            ]
        },
        {
            id: 'conservancy-area',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'municipal-boundaries',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'recreation-polygons',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'managed-licence-woodlot',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'fuel-type',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: '1-km-wui',
            isVisible: false,
            alwaysShowLegend: true,
        }

    ];
}