import { layerSettings } from '..';

export function OutWildfiresLayerConfig(ls: layerSettings) {
    
  return {
      type: 'vector',
      id: 'active-wildfires-out',
      title: 'Declared Out Wildfires',
      isQueryable: true,
      useClustering: false,
      useHeatmap: false,
      visible: false,
      dataUrl:
        ls.wfnewsApiBaseUrl +
        '/publicPublishedIncident/features?stageOfControl=OUT',
      titleAttribute: 'incident_name',
      popupTemplate: '@wf-incident-feature',
      attributes: [
        {
          name: 'incident_name',
          title: 'Fire Name',
          visible: true,
        },
        {
          name: 'fire_year',
          title: 'Fire Year',
          visible: true,
        },
      ],
      style: {
        strokeWidth: '7',
        strokeStyle: '1',
        strokeColor: '#5c6671',
        strokeOpacity: '1',
        fillColor: '#5c6671',
        fillOpacity: '1',
        fill: true,
      },
      header: {
        apikey: ls.wfnewsApiKey
      }
    }
}
  