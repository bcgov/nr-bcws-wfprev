import { OutOfControlWildfiresLayerConfig } from "./wildfires/out-of-control-wildfires.config";
import { FirePerimetersLayerConfig } from "./fire-perimeters.config";
import { MinistryOfForestsDistrictsLayerConfig } from "./ministry-of-forests-districts.config";
import { MinistryOfForestsRegionsLayerConfig } from "./ministry-of-forests-regions.config";
import { WildfireOrgUnitFireCentreLayerConfig } from "./wildfire-org-unit-fire-centre";
import { BeingHeldWildfiresLayerConfig } from "./wildfires/being-held-wildfires.config";
import { UnderControlWildfiresLayerConfig } from "./wildfires/under-control-wildfires.config";
import { OutWildfiresLayerConfig } from "./wildfires/out-wildfires.config";
import { MapServices } from "..";
import { LegacyFuelTreatmentsLayerConfig } from "./legacy-fuel-treatments.config";
import { ResultsActivityTreatmentLayerConfig } from "./risk-reduction-projects-activities.config";

export interface LayerSettings {
  geoserverApiBaseUrl: string;
  wfnewsApiBaseUrl: string;
  wfnewsApiKey: string;
}
export function LayerConfig(mapServices: MapServices, token?: string) {
  const ls: LayerSettings = {
    geoserverApiBaseUrl: mapServices['geoserverApiBaseUrl'],
    wfnewsApiBaseUrl: mapServices['wfnewsApiBaseUrl'],
    wfnewsApiKey: mapServices['wfnewsApiKey']
  };

  const authHeader: Record<string, string> = {};
  if (token) {
    authHeader['Authorization'] = `Bearer ${token}`;
  }

  return [
    MinistryOfForestsRegionsLayerConfig(ls, authHeader),
    MinistryOfForestsDistrictsLayerConfig(ls, authHeader), 
    WildfireOrgUnitFireCentreLayerConfig(ls, authHeader),
    FirePerimetersLayerConfig(ls, authHeader),
    OutOfControlWildfiresLayerConfig(ls),
    BeingHeldWildfiresLayerConfig(ls),
    UnderControlWildfiresLayerConfig(ls),
    OutWildfiresLayerConfig(ls),
    LegacyFuelTreatmentsLayerConfig(ls, authHeader),
    ResultsActivityTreatmentLayerConfig(ls)
  ];
  
}