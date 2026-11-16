import { LayerSettings } from "src/app/components/models";

export function BcWildfireZonesLayerConfig(ls: LayerSettings) {
  return {
    id: "bc-wildfire-zones",
    type: 'wms',
    title: "BC Wildfire Zones",
    serviceUrl: `${ls.wildfiremaps}/geoserver/wms`,
    isQueryable: false,
    layerName: "WILDFIRE_ORG_UNIT_FIRE_ZONE",
    legend: {
      title: ' ',
      clipWidth: 20,
      clipHeight: 20,
    }
  }
}