import { LayerSettings } from "src/app/components/models";

export function PhysicalInfrastructureLayerConfig(ls: LayerSettings) {
  const items: any[] = [];
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
