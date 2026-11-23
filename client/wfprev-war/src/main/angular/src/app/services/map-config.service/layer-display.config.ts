export function LayerDisplayConfig() {
    return [
        {
            id: 'administrative-boundaries',
            type: 'folder',
            title: 'Administrative Boundaries',
            isVisible: true,
            isExpanded: false,
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
                    id: 'bc-wildfire-zones',
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
            isExpanded: false,
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
                },
                {
                    id: 'fwa-wetlands',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 140000,
                }
            ]
        },
        {
            id: 'bc-wildfire-layers',
            type: 'folder',
            title: 'BC Wildfire Layers',
            isVisible: false,
            isExpanded: false,
            items: [
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
                    id: 'wui-risk-class',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 2500000,
                },
                {
                    id: '1-km-wui',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 2500000,
                },
                {
                    id: 'human-interface-buffer',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 2500000,
                },
            ]
        },
        {
            id: 'fuel-treatments',
            type: 'folder',
            title: 'Fuel Treatments',
            isVisible: false,
            isExpanded: false,
            items: [
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
                }
            ]
        },
        {
            id: 'land-ownership-and-status',
            type: 'folder',
            title: 'Land Ownership and Status',
            isVisible: false,
            isExpanded: false,
            items: [
                {
                    id: 'parcel-fabric-ownership',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 40000,
                },
                {
                    id: 'conservancy-area',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 6000000,
                }
            ]
        },
        {
            id: 'licences-and-permits',
            type: 'folder',
            title: 'Licences and Permits',
            isVisible: false,
            isExpanded: false,
            items: [
                {
                    id: 'range-tenure-polygons',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 1200000,
                },
                {
                    id: 'forest-tenure-managed-licence',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 100000,
                },
                {
                    id: 'bc-timber-sales-operating-areas',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 2500000,
                },
                {
                    id: 'tree-farm-licence',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 2500000,
                }
            ]
        },
        {
            id: 'parks-and-recreation-trails',
            type: 'folder',
            title: 'Parks and Recreation Trails',
            isVisible: false,
            isExpanded: false,
            items: [
                {
                    id: 'bc-controlled-recreation-areas',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 1000000,
                },
                {
                    id: 'recreation-polygons',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 250000,
                },
                {
                    id: 'bc-parks-and-protected-areas',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 6000000,
                }
            ]
        },
        {
            id: 'socio-economic',
            type: 'folder',
            title: 'Socio-Economic',
            isVisible: false,
            isExpanded: false,
            items: [
                {
                    id: 'timber-processing-facilities',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 10000000,
                }
            ]
        },
        {
            id: 'physical-infrastructure',
            type: 'folder',
            title: 'Physical Infrastructure',
            isVisible: false,
            isExpanded: false,
            items: [
                {
                    id: 'transmission-lines',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 2000000,
                }
            ]
        },
        {
            id: 'fuel-types',
            type: 'folder',
            title: 'Fuel Type',
            isVisible: false,
            isExpanded: false,
            items: [
                {
                    id: 'fuel-type',
                    isVisible: false,
                    alwaysShowLegend: false,
                    minScale: 50000,
                }
            ]
        },
        {
            id: 'threat-analysis',
            type: 'folder',
            title: 'Threat Analysis',
            isVisible: false,
            isExpanded: false,
            items: [
                {
                    id: 'psta',
                    type: 'folder',
                    title: 'PSTA',
                    isVisible: false,
                    isExpanded: false,
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
                }
            ]
        }
    ];
}