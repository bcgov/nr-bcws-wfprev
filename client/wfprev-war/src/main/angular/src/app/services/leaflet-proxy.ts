import * as L from 'leaflet';

export const leafletProxy = {
  maplibreGL: (opts: any): any => (L as any).maplibreGL(opts),
  map: (element: any, options?: any): L.Map => L.map(element, options),
  marker: (latlng: L.LatLngExpression, options?: L.MarkerOptions): L.Marker => L.marker(latlng, options),
  geoJSON: (geojson?: any, options?: any): L.GeoJSON => L.geoJSON(geojson, options),
  tileLayer: (url: string, options?: any): L.TileLayer => L.tileLayer(url, options),
  featureGroup: (layers?: L.Layer[]): L.FeatureGroup => L.featureGroup(layers),
  layerGroup: (layers?: L.Layer[]): L.LayerGroup => L.layerGroup(layers),
  markerClusterGroup: (options?: any): L.MarkerClusterGroup => L.markerClusterGroup(options),
};