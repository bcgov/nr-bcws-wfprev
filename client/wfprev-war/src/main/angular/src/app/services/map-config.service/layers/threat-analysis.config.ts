import { LayerSettings } from "src/app/components/models";

export function ThreatAnalysisLayerConfig(ls: LayerSettings) {
  const items: any[] = [];
  return {
    id: "threat-analysis",
    type: "folder",
    title: "Threat Analysis",
    isVisible: false,
    isExpanded: true,
    items,
    layers: items,
  };
}
