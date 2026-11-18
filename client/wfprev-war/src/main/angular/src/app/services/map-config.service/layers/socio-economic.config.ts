import { LayerSettings } from "src/app/components/models";

export function SocioEconomicLayerConfig(ls: LayerSettings) {
  const items: any[] = [];
  return {
    id: "socio-economic",
    type: "folder",
    title: "Socio-Economic",
    isVisible: false,
    isExpanded: true,
    items,
    layers: items,
  };
}
