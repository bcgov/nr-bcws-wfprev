import { LayerSettings } from "src/app/components/models";

export function ResultsActivityTreatmentLayerConfig(
  ls: LayerSettings,
  authHeader?: Record<string, string>
) {
  const where =
    "GEOMETRY_EXIST_IND = 'Y' AND RESULTS_IND = 'Y' AND " +
    "(SILV_FUND_SOURCE_CODE IN ('CF','FEP','WRR') " +
    "OR (SILV_FUND_SOURCE_CODE = 'FES' AND FIA_PROJECT_ID LIKE 'WR%')) " +
    "AND SILV_BASE_CODE NOT IN('LB','SU') " +
    "AND (SILV_OBJECTIVE_CODE_1 IN ('HAZ','FRE') " +
    "OR SILV_OBJECTIVE_CODE_2 IN ('HAZ','FRE') " +
    "OR SILV_OBJECTIVE_CODE_3 IN ('HAZ','FRE'))";

  return {
    serviceUrl: `${ls.openmaps}/geo/pub/WHSE_FOREST_VEGETATION.RSLT_ACTIVITY_TREATMENT_SVW/ows`,
    id: "risk-reduction-projects-activities",
    title: "RESULTS - Wildfire Risk Reduction Project Activities",
    type: "wms",
    visible: true,
    isQueryable: true,
    layerName: "pub:WHSE_FOREST_VEGETATION.RSLT_ACTIVITY_TREATMENT_SVW",
    version: "1.1.1",
    where,
    transparent: true,
    header: authHeader
  };
}
