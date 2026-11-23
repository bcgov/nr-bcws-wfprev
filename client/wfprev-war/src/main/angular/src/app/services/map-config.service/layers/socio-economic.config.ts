import { LayerSettings } from "src/app/components/models";
import { TimberProcessingFacilitiesLayerConfig } from "./socio-economic/timber-processing-facilities.config";

export function SocioEconomicLayerConfig(ls: LayerSettings) {
  const items = [
    TimberProcessingFacilitiesLayerConfig(ls),
  ];
  return {
    id: "socio-economic",
    type: "folder",
    title: "Socio-Economic",
    isVisible: false,
    isExpanded: false,
    items,
    layers: items,
  };
}
