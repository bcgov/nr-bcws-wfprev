import { LayerSettings } from "src/app/components/models";
import { SpottingImpactLayerConfig } from "./psta/spotting-impact.config";
import { HeadfireIntensityLayerConfig } from "./psta/headfire-intensity.config";
import { FireThreatRatingLayerConfig } from "./psta/fire-threat-rating.config";
import { FireStartDensityAllLayerConfig } from "./psta/fire-start-density-all.config";

export function PSTAConfig(ls: LayerSettings) {
  const items = [
    SpottingImpactLayerConfig(ls),
    HeadfireIntensityLayerConfig(ls),
    FireThreatRatingLayerConfig(ls),
    FireStartDensityAllLayerConfig(ls),
  ];
  return {
    id: "psta",
    type: "folder",
    title: "PSTA",
    isVisible: true,
    isExpanded: true,
    items,
    layers: items,
  };
}