import { LayerSettings } from "src/app/components/models";

export function ResultsActivityTreatmentLayerConfig(ls: LayerSettings, authHeader?: Record<string, string>) {
  // Dataset-scoped WMS endpoint recommended by DataBC
  const serviceUrl =
    "https://openmaps.gov.bc.ca/geo/pub/WHSE_FOREST_VEGETATION.RSLT_ACTIVITY_TREATMENT_SVW/ows";

  // Same filter you provided (CQL_FILTER for GeoServer)
  const cql =
    "GEOMETRY_EXIST_IND = 'Y' AND RESULTS_IND = 'Y' AND (SILV_FUND_SOURCE_CODE IN ('CF','FEP','WRR') OR (SILV_FUND_SOURCE_CODE = 'FES' AND FIA_PROJECT_ID LIKE 'WR%')) AND SILV_BASE_CODE NOT IN('LB','SU') AND (SILV_OBJECTIVE_CODE_1 IN ('HAZ','FRE') OR SILV_OBJECTIVE_CODE_2 IN ('HAZ','FRE') OR SILV_OBJECTIVE_CODE_3 IN ('HAZ','FRE'))";

  return {
    serviceUrl,                              // WMS 1.1.1 is fine on this service
    id: "risk-reduction-projects-activities",
    title: "RESULTS – Activity Treatment Units (filtered)",
    visible: true,
    type: "wms",
    isQueryable: true,
    combiningClass: "wfBoundary",
    // Use the fully qualified layer name as published on the DataBC GeoServer
    layerName: "pub:WHSE_FOREST_VEGETATION.RSLT_ACTIVITY_TREATMENT_SVW",
    version: "1.1.1",
    format: "image/png",
    transparent: true,
    // Pass the CQL filter as a vendor parameter (your SMK/leaflet adapter typically forwards `params`)
    params: {
      CQL_FILTER: cql
    },
    header: authHeader
  };
}
