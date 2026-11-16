import { LayerSettings } from "src/app/components/models";
import { ManagedLicenceWoodlotLayerConfig } from "./licenses-and-permits/managed-licence-woodlot.config";

export function LicensesAndPermitsLayerConfig(ls: LayerSettings) {
  const items = [
    ManagedLicenceWoodlotLayerConfig(ls),
  ];
  return {
    id: "licenses-and-permits",
    type: "folder",
    title: "Licenses and Permits",
    isVisible: false,
    isExpanded: true,
    items,
    layers: items,
  };
}

