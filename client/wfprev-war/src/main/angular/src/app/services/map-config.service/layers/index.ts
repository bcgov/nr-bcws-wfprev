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
import { BCParksAndProtectedAreasLayerConfig } from "./bc-parks-and-protected-areas.config";
import { ConservancyAreaLayerConfig } from "./conservancy-area.config";
import { PSTAConfig } from "./psta.config";
import { MunicipalBoundariesLayerConfig } from "./municipal-boundaries.config";
import { RecreationPolygonsLayerConfig } from "./recreation-polygons.config";
import { ManagedLicenceWoodlotLayerConfig } from "./managed-licence-woodlot.config";
import { FuelTypeLayerConfig } from "./fuel-type.config";
import { OneKMWUILayerConfig } from "./1-km-wui.config";
import { WuiRiskClassLayerConfig } from "./wui-risk-class.config";

export interface LayerSettings {
  geoserverApiBaseUrl: string;
  wfnewsApiBaseUrl: string;
  wfnewsApiKey: string;
  openmaps: string;
}
export function LayerConfig(mapServices: MapServices, token?: string) {
  const ls: LayerSettings = {
    geoserverApiBaseUrl: mapServices['geoserverApiBaseUrl'],
    wfnewsApiBaseUrl: mapServices['wfnewsApiBaseUrl'],
    wfnewsApiKey: mapServices['wfnewsApiKey'],
    openmaps: mapServices['openmaps'],
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
    LegacyFuelTreatmentsLayerConfig(ls),
    ResultsActivityTreatmentLayerConfig(ls),
    BCParksAndProtectedAreasLayerConfig(ls),
    PSTAConfig(ls),
    ConservancyAreaLayerConfig(ls),
    MunicipalBoundariesLayerConfig(ls),
    RecreationPolygonsLayerConfig(ls),
    ManagedLicenceWoodlotLayerConfig(ls),
    FuelTypeLayerConfig(ls),
    OneKMWUILayerConfig(ls),
    WuiRiskClassLayerConfig(ls)
  ];

}