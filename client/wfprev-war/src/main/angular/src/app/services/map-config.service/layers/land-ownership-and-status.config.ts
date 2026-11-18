import { LayerSettings } from "src/app/components/models";

export function LandOwnershipAndStatusLayerConfig(ls: LayerSettings) {
  const items: any[] = [];
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
