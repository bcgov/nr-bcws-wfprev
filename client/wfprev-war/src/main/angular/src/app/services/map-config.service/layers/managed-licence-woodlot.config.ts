import { LayerSettings } from "src/app/components/models";

export const MANAGED_LICENCE_WOODLOT_SLD = `<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" version="1.0.0">
  <sld:NamedLayer>
    <sld:Name>pub:WHSE_FOREST_TENURE.FTEN_MANAGED_LICENCE_POLY_SVW</sld:Name>
    <sld:UserStyle>
      <sld:Name>managed_licence_woodlot</sld:Name>
      <sld:Title>Managed Licence - Woodlot</sld:Title>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <sld:Name>Managed Licence Polygon Outlines</sld:Name>
          <sld:Title>Managed Licence Polygon Outlines</sld:Title>
          <sld:PolygonSymbolizer>
            <sld:Fill>
              <sld:CssParameter name="fill">#009933</sld:CssParameter>
              <sld:CssParameter name="fill-opacity">0.5</sld:CssParameter>
            </sld:Fill>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#ff6500</sld:CssParameter>
              <sld:CssParameter name="stroke-opacity">0.5</sld:CssParameter>
              <sld:CssParameter name="stroke-width">1</sld:CssParameter>
            </sld:Stroke>
          </sld:PolygonSymbolizer>
          <sld:TextSymbolizer>
            <sld:Geometry>
              <ogc:Function name="centroid">
                <ogc:PropertyName>GEOMETRY</ogc:PropertyName>
              </ogc:Function>
            </sld:Geometry>
            <sld:Label>
              <ogc:PropertyName>MAP_LABEL</ogc:PropertyName>
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

export function ManagedLicenceWoodlotLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_FOREST_TENURE.FTEN_MANAGED_LICENCE_POLY_SVW/ows`,
    id: "managed-licence-woodlot",
    title: "Managed Licence - Woodlot",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_FOREST_TENURE.FTEN_MANAGED_LICENCE_POLY_SVW",
    sld: MANAGED_LICENCE_WOODLOT_SLD,
    geometryAttribute: "GEOMETRY",
    minScale: 100000
  }
}