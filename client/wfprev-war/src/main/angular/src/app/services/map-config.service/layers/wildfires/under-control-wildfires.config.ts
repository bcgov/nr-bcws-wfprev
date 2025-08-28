import { layerSettings } from '..';

export function UnderControlWildfiresLayerConfig(ls: layerSettings, key?: string) {
    
  return {
      type: 'vector',
      id: 'active-wildfires-under-control',
      title: 'Under Control Wildfires',
      isQueryable: false,
      useClustering: false,
      visible: false,
      dataUrl:
        ls.wfnewsUrl +
        '/publicPublishedIncident/features?stageOfControl=UNDR_CNTRL',
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
        fillColor: '#98E600',
        fillOpacity: '1',
        fill: true,
      },
    }
}
    
