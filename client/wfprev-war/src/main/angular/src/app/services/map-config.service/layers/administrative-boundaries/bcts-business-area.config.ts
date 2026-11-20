import { LayerSettings } from "src/app/components/models";

export const BCTS_BUSINESS_AREA_SLD = `<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" version="1.0.0">
  <sld:NamedLayer>
    <sld:Name>pub:WHSE_FOREST_TENURE.BCTS_OPERATING_AREAS_SP</sld:Name>
    <sld:UserStyle>
      <sld:Name>bcts_business_area</sld:Name>
      <sld:Title>BCTS Business Area</sld:Title>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <sld:Name>BCTS Business Area</sld:Name>
          <sld:Title>BCTS Business Area</sld:Title>
          <sld:MaxScaleDenominator>2500000.0</sld:MaxScaleDenominator>
          <sld:PolygonSymbolizer>
            <sld:Fill>
              <sld:CssParameter name="fill">#ffa77f</sld:CssParameter>
              <sld:CssParameter name="fill-opacity">0.8</sld:CssParameter>
            </sld:Fill>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#6e6e6e</sld:CssParameter>
              <sld:CssParameter name="stroke-opacity">0.8</sld:CssParameter>
              <sld:CssParameter name="stroke-width">1.4</sld:CssParameter>
            </sld:Stroke>
          </sld:PolygonSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</sld:StyledLayerDescriptor>`;

export function BctsBusinessAreaLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_FOREST_TENURE.BCTS_OPERATING_AREAS_SP/ows`,
    id: "bcts-business-area",
    title: "BCTS Business Area",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_FOREST_TENURE.BCTS_OPERATING_AREAS_SP",
    sld: BCTS_BUSINESS_AREA_SLD,
    geometryAttribute: "SHAPE",
    minScale: 2500000
  }
}