import { LayerSettings } from "src/app/components/models";

export function FuelTypeLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LAND_AND_NATURAL_RESOURCE.PROT_FUEL_TYPE_SP/ows`,
    id: "fuel-type",
    title: "Fuel Type",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_LAND_AND_NATURAL_RESOURCE.PROT_FUEL_TYPE_SP",
    geometryAttribute: "SHAPE",
    minScale: 50000
  }

}