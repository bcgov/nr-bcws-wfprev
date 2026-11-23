import { LayerSettings } from "src/app/components/models";
import { RangeTenurePolygonsLayerConfig } from "./licences-and-permits/range-tenure-polygons.config";
import { ForestTenureManagedLicenceLayerConfig } from "./licences-and-permits/forest-tenure-managed-licence.config";
import { BCTimberSalesOperatingAreasLayerConfig } from "./licences-and-permits/bc-timber-sales-operating-areas.config";
import { TreeFarmLicenceLayerConfig } from "./licences-and-permits/tree-farm-licence.config";

export function LicencesAndPermitsLayerConfig(ls: LayerSettings) {
  const items = [
    RangeTenurePolygonsLayerConfig(ls),
    ForestTenureManagedLicenceLayerConfig(ls),
    BCTimberSalesOperatingAreasLayerConfig(ls),
    TreeFarmLicenceLayerConfig(ls),
  ];
  return {
    id: "licences-and-permits",
    type: "folder",
    title: "Licences and Permits",
    isVisible: false,
    isExpanded: false,
    items,
    layers: items,
  };
}
