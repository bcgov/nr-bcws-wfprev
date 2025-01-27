export const mapConfig = () => ({
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

  ]
});


