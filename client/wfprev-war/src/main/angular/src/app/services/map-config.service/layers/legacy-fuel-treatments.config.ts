import { LayerSettings } from "src/app/components/models";

export function LegacyFuelTreatmentsLayerConfig(ls: LayerSettings) {
  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_LAND_AND_NATURAL_RESOURCE.PROT_FUEL_TREATMENTS_SP/ows`,
    id: "legacy-fuel-treatments",
    title: "FireSmart Community Funding and Supports - Legacy Fuel Treatments",
    visible: true,
    type: "wms",
    isQueryable: true,
    version: "1.1.1",
    transparent: true,
    layerName: "pub:WHSE_LAND_AND_NATURAL_RESOURCE.PROT_FUEL_TREATMENTS_SP",
    geometryAttribute: "SHAPE"
  }

}