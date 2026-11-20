import { LayerSettings } from "src/app/components/models";

export const PARCEL_FABRIC_OWNERSHIP_SLD = `<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" version="1.0.0">
  <sld:NamedLayer>
    <sld:Name>pub:WHSE_CADASTRE.PMBC_PARCEL_FABRIC_POLY_SVW</sld:Name>
    <sld:UserStyle>
      <sld:Name>parcel_fabric_ownership</sld:Name>
      <sld:Title>Parcel Fabric Ownership</sld:Title>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <sld:Name>Parcel Fabric Ownership</sld:Name>
          <sld:Title>Parcel Fabric Ownership</sld:Title>
          <sld:MaxScaleDenominator>40000.0</sld:MaxScaleDenominator>
          <sld:PolygonSymbolizer>
            <sld:Fill>
              <sld:CssParameter name="fill">#000000</sld:CssParameter>
              <sld:CssParameter name="fill-opacity">0.01</sld:CssParameter>
            </sld:Fill>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#e69800</sld:CssParameter>
              <sld:CssParameter name="stroke-opacity">0.8</sld:CssParameter>
              <sld:CssParameter name="stroke-width">1.5</sld:CssParameter>
            </sld:Stroke>
          </sld:PolygonSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</sld:StyledLayerDescriptor>`;

export function ParcelFabricOwnershipLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_CADASTRE.PMBC_PARCEL_FABRIC_POLY_SVW/ows`,
    id: "parcel-fabric-ownership",
    title: "Parcel Fabric Ownership",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_CADASTRE.PMBC_PARCEL_FABRIC_POLY_SVW",
    sld: PARCEL_FABRIC_OWNERSHIP_SLD,
    geometryAttribute: "SHAPE",
    minScale: 40000
  }
}