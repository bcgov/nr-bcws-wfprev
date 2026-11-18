import { MapServices } from "..";
import { LegacyFuelTreatmentsLayerConfig } from "./legacy-fuel-treatments.config";
import { ResultsActivityTreatmentLayerConfig } from "./risk-reduction-projects-activities.config";
import { LicensesAndPermitsLayerConfig } from "./licenses-and-permits.config";
import { AdministrativeBoundariesLayerConfig } from "./administrative-boundaries.config";
import { NaturalFeaturesLayerConfig } from "./natural-features.config";
import { BcWildfireLayersConfig } from "./bc-wildfire-layers.config";
import { LandOwnershipAndStatusLayerConfig } from "./land-ownership-and-status.config";
import { ParksAndRecreationTrailsLayerConfig } from "./parks-and-recreation-trails.config";
import { SocioEconomicLayerConfig } from "./socio-economic.config";
import { PhysicalInfrastructureLayerConfig } from "./physical-infrastructure.config";

export interface LayerSettings {
  geoserverApiBaseUrl: string;
  wfnewsApiBaseUrl: string;
  wfnewsApiKey: string;
  openmaps: string;
  wildfiremaps: string;
}
export function LayerConfig(mapServices: MapServices, token?: string) {
  const ls: LayerSettings = {
    geoserverApiBaseUrl: mapServices['geoserverApiBaseUrl'],
    wfnewsApiBaseUrl: mapServices['wfnewsApiBaseUrl'],
    wfnewsApiKey: mapServices['wfnewsApiKey'],
    openmaps: mapServices['openmaps'],
    wildfiremaps: mapServices['wildfiremaps'],
  };

  const authHeader: Record<string, string> = {};
  if (token) {
    authHeader['Authorization'] = `Bearer ${token}`;
  }

  return [
    AdministrativeBoundariesLayerConfig(ls, authHeader),
    NaturalFeaturesLayerConfig(ls),
    BcWildfireLayersConfig(ls, authHeader),
    LandOwnershipAndStatusLayerConfig(ls),
    LicensesAndPermitsLayerConfig(ls),
    ParksAndRecreationTrailsLayerConfig(ls),
    SocioEconomicLayerConfig(ls),
    PhysicalInfrastructureLayerConfig(ls),
    LegacyFuelTreatmentsLayerConfig(ls),
    ResultsActivityTreatmentLayerConfig(ls)
  ];

}