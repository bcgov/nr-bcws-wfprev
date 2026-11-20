import { LayerSettings } from "src/app/components/models";
import { PSTAConfig } from "./threat-analysis/psta.config";

export function ThreatAnalysisLayerConfig(ls: LayerSettings) {
  const items = [
    PSTAConfig(ls),
  ];
  return {
    id: "threat-analysis",
    type: "folder",
    title: "Threat Analysis",
    isVisible: false,
    isExpanded: false,
    items,
    layers: items,
  };
}
