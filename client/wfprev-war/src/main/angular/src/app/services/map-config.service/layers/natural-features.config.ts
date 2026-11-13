import { LayerSettings } from "src/app/components/models";
import { FwaStreamsLayerConfig } from "./natural-features/fwa-streams.config";

export function NaturalFeaturesLayerConfig(ls: LayerSettings) {
  const items = [
    FwaStreamsLayerConfig(ls),
  ];
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

