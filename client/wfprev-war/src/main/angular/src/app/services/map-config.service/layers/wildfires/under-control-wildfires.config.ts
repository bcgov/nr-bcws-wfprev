import { LayerSettings } from "src/app/components/models";

export function UnderControlWildfiresLayerConfig(ls: LayerSettings) {
    
  return {
      type: 'vector',
      id: 'active-wildfires-under-control',
      title: 'Under Control Wildfires',
      isQueryable: true,
      useClustering: false,
      visible: false,
      dataUrl:
        ls.wfnewsApiBaseUrl +
        '/publicPublishedIncident/features?stageOfControl=UNDR_CNTRL',
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
        fillColor: '#98E600',
        fillOpacity: '1',
        fill: true,
      },
      header: {
        apikey: ls.wfnewsApiKey
      }
    }
}
    
