package ca.bc.gov.nrs.wfprev.data.params;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class FeatureQueryParams {
    private List<UUID> programAreaGuids;
    private List<String> fiscalYears;
    private List<String> forestRegionOrgUnitIds;
    private List<String> forestDistrictOrgUnitIds;
    private List<String> fireCentreOrgUnitIds;
    private List<String> activityCategoryCodes;
    private List<String> planFiscalStatusCodes;
    private String searchText;
}
