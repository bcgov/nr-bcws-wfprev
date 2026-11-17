import { LayerSettings } from "src/app/components/models";
import { ForestTenureManagedLicenceLayerConfig } from "./licenses-and-permits/forest-tenure-managed-licence.config";

export function LicensesAndPermitsLayerConfig(ls: LayerSettings) {
  const items = [
    ForestTenureManagedLicenceLayerConfig(ls),
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
