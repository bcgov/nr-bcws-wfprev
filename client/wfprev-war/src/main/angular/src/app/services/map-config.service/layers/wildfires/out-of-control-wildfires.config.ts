import { layerSettings } from '..';

export function OutOfControlWildfiresLayerConfig(ls: layerSettings) {
  return {
      type: 'vector',
      id: 'active-wildfires-out-of-control',
      title: 'Out of Control Wildfires',
      isQueryable: false,
      useClustering: false,
      visible: false,
      dataUrl:
        ls.wfnewsApiBaseUrl +
        '/publicPublishedIncident/features?stageOfControl=OUT_CNTRL',
      titleAttribute: 'incident_name',
      popupTemplate: '@wf-incident-feature',
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
        fillColor: '#FF0000',
        fillOpacity: '1',
        fill: true,
      },
    }
}