import { LayerSettings } from "src/app/components/models";

export const WUI_RISK_CLASS_SLD = `<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" version="1.0.0">
  <sld:NamedLayer>
    <sld:Name>pub:WHSE_LAND_AND_NATURAL_RESOURCE.PROT_WUI_RISK_CLASS_SP</sld:Name>
    <sld:UserStyle>
      <sld:Name>wui_risk_class</sld:Name>
      <sld:Title>WUI Risk Class</sld:Title>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <sld:Name>WUI Risk Class</sld:Name>
          <sld:Title>WUI Risk Class</sld:Title>
          <sld:MaxScaleDenominator>2500000.0</sld:MaxScaleDenominator>
          <sld:PolygonSymbolizer>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#e600a9</sld:CssParameter>
              <sld:CssParameter name="stroke-width">2</sld:CssParameter>
            </sld:Stroke>
          </sld:PolygonSymbolizer>
          <sld:TextSymbolizer>
            <sld:Geometry>
              <ogc:Function name="centroid">
                <ogc:PropertyName>SHAPE</ogc:PropertyName>
              </ogc:Function>
            </sld:Geometry>
            <sld:Label><ogc:PropertyName>WUI_POLYGON_NAME</ogc:PropertyName> RC: <ogc:PropertyName>RISK_CLASS</ogc:PropertyName></sld:Label>
            <sld:Font>
              <sld:CssParameter name="font-family">Liberation Sans</sld:CssParameter>
              <sld:CssParameter name="font-size">14</sld:CssParameter>
              <sld:CssParameter name="font-style">normal</sld:CssParameter>
              <sld:CssParameter name="font-weight">bold</sld:CssParameter>
            </sld:Font>
            <sld:LabelPlacement>
              <sld:PointPlacement>
                <sld:AnchorPoint>
                  <sld:AnchorPointX>0.5</sld:AnchorPointX>
                  <sld:AnchorPointY>0.5</sld:AnchorPointY>
                </sld:AnchorPoint>
              </sld:PointPlacement>
            </sld:LabelPlacement>
            <sld:Halo>
              <sld:Radius>1.5</sld:Radius>
              <sld:Fill>
                <sld:CssParameter name="fill">#FFFFFF</sld:CssParameter>
              </sld:Fill>
            </sld:Halo>
            <sld:Fill>
              <sld:CssParameter name="fill">#8400a8</sld:CssParameter>
            </sld:Fill>
            <sld:VendorOption name="autoWrap">100</sld:VendorOption>
            <sld:VendorOption name="spaceAround">10</sld:VendorOption>
            <sld:VendorOption name="goodnessOfFit">0.3</sld:VendorOption>
          </sld:TextSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</sld:StyledLayerDescriptor>`;

export function WuiRiskClassLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LAND_AND_NATURAL_RESOURCE.PROT_WUI_RISK_CLASS_SP/ows`,
    id: "wui-risk-class",
    title: "WUI Risk Class",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_LAND_AND_NATURAL_RESOURCE.PROT_WUI_RISK_CLASS_SP",
    sld: WUI_RISK_CLASS_SLD,
    geometryAttribute: "SHAPE",
    minScale: 2500000
  }

}