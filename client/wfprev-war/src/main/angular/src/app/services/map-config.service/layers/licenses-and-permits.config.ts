import { LayerSettings } from "src/app/components/models";
import { ForestTenureManagedLicenceLayerConfig } from "./licenses-and-permits/forest-tenure-managed-licence.config";
import { BCTimberSalesOperatingAreasLayerConfig } from "./licenses-and-permits/bc-timber-sales-operating-areas.config";

export function LicensesAndPermitsLayerConfig(ls: LayerSettings) {
  const items = [
    ForestTenureManagedLicenceLayerConfig(ls),
    BCTimberSalesOperatingAreasLayerConfig(ls),
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
