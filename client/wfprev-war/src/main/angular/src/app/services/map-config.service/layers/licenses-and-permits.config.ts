import { LayerSettings } from "src/app/components/models";
import { RangeTenurePolygonsLayerConfig } from "./licenses-and-permits/range-tenure-polygons.config";
import { ForestTenureManagedLicenceLayerConfig } from "./licenses-and-permits/forest-tenure-managed-licence.config";
import { BCTimberSalesOperatingAreasLayerConfig } from "./licenses-and-permits/bc-timber-sales-operating-areas.config";
import { TreeFarmLicenseLayerConfig } from "./licenses-and-permits/tree-farm-license.config";

export function LicensesAndPermitsLayerConfig(ls: LayerSettings) {
  const items = [
    RangeTenurePolygonsLayerConfig(ls),
    ForestTenureManagedLicenceLayerConfig(ls),
    BCTimberSalesOperatingAreasLayerConfig(ls),
    TreeFarmLicenseLayerConfig(ls),
  ];
  return {
    id: "licenses-and-permits",
    type: "folder",
    title: "Licenses and Permits",
    isVisible: false,
    isExpanded: false,
    items,
    layers: items,
  };
}
