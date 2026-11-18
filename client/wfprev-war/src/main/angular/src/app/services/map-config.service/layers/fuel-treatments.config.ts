import { LayerSettings } from "src/app/components/models";
import { LegacyFuelTreatmentsLayerConfig } from "./fuel-treatments/legacy-fuel-treatments.config";

export function FuelTreatmentsLayerConfig(ls: LayerSettings) {
  const items = [
    LegacyFuelTreatmentsLayerConfig(ls),
  ];
  return {
    id: "fuel-treatments",
    type: "folder",
    title: "Fuel Treatments",
    isVisible: false,
    isExpanded: true,
    items,
    layers: items,
  };
}
