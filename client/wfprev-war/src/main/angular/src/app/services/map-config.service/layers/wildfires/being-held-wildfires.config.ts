import { layerSettings } from '..';

export function BeingHeldWildfiresLayerConfig(ls: layerSettings) {
     return {
      type: 'vector',
      id: 'active-wildfires-holding',
      title: 'Being Held Wildfires',
      isQueryable: true,
      useClustering: false,
      visible: false,
      dataUrl:
        ls.wfnewsApiBaseUrl+
        '/publicPublishedIncident/features?stageOfControl=HOLDING',
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
        strokeColor: '#00000069',
        strokeOpacity: '1',
        fillColor: '#FFFF00',
        fillOpacity: '1',
        fill: true,
      },
      header: {
        apikey: ls.wfnewsApiKey
      }
    }
  }
    
