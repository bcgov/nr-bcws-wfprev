import { LayerSettings } from "src/app/components/models";

export const RESULTS_ACTIVITY_TREATMENT_SLD = `<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" version="1.0.0">
  <sld:NamedLayer>
    <sld:Name>pub:WHSE_FOREST_VEGETATION.RSLT_ACTIVITY_TREATMENT_SVW</sld:Name>
    <sld:UserStyle>
      <sld:Name>results_activity_treatment</sld:Name>
      <sld:Title>RESULTS - Wildfire Risk Reduction Project Activities</sld:Title>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <sld:Name>Treatment Base Code - Technique (amount HA)</sld:Name>
          <sld:Title>Treatment Base Code - Technique (amount HA)</sld:Title>
          <sld:MaxScaleDenominator>200000.0</sld:MaxScaleDenominator>
          <sld:PolygonSymbolizer>
            <sld:Fill>
              <sld:GraphicFill>
                <sld:Graphic>
                  <sld:Mark>
                    <sld:WellKnownName>shape://backslash</sld:WellKnownName>
                    <sld:Stroke>
                      <sld:CssParameter name="stroke">#38a800</sld:CssParameter>
                    </sld:Stroke>
                  </sld:Mark>
                  <sld:Size>20</sld:Size>
                </sld:Graphic>
              </sld:GraphicFill>
            </sld:Fill>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#38a800</sld:CssParameter>
              <sld:CssParameter name="stroke-width">2</sld:CssParameter>
            </sld:Stroke>
          </sld:PolygonSymbolizer>
          <sld:TextSymbolizer>
            <sld:Geometry>
              <ogc:Function name="centroid">
                <ogc:PropertyName>GEOMETRY</ogc:PropertyName>
              </ogc:Function>
            </sld:Geometry>
            <sld:Label><ogc:PropertyName>SILV_BASE_CODE</ogc:PropertyName> - <ogc:PropertyName>SILV_TECHNIQUE_CODE</ogc:PropertyName> <ogc:PropertyName>DISTURBANCE_CODE</ogc:PropertyName> (<ogc:PropertyName>ACTUAL_TREATMENT_AREA</ogc:PropertyName> HA)</sld:Label>
            <sld:Font>
              <sld:CssParameter name="font-family">Liberation Sans</sld:CssParameter>
              <sld:CssParameter name="font-size">14</sld:CssParameter>
              <sld:CssParameter name="font-style">italic</sld:CssParameter>
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
              <sld:CssParameter name="fill">#4c7300</sld:CssParameter>
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

export function ResultsActivityTreatmentLayerConfig(ls: LayerSettings) {
  const where =
    "GEOMETRY_EXIST_IND = 'Y' AND RESULTS_IND = 'Y' AND " +
    "(SILV_FUND_SOURCE_CODE IN ('CF','FEP','WRR') " +
    "OR (SILV_FUND_SOURCE_CODE = 'FES' AND FIA_PROJECT_ID LIKE 'WR%')) " +
    "AND SILV_BASE_CODE NOT IN('LB','SU') " +
    "AND (SILV_OBJECTIVE_CODE_1 IN ('HAZ','FRE') " +
    "OR SILV_OBJECTIVE_CODE_2 IN ('HAZ','FRE') " +
    "OR SILV_OBJECTIVE_CODE_3 IN ('HAZ','FRE'))";

  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_FOREST_VEGETATION.RSLT_ACTIVITY_TREATMENT_SVW/ows`,
    id: "risk-reduction-projects-activities",
    title: "RESULTS - Wildfire Risk Reduction Project Activities",
    type: "wms",
    visible: true,
    isQueryable: true,
    layerName: "pub:WHSE_FOREST_VEGETATION.RSLT_ACTIVITY_TREATMENT_SVW",
    sld: RESULTS_ACTIVITY_TREATMENT_SLD,
    version: "1.1.1",
    where,
    transparent: true,
    minScale: 200000
  };
}
