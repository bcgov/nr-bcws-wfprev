import { LayerSettings } from "src/app/components/models";
import { ParcelFabricOwnershipLayerConfig } from "./land-ownership-and-status/parcel-fabric-ownership.config";
import { ParcelFabricLayerConfig } from "./land-ownership-and-status/parcel-fabric.config";
import { ConservancyAreaLayerConfig } from "./land-ownership-and-status/conservancy-area.config";

export function LandOwnershipAndStatusLayerConfig(ls: LayerSettings) {
  const items = [
    ParcelFabricOwnershipLayerConfig(ls),
    ParcelFabricLayerConfig(ls),
    ConservancyAreaLayerConfig(ls),
  ];
  return {
    id: "land-ownership-and-status",
    type: "folder",
    title: "Land Ownership and Status",
    isVisible: false,
    isExpanded: false,
    items,
    layers: items,
  };
}
