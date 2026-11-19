import { LayerSettings } from "src/app/components/models";
import { TransmissionLinesLayerConfig } from "./physical-infrastructure/transmission-lines.config";

export function PhysicalInfrastructureLayerConfig(ls: LayerSettings) {
  const items = [
    TransmissionLinesLayerConfig(ls),
  ];
  return {
    id: "physical-infrastructure",
    type: "folder",
    title: "Physical Infrastructure",
    isVisible: false,
    isExpanded: true,
    items,
    layers: items,
  };
}
