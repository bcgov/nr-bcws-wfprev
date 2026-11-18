import { LayerSettings } from "src/app/components/models";
import { ParcelFabricOwnershipLayerConfig } from "./land-ownership-and-status/parcel-fabric-ownership.config";

export function LandOwnershipAndStatusLayerConfig(ls: LayerSettings) {
  const items = [
    ParcelFabricOwnershipLayerConfig(ls),
  ];
  return {
    id: "land-ownership-and-status",
    type: "folder",
    title: "Land Ownership and Status",
    isVisible: false,
    isExpanded: true,
    items,
    layers: items,
  };
}
