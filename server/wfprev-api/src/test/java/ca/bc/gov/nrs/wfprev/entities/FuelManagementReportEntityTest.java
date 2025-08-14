package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.FuelManagementReportEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FuelManagementReportEntityTest {

    @Test
    void testAllFields() {
        UUID guid1 = UUID.randomUUID();
        UUID guid2 = UUID.randomUUID();
        BigDecimal decimal = new BigDecimal("123.45");
        Date now = new Date();

        FuelManagementReportEntity entity = new FuelManagementReportEntity();

        entity.setProjectPlanFiscalGuid(guid1);
        entity.setLinkToProject("http://example.com/project");
        entity.setLinkToFiscalActivity("http://example.com/fiscal");
        entity.setProjectGuid(guid2);
        entity.setProjectTypeDescription("Type Desc");
        entity.setProjectName("Project Name");
        entity.setForestRegionOrgUnitName("Region");
        entity.setForestDistrictOrgUnitName("District");
        entity.setBcParksRegionOrgUnitName("BC Parks Region");
        entity.setBcParksSectionOrgUnitName("BC Parks Section");
        entity.setFireCentreOrgUnitName("Fire Centre");
        entity.setBusinessArea("Business Area");
        entity.setProgramAreaGuid(guid1);
        entity.setPlanningUnitName("Planning Unit");
        entity.setGrossProjectAreaHa(decimal);
        entity.setClosestCommunityName("Community");
        entity.setProjectLead("Project Lead");
        entity.setProposalTypeDescription("Proposal Type");
        entity.setProjectFiscalName("Fiscal Name");
        entity.setProjectFiscalDescription("Fiscal Desc");
        entity.setFiscalYear("2025");
        entity.setActivityCategoryDescription("Activity Category");
        entity.setPlanFiscalStatusDescription("Status Desc");
        entity.setFundingStream("Funding");
        entity.setTotalEstimatedCostAmount(decimal);
        entity.setFiscalAncillaryFundAmount(decimal);
        entity.setFiscalReportedSpendAmount(decimal);
        entity.setFiscalActualAmount(decimal);
        entity.setFiscalPlannedProjectSizeHa(decimal);
        entity.setFiscalCompletedSizeHa(decimal);
        entity.setSpatialSubmitted("Y");
        entity.setFirstNationsEngagement("Y");
        entity.setFirstNationsDelivPartners("Partner A");
        entity.setFirstNationsPartner("Partner B");
        entity.setOtherPartner("Other Partner");
        entity.setCfsProjectCode("CFS123");
        entity.setResultsProjectCode("RESULTS123");
        entity.setResultsOpeningId("OPENING123");
        entity.setPrimaryObjectiveTypeDescription("Primary Obj");
        entity.setSecondaryObjectiveTypeDescription("Secondary Obj");
        entity.setEndorsementTimestamp(now);
        entity.setApprovedTimestamp(now);
        entity.setWuiRiskClassDescription("High");
        entity.setLocalWuiRiskClassDescription("Very High");
        entity.setLocalWuiRiskClassRationale("Rationale");
        entity.setTotalCoarseFilterSectionScore(decimal);
        entity.setTotalMediumFilterSectionScore(decimal);
        entity.setMediumFilterSectionComment("Medium comment");
        entity.setTotalFineFilterSectionScore(decimal);
        entity.setFineFilterSectionComment("Fine comment");
        entity.setTotalFilterSectionScore(decimal);

        assertThat(entity.getProjectPlanFiscalGuid()).isEqualTo(guid1);
        assertThat(entity.getLinkToProject()).isEqualTo("http://example.com/project");
        assertThat(entity.getLinkToFiscalActivity()).isEqualTo("http://example.com/fiscal");
        assertThat(entity.getProjectGuid()).isEqualTo(guid2);
        assertThat(entity.getProjectTypeDescription()).isEqualTo("Type Desc");
        assertThat(entity.getProjectName()).isEqualTo("Project Name");
        assertThat(entity.getForestRegionOrgUnitName()).isEqualTo("Region");
        assertThat(entity.getForestDistrictOrgUnitName()).isEqualTo("District");
        assertThat(entity.getBcParksRegionOrgUnitName()).isEqualTo("BC Parks Region");
        assertThat(entity.getBcParksSectionOrgUnitName()).isEqualTo("BC Parks Section");
        assertThat(entity.getFireCentreOrgUnitName()).isEqualTo("Fire Centre");
        assertThat(entity.getBusinessArea()).isEqualTo("Business Area");
        assertThat(entity.getProgramAreaGuid()).isEqualTo(guid1);
        assertThat(entity.getPlanningUnitName()).isEqualTo("Planning Unit");
        assertThat(entity.getGrossProjectAreaHa()).isEqualTo(decimal);
        assertThat(entity.getClosestCommunityName()).isEqualTo("Community");
        assertThat(entity.getProjectLead()).isEqualTo("Project Lead");
        assertThat(entity.getProposalTypeDescription()).isEqualTo("Proposal Type");
        assertThat(entity.getProjectFiscalName()).isEqualTo("Fiscal Name");
        assertThat(entity.getProjectFiscalDescription()).isEqualTo("Fiscal Desc");
        assertThat(entity.getFiscalYear()).isEqualTo("2025");
        assertThat(entity.getActivityCategoryDescription()).isEqualTo("Activity Category");
        assertThat(entity.getPlanFiscalStatusDescription()).isEqualTo("Status Desc");
        assertThat(entity.getFundingStream()).isEqualTo("Funding");
        assertThat(entity.getTotalEstimatedCostAmount()).isEqualTo(decimal);
        assertThat(entity.getFiscalAncillaryFundAmount()).isEqualTo(decimal);
        assertThat(entity.getFiscalReportedSpendAmount()).isEqualTo(decimal);
        assertThat(entity.getFiscalActualAmount()).isEqualTo(decimal);
        assertThat(entity.getFiscalPlannedProjectSizeHa()).isEqualTo(decimal);
        assertThat(entity.getFiscalCompletedSizeHa()).isEqualTo(decimal);
        assertThat(entity.getSpatialSubmitted()).isEqualTo("Y");
        assertThat(entity.getFirstNationsEngagement()).isEqualTo("Y");
        assertThat(entity.getFirstNationsDelivPartners()).isEqualTo("Partner A");
        assertThat(entity.getFirstNationsPartner()).isEqualTo("Partner B");
        assertThat(entity.getOtherPartner()).isEqualTo("Other Partner");
        assertThat(entity.getCfsProjectCode()).isEqualTo("CFS123");
        assertThat(entity.getResultsProjectCode()).isEqualTo("RESULTS123");
        assertThat(entity.getResultsOpeningId()).isEqualTo("OPENING123");
        assertThat(entity.getPrimaryObjectiveTypeDescription()).isEqualTo("Primary Obj");
        assertThat(entity.getSecondaryObjectiveTypeDescription()).isEqualTo("Secondary Obj");
        assertThat(entity.getEndorsementTimestamp()).isEqualTo(now);
        assertThat(entity.getApprovedTimestamp()).isEqualTo(now);
        assertThat(entity.getWuiRiskClassDescription()).isEqualTo("High");
        assertThat(entity.getLocalWuiRiskClassDescription()).isEqualTo("Very High");
        assertThat(entity.getLocalWuiRiskClassRationale()).isEqualTo("Rationale");
        assertThat(entity.getTotalCoarseFilterSectionScore()).isEqualTo(decimal);
        assertThat(entity.getTotalMediumFilterSectionScore()).isEqualTo(decimal);
        assertThat(entity.getMediumFilterSectionComment()).isEqualTo("Medium comment");
        assertThat(entity.getTotalFineFilterSectionScore()).isEqualTo(decimal);
        assertThat(entity.getFineFilterSectionComment()).isEqualTo("Fine comment");
        assertThat(entity.getTotalFilterSectionScore()).isEqualTo(decimal);
    }
}
