import { LayerSettings } from "src/app/components/models";

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
    geometryAttribute: "GEOMETRY"
  }

}