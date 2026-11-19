import { LayerSettings } from "src/app/components/models";
import { LegacyFuelTreatmentsLayerConfig } from "./fuel-treatments/legacy-fuel-treatments.config";
import { ResultsActivityTreatmentLayerConfig } from "./fuel-treatments/risk-reduction-projects-activities.config";

export function FuelTreatmentsLayerConfig(ls: LayerSettings) {
  const items = [
    LegacyFuelTreatmentsLayerConfig(ls),
    ResultsActivityTreatmentLayerConfig(ls),
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
