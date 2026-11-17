import { LayerSettings } from "src/app/components/models";

export const ONE_KM_WUI_SLD = `<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" version="1.0.0">
  <sld:NamedLayer>
    <sld:Name>pub:WHSE_LAND_AND_NATURAL_RESOURCE.PROT_WUI_ELIGIBLE_BUFFER_SP</sld:Name>
    <sld:UserStyle>
      <sld:Name>1_km_wui</sld:Name>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <sld:Title/>
          <sld:MaxScaleDenominator>2500000.0</sld:MaxScaleDenominator>
          <sld:PolygonSymbolizer>
            <sld:Fill>
              <sld:CssParameter name="fill">#e1e1e1</sld:CssParameter>
              <sld:CssParameter name="fill-opacity">0.8</sld:CssParameter>
            </sld:Fill>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#686868</sld:CssParameter>
              <sld:CssParameter name="stroke-width">2</sld:CssParameter>
            </sld:Stroke>
          </sld:PolygonSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</sld:StyledLayerDescriptor>`;

export function OneKMWUILayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LAND_AND_NATURAL_RESOURCE.PROT_WUI_ELIGIBLE_BUFFER_SP/ows`,
    id: "1-km-wui",
    title: "1 km WUI",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_LAND_AND_NATURAL_RESOURCE.PROT_WUI_ELIGIBLE_BUFFER_SP",
    sld: ONE_KM_WUI_SLD,
    geometryAttribute: "SHAPE",
    minScale: 2500000
  }
}
