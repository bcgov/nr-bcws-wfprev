import { MapServices } from "..";
import { LegacyFuelTreatmentsLayerConfig } from "./legacy-fuel-treatments.config";
import { ResultsActivityTreatmentLayerConfig } from "./risk-reduction-projects-activities.config";
import { BCParksAndProtectedAreasLayerConfig } from "./bc-parks-and-protected-areas.config";
import { ConservancyAreaLayerConfig } from "./conservancy-area.config";
import { RecreationPolygonsLayerConfig } from "./recreation-polygons.config";
import { LicensesAndPermitsLayerConfig } from "./licenses-and-permits.config";
import { ParcelFabricLayerConfig } from "./parcel-fabric.config";
import { OneKMWUILayerConfig } from "./1-km-wui.config";
import { WuiRiskClassLayerConfig } from "./wui-risk-class.config";
import { MountainResortsBranchLayerConfig } from "./mountain-resorts-branch.config";
import { AdministrativeBoundariesLayerConfig } from "./administrative-boundaries.config";
import { NaturalFeaturesLayerConfig } from "./natural-features.config";
import { BcWildfireLayersConfig } from "./bc-wildfire-layers.config";

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
    LicensesAndPermitsLayerConfig(ls),
    LegacyFuelTreatmentsLayerConfig(ls),
    ResultsActivityTreatmentLayerConfig(ls),
    BCParksAndProtectedAreasLayerConfig(ls),
    ConservancyAreaLayerConfig(ls),
    RecreationPolygonsLayerConfig(ls),
    ParcelFabricLayerConfig(ls),
    OneKMWUILayerConfig(ls),
    WuiRiskClassLayerConfig(ls),
    MountainResortsBranchLayerConfig(ls)
  ];

}