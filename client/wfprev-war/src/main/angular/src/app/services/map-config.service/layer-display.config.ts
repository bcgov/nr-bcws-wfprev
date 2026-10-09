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
            alwaysShowLegend: false,
            minScale: 6000000,
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
                    alwaysShowLegend: false,
                    minScale: 150000,
                },
                {
                    id: 'headfire-intensity',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 150000,
                },
                {
                    id: 'fire-threat-rating',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 50000,
                },
                {
                    id: 'fire-start-density-all',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 150000,
                },
            ]
        },
        {
            id: 'conservancy-area',
            isVisible: false,
            alwaysShowLegend: false,
            minScale: 6000000,
        },
        {
            id: 'municipal-boundaries',
            isVisible: false,
            alwaysShowLegend: false,
            minScale: 500000,
        },
        {
            id: 'recreation-polygons',
            isVisible: false,
            alwaysShowLegend: false,
            minScale: 250000,
        },
        {
            id: 'managed-licence-woodlot',
            isVisible: false,
            alwaysShowLegend: true,
        },
        {
            id: 'parcel-fabric',
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
        },
        {
            id: 'wui-risk-class',
            isVisible: false,
            alwaysShowLegend: false,
            minScale: 2500000,
        }

    ];
}