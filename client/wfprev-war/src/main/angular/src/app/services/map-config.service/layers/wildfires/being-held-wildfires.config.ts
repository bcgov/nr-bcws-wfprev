import { layerSettings } from '..';

export function BeingHeldWildfiresLayerConfig(ls: layerSettings, key?: string) {
     return {
      type: 'vector',
      id: 'active-wildfires-holding',
      title: 'Being Held Wildfires',
      isQueryable: false,
      useClustering: false,
      visible: false,
      dataUrl:
        ls.wfnewsUrl +
        '/publicPublishedIncident/features?stageOfControl=HOLDING',
      titleAttribute: 'incident_name',
      popupTemplate: '@wf-incident-feature',
      header: { apiKey: key },
      attributes: [
        {
          name: 'incident_name',
          title: 'Fire Name',
          visible: false,
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
    }
  }
    
