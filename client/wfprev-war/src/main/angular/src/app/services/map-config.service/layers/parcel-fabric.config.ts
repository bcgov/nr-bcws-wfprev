import { LayerSettings } from "src/app/components/models";

export function ParcelFabricLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_CADASTRE.PMBC_PARCEL_FABRIC_POLY_SVW/ows`,
    id: "parcel-fabric",
    title: "Parcel Fabric - Ownership and Parcel Fabric - Private Land",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_CADASTRE.PMBC_PARCEL_FABRIC_POLY_SVW",
    geometryAttribute: "SHAPE"
  }

}