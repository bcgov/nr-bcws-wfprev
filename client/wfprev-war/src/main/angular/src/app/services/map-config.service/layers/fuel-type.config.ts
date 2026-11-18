import { LayerSettings } from "src/app/components/models";

export function FuelTypeLayerConfig(ls: LayerSettings) {
  const items: any[] = [];
  return {
    id: "fuel-type",
    type: "folder",
    title: "Fuel Type",
    isVisible: false,
    isExpanded: true,
    items,
    layers: items,
  };
}
