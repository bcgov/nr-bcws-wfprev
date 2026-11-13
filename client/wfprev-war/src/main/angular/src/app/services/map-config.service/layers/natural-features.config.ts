import { LayerSettings } from "src/app/components/models";

export function NaturalFeaturesLayerConfig(ls: LayerSettings) {
  const items: any[] = [];
  return {
    id: "natural-features",
    type: "folder",
    title: "Natural Features",
    isVisible: false,
    isExpanded: true,
    items,
    layers: items,
  };
}

