import { BASEMAPS, DEFAULT_BASEMAP_ID } from "src/app/utils/constants";
import { MapServices } from ".";
import { LayerDisplayConfig } from "./layer-display.config";
import { LayerConfig } from "./layers";

// The approved basemaps, in switcher order, with a static preview image per option instead of SMK's live mini map
const basemapsViewerConfig = () => ({
  baseMap: DEFAULT_BASEMAP_ID,
  baseMapConfig: BASEMAPS.map(({ id, order, previewUrl }) => ({ id, order, optionImageUrl: previewUrl })),
});

// SMK ships this tool disabled; enable it so users can switch between the approved basemaps
export const baseMapsToolConfig = () => ({
  type: 'baseMaps',
  enabled: true,
  choices: BASEMAPS.map(basemap => basemap.id),
});

// The mini maps (project details, fiscal map, spatial file viewer): the approved basemaps and their switcher, with
// panning and zooming. SMK turns its search and location tools on unless told otherwise. It starts on BC.
export const miniMapConfig = () => ({
  viewer: {
    type: 'leaflet',
    // SMK would lay out a map this narrow for a phone
    device: 'desktop',
    themes: ['wf'],
    ...basemapsViewerConfig(),
    minZoom: 4,
  },
  tools: [
    { type: 'pan', enabled: true },
    { type: 'zoom', enabled: true, mouseWheel: true, doubleClick: true, box: true, control: true },
    baseMapsToolConfig(),
    { type: 'search', enabled: false },
    { type: 'location', enabled: false },
  ],
});

// Starts on SMK's own view of BC, the same bounds as BC_BOUNDS
export const mapConfigBase = (mapServices: MapServices, token?: string) => ({
  viewer: {
    type: 'leaflet',
    ...basemapsViewerConfig(),
    minZoom: 4,
    maxZoom: 30,
  },
  tools: [
    { type: 'pan', enabled: true },
    { type: 'zoom', enabled: true, mouseWheel: true, doubleClick: true, box: true, control: true },
    { type: 'search', enabled: false },
    { type: 'location', enabled: false },
    { type: 'identify', enabled: true },
    {
      type: 'layers',
      enabled: true,
      showTitle: false,
      position: 'shortcut-menu',
      glyph: { visible: 'check_box', hidden: 'check_box_outline_blank' },
      command: { allVisibility: true, filter: true, legend: true },
      legend: true,
      order: 2,
      display: LayerDisplayConfig(),
    },
  ],
});

export const mapConfigLayers = (mapServices: MapServices, token?: string) => ({
  layers: LayerConfig(mapServices, token)
});

export const mapConfig = (mapServices: MapServices, token?: string) => ({
  ...mapConfigBase(mapServices, token),
  ...mapConfigLayers(mapServices, token),
});
