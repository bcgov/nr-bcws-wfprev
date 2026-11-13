export function LayerDisplayConfig() {
    return [
        {
            id: 'administrative-boundaries',
            type: 'folder',
            title: 'Administrative Boundaries',
            isVisible: true,
            isExpanded: true,
            items: [
                {
                    id: 'ministry-of-forests-regions',
                    isVisible: true,
                    alwaysShowLegend: true,
                },
                {
                    id: 'ministry-of-forests-districts',
                    isVisible: false,
                    alwaysShowLegend: true,
                },
                {
                    id: 'municipal-boundaries',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 500000,
                },
                {
                    id: 'wildfire-org-unit-fire-centre',
                    isVisible: false,
                    alwaysShowLegend: true,
                },
                {
                    id: 'regional-districts',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 12000000,
                },
                {
                    id: 'bcts-business-area',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 2500000,
                }
            ]
        },
        {
            id: 'natural-features',
            type: 'folder',
            title: 'Natural Features',
            isVisible: false,
            isExpanded: true,
            items: [
                {
                    id: 'fwa-streams',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 70000,
                },
                {
                    id: 'fwa-lakes',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 140000,
                }
            ]
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
            alwaysShowLegend: false,
            minScale: 20000000,
        },
        {
            id: 'risk-reduction-projects-activities',
            isVisible: false,
            alwaysShowLegend: false,
            minScale: 200000,
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
            id: 'recreation-polygons',
            isVisible: false,
            alwaysShowLegend: false,
            minScale: 250000,
        },
        {
            id: 'managed-licence-woodlot',
            isVisible: false,
            alwaysShowLegend: false,
            minScale: 100000,
        },
        {
            id: 'parcel-fabric',
            isVisible: false,
            alwaysShowLegend: false,
            minScale: 40000,
        },
        {
            id: 'fuel-type',
            isVisible: false,
            alwaysShowLegend: false,
            minScale: 50000,
        },
        {
            id: '1-km-wui',
            isVisible: false,
            alwaysShowLegend: false,
            minScale: 2500000,
        },
        {
            id: 'wui-risk-class',
            isVisible: false,
            alwaysShowLegend: false,
            minScale: 2500000,
        },
        {
            id: 'mountain-resorts-branch',
            isVisible: false,
            alwaysShowLegend: false,
            minScale: 1000000,
        }
    ];
}