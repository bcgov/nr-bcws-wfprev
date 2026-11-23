import { LayerSettings } from "src/app/components/models";
import { BcControlledRecreationAreasLayerConfig } from "./parks-and-recreation-trails/bc-controlled-recreation-areas.config";
import { RecreationPolygonsLayerConfig } from "./parks-and-recreation-trails/recreation-polygons.config";
import { BCParksAndProtectedAreasLayerConfig } from "./parks-and-recreation-trails/bc-parks-and-protected-areas.config";

export function ParksAndRecreationTrailsLayerConfig(ls: LayerSettings) {
  const items = [
    BcControlledRecreationAreasLayerConfig(ls),
    RecreationPolygonsLayerConfig(ls),
    BCParksAndProtectedAreasLayerConfig(ls),
  ];
  return {
    id: "parks-and-recreation-trails",
    type: "folder",
    title: "Parks and Recreation Trails",
    isVisible: false,
    isExpanded: false,
    items,
    layers: items,
  };
}
