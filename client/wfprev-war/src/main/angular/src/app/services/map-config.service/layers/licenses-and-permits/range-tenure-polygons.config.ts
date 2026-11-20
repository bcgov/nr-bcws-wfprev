import { LayerSettings } from "src/app/components/models";

export const RANGE_TENURE_POLYGONS_SLD = `<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" version="1.0.0">
  <sld:NamedLayer>
    <sld:Name>pub:WHSE_FOREST_TENURE.FTEN_RANGE_POLY_SVW</sld:Name>
    <sld:UserStyle>
      <sld:Name>range_tenure_polygons</sld:Name>
      <sld:Title>Range Tenure Polygons</sld:Title>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <sld:Name>FTEN Range Tenures - All</sld:Name>
          <sld:Title>FTEN Range Tenures - All</sld:Title>
          <sld:MaxScaleDenominator>1200000.0</sld:MaxScaleDenominator>
          <sld:PolygonSymbolizer>
            <sld:Fill>
              <sld:CssParameter name="fill">#ffd37f</sld:CssParameter>
              <sld:CssParameter name="fill-opacity">0.5</sld:CssParameter>
            </sld:Fill>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#686868</sld:CssParameter>
            </sld:Stroke>
          </sld:PolygonSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</sld:StyledLayerDescriptor>`;

export function RangeTenurePolygonsLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_FOREST_TENURE.FTEN_RANGE_POLY_SVW/ows`,
    id: "range-tenure-polygons",
    title: "Range Tenure Polygons",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_FOREST_TENURE.FTEN_RANGE_POLY_SVW",
    sld: RANGE_TENURE_POLYGONS_SLD,
    geometryAttribute: "GEOMETRY",
    minScale: 1200000
  }

}
