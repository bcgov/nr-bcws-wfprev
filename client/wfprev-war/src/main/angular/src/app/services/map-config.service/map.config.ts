import { MapServices } from ".";
import { LayerDisplayConfig } from "./layer-display.config";
import { LayerConfig } from "./layers";

export const mapConfig = (
  mapServices: MapServices,
  token?: string
) => ({
  viewer: {
    type: 'leaflet',
    location: {
      extent: [-136.3, 45, -116, 64.2],
    },
    baseMap: 'openstreetmap',
    minZoom: 4,
    maxZoom: 30,
  },
  tools: [
    {
      type: 'pan',
      enabled: true,
    },
    {
      type: 'zoom',
      enabled: true,
      mouseWheel: true,
      doubleClick: true,
      box: true,
      control: true,
    },
    {
      type: 'search',
      enabled: false,
    },
    {
      type: 'location',
      enabled: false
    },
    {
      type: 'identify',
      enabled: true
    },
    {
      type: 'layers',
      enabled: true,
      showTitle: true,
      position: 'shortcut-menu',
      glyph: {
        visible: 'check_box',
        hidden: 'check_box_outline_blank',
      },
      command: {
        allVisibility: true,
        filter: true,
        legend: true,
      },
      legend: true,
      order: 2,
      display: LayerDisplayConfig(),
    },
  ],
  layers: LayerConfig(mapServices, token)
});


