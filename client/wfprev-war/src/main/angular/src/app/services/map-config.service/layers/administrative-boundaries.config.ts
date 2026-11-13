import { LayerSettings } from "src/app/components/models";
import { MinistryOfForestsRegionsLayerConfig } from "./administrative-boundaries/ministry-of-forests-regions.config";
import { MinistryOfForestsDistrictsLayerConfig } from "./administrative-boundaries/ministry-of-forests-districts.config";
import { MunicipalBoundariesLayerConfig } from "./administrative-boundaries/municipal-boundaries.config";
import { RegionalDistrictsLayerConfig } from "./administrative-boundaries/regional-districts.config";

export function AdministrativeBoundariesLayerConfig(ls: LayerSettings, authHeader?: Record<string, string>) {
  const items = [
    MinistryOfForestsRegionsLayerConfig(ls, authHeader),
    MinistryOfForestsDistrictsLayerConfig(ls, authHeader),
    MunicipalBoundariesLayerConfig(ls),
    RegionalDistrictsLayerConfig(ls),
  ];
  return {
    id: "administrative-boundaries",
    type: "folder",
    title: "Administrative Boundaries",
    isVisible: true,
    isExpanded: true,
    items,
    layers: items,
  };
}
