import { LayerSettings } from "src/app/components/models";

export const MOUNTAIN_RESORTS_BRANCH_SLD = `<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" version="1.0.0">
  <sld:NamedLayer>
    <sld:Name>pub:REG_LEGAL_AND_ADMIN_BOUNDARIES.REC_TENURE_ALPINE_SKI_AREAS_SP</sld:Name>
    <sld:UserStyle>
      <sld:Name>mountain_resorts_branch</sld:Name>
      <sld:Title>Mountain Resorts Branch</sld:Title>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <sld:Name>Controlled Recreation Area</sld:Name>
          <sld:Title>Controlled Recreation Area</sld:Title>
          <sld:MaxScaleDenominator>1000000.0</sld:MaxScaleDenominator>
          <sld:PolygonSymbolizer>
            <sld:Fill>
              <sld:GraphicFill>
                <sld:Graphic>
                  <sld:Mark>
                    <sld:WellKnownName>shape://times</sld:WellKnownName>
                    <sld:Stroke>
                      <sld:CssParameter name="stroke">#33cfff</sld:CssParameter>
                      <sld:CssParameter name="stroke-opacity">0.8</sld:CssParameter>
                    </sld:Stroke>
                  </sld:Mark>
                  <sld:Size>20</sld:Size>
                </sld:Graphic>
              </sld:GraphicFill>
            </sld:Fill>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#00c3ff</sld:CssParameter>
              <sld:CssParameter name="stroke-opacity">0.8</sld:CssParameter>
              <sld:CssParameter name="stroke-width">1.5</sld:CssParameter>
            </sld:Stroke>
          </sld:PolygonSymbolizer>
          <sld:TextSymbolizer>
            <sld:Geometry>
              <ogc:Function name="centroid">
                <ogc:PropertyName>SHAPE</ogc:PropertyName>
              </ogc:Function>
            </sld:Geometry>
            <sld:Label>
              <ogc:PropertyName>RESORT_NAME</ogc:PropertyName>
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
              <sld:Radius>1</sld:Radius>
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

export function MountainResortsBranchLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/REG_LEGAL_AND_ADMIN_BOUNDARIES.REC_TENURE_ALPINE_SKI_AREAS_SP/ows`,
    id: "mountain-resorts-branch",
    title: "Mountain Resorts Branch",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:REG_LEGAL_AND_ADMIN_BOUNDARIES.REC_TENURE_ALPINE_SKI_AREAS_SP",
    sld: MOUNTAIN_RESORTS_BRANCH_SLD,
    geometryAttribute: "SHAPE",
    minScale: 1000000
  }
}
