import { LayerSettings } from "src/app/components/models";
import { RegionalDistrictsLayerConfig } from "./administrative-boundaries/regional-districts.config";

export function AdministrativeBoundariesLayerConfig(ls: LayerSettings) {
  const items = [
    RegionalDistrictsLayerConfig(ls),
  ];
  return {
    id: "administrative-boundaries",
    type: "folder",
    title: "Administrative Boundaries",
    isVisible: false,
    isExpanded: true,
    items,
    layers: items,
  };
}

