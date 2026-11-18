import { LayerSettings } from "src/app/components/models";

export function ParksAndRecreationTrailsLayerConfig(ls: LayerSettings) {
  const items: any[] = [];
  return {
    id: "parks-and-recreation-trails",
    type: "folder",
    title: "Parks and Recreation Trails",
    isVisible: false,
    isExpanded: true,
    items,
    layers: items,
  };
}
