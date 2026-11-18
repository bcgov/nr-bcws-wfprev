import { LayerSettings } from "src/app/components/models";

export function FuelTreatmentsLayerConfig(ls: LayerSettings) {
  const items: any[] = [];
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
