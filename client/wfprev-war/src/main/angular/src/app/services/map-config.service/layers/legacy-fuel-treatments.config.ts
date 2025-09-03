import { LayerSettings } from "src/app/components/models";

export function LegacyFuelTreatmentsLayerConfig(ls: LayerSettings, authHeader?: Record<string, string>) {
  return {
    serviceUrl: ls.geoserverApiBaseUrl + "/ows",
    id: "legacy-fuel-treatments",
    title: "FireSmart Community Funding and Supports - Legacy Fuel Treatments",
    visible: false,
    type: "wms",
    isQueryable: false,
    combiningClass: "wfBoundary",
    layerName: "PROT_FUEL_TREATMENTS_SP",
    header: authHeader
  }

}