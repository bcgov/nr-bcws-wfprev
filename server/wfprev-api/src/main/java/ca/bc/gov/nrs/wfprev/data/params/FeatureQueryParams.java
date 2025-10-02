package ca.bc.gov.nrs.wfprev.data.params;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class FeatureQueryParams {
    private UUID projectGuid;
    private List<UUID> programAreaGuids;
    private List<String> fiscalYears;
    private List<String> forestRegionOrgUnitIds;
    private List<String> forestDistrictOrgUnitIds;
    private List<String> fireCentreOrgUnitIds;
    private List<String> activityCategoryCodes;
    private List<String> planFiscalStatusCodes;
    private List<String> projectTypeCodes;
    private String searchText;
}
