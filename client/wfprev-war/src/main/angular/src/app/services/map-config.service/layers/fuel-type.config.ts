import { LayerSettings } from "src/app/components/models";
import { FuelTypeLayerConfig as FuelTypeItemLayerConfig } from "./fuel-type/fuel-type.config";

export function FuelTypeLayerConfig(ls: LayerSettings) {
  const items = [
    FuelTypeItemLayerConfig(ls),
  ];
  return {
    id: "fuel-types",
    type: "folder",
    title: "Fuel Type",
    isVisible: false,
    isExpanded: false,
    items,
    layers: items,
  };
}
