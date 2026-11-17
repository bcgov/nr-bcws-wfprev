import { LayerSettings } from "src/app/components/models";

export const BC_WILDFIRE_ZONES_SLD = `<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" version="1.0.0">
  <sld:NamedLayer>
    <sld:Name>WILDFIRE_ORG_UNIT_FIRE_ZONE</sld:Name>
    <sld:UserStyle>
      <sld:Name>bc_wildfire_zones</sld:Name>
      <sld:Title>BC Wildfire Zones</sld:Title>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <sld:Name>Fire Zone</sld:Name>
          <sld:Title>Fire Zone</sld:Title>
          <sld:MaxScaleDenominator>4500000.0</sld:MaxScaleDenominator>
          <sld:LineSymbolizer>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#FFFFFF</sld:CssParameter>
              <sld:CssParameter name="stroke-width">2.5</sld:CssParameter>
            </sld:Stroke>
          </sld:LineSymbolizer>
          <sld:LineSymbolizer>
            <sld:Stroke/>
          </sld:LineSymbolizer>
          <sld:TextSymbolizer>
            <sld:Label>
              <ogc:PropertyName>FIRE_ZONE_NAME</ogc:PropertyName>
              <![CDATA[ -  ]]>
              <ogc:PropertyName>CHARACTER_ALIAS</ogc:PropertyName>
            </sld:Label>
            <sld:Font>
              <sld:CssParameter name="font-family">BC Sans Regular</sld:CssParameter>
              <sld:CssParameter name="font-size">13</sld:CssParameter>
              <sld:CssParameter name="font-style">normal</sld:CssParameter>
              <sld:CssParameter name="font-weight">normal</sld:CssParameter>
            </sld:Font>
            <sld:LabelPlacement>
              <sld:PointPlacement>
                <sld:AnchorPoint>
                  <sld:AnchorPointX>0.5</sld:AnchorPointX>
                  <sld:AnchorPointY>0.0</sld:AnchorPointY>
                </sld:AnchorPoint>
                <sld:Displacement>
                  <sld:DisplacementX>0</sld:DisplacementX>
                  <sld:DisplacementY>5</sld:DisplacementY>
                </sld:Displacement>
              </sld:PointPlacement>
            </sld:LabelPlacement>
            <sld:Halo>
              <sld:Radius>1.5</sld:Radius>
              <sld:Fill>
                <sld:CssParameter name="fill">#FFFFFF</sld:CssParameter>
              </sld:Fill>
            </sld:Halo>
            <sld:Fill>
              <sld:CssParameter name="fill">#000000</sld:CssParameter>
            </sld:Fill>
            <sld:VendorOption name="maxDisplacement">50</sld:VendorOption>
            <sld:VendorOption name="spaceAround">10</sld:VendorOption>
          </sld:TextSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</sld:StyledLayerDescriptor>`;

export function BcWildfireZonesLayerConfig(ls: LayerSettings) {
  return {
    id: "bc-wildfire-zones",
    type: 'wms',
    title: "BC Wildfire Zones",
    serviceUrl: `${ls.wildfiremaps}/geoserver/wms`,
    isQueryable: true,
    layerName: "WILDFIRE_ORG_UNIT_FIRE_ZONE",
    geometryAttribute: "GEOMETRY",
    titleAttribute: "FIRE_ZONE_NAME",
    sld: BC_WILDFIRE_ZONES_SLD,
    minScale: 4500000
  }
}
