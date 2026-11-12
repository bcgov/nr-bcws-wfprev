import { LayerSettings } from "src/app/components/models";

export const MUNICIPAL_BOUNDARIES_SLD = `<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" version="1.0.0">
  <sld:NamedLayer>
    <sld:Name>pub:WHSE_LEGAL_ADMIN_BOUNDARIES.ABMS_MUNICIPALITIES_SP</sld:Name>
    <sld:UserStyle>
      <sld:Name>municipal_boundaries</sld:Name>
      <sld:Title>Municipal Boundaries</sld:Title>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <sld:Name>Municipalities</sld:Name>
          <sld:Title>Municipalities</sld:Title>
          <sld:MaxScaleDenominator>10000000.0</sld:MaxScaleDenominator>
          <sld:PolygonSymbolizer>
            <sld:Fill>
              <sld:CssParameter name="fill">#e8beff</sld:CssParameter>
              <sld:CssParameter name="fill-opacity">0.8</sld:CssParameter>
            </sld:Fill>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#686868</sld:CssParameter>
              <sld:CssParameter name="stroke-opacity">0.8</sld:CssParameter>
              <sld:CssParameter name="stroke-width">1.4</sld:CssParameter>
            </sld:Stroke>
          </sld:PolygonSymbolizer>
          <sld:TextSymbolizer>
            <sld:Geometry>
              <ogc:Function name="centroid">
                <ogc:PropertyName>SHAPE</ogc:PropertyName>
              </ogc:Function>
            </sld:Geometry>
            <sld:Label>
              <ogc:PropertyName>ADMIN_AREA_NAME</ogc:PropertyName>
            </sld:Label>
            <sld:Font>
              <sld:CssParameter name="font-family">Liberation Sans</sld:CssParameter>
              <sld:CssParameter name="font-size">11</sld:CssParameter>
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
              <sld:Radius>2</sld:Radius>
              <sld:Fill>
                <sld:CssParameter name="fill">#FFFFFF</sld:CssParameter>
              </sld:Fill>
            </sld:Halo>
            <sld:Fill>
              <sld:CssParameter name="fill">#111111</sld:CssParameter>
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

export function MunicipalBoundariesLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LEGAL_ADMIN_BOUNDARIES.ABMS_MUNICIPALITIES_SP/ows`,
    id: "municipal-boundaries",
    title: "Municipal Boundaries",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_LEGAL_ADMIN_BOUNDARIES.ABMS_MUNICIPALITIES_SP",
    sld: MUNICIPAL_BOUNDARIES_SLD,
    geometryAttribute: "SHAPE",
    minScale: 500000
  }

}
