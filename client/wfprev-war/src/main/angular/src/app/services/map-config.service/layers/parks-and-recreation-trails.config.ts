import { LayerSettings } from "src/app/components/models";
import { BcControlledRecreationAreasLayerConfig } from "./parks-and-recreation-trails/bc-controlled-recreation-areas.config";

export function ParksAndRecreationTrailsLayerConfig(ls: LayerSettings) {
  const items = [
    BcControlledRecreationAreasLayerConfig(ls),
  ];
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
