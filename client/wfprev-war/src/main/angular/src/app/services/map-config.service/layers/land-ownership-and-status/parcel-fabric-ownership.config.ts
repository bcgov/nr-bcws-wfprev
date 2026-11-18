import { LayerSettings } from "src/app/components/models";

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
    geometryAttribute: "SHAPE",
    minScale: 40000
  }
}