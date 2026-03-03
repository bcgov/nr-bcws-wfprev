package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.models.ActivityCategoryCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ActivityStatusCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.AncillaryFundingSourceCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.AttachmentContentTypeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.BCParksRegionCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.BCParksSectionCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ContractPhaseCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ForestAreaCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.FundingSourceCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.GeneralScopeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ObjectiveTypeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.PlanFiscalStatusCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectStatusCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProposalTypeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportingPeriodCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.RiskRatingCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.SilvicultureBaseCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.SilvicultureMethodCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.SilvicultureTechniqueCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.SourceObjectNameCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.WUIRiskClassRankModel;
import ca.bc.gov.nrs.wfprev.data.models.WildfireOrgUnitModel;
import ca.bc.gov.nrs.wfprev.services.CodesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CodesController.class)
@Import({SecurityConfig.class, TestcontainersConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class CodesControllerTest {

    @MockBean
    private CodesService codesService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "springSecurityAuditorAware")  // Changed to match the expected bean name
    private AuditorAware<String> auditorAware;

    @Test
    @WithMockUser
    void testGetAllCodes() throws Exception {
        testGetForestAreaCodes();
        testGetGeneralScopeCodes();
        testGetProjectTypeCodes();
        testGetProposalTypeCodes();
        testGetObjectiveTypeCodes();
        testGetActivityStatusCodes();
        testGetRiskRatingCodes();
        testGetContractPhaseCodes();
        testGetActivityCategoryCodes();
        testGetPlanFiscalStatusCodes();
        testGetAncillaryFundingSourceCodes();
        testGetFundingSourceCodes();
        testGetSilvicultureBaseCodes();
        testGetSilvicultureMethodCodes();
        testGetSilvicultureTechniqueCodes();
        testGetProjectStatusCodes();
        testGetWuiRiskClassCodes();
        testGetEvaluationCriteriaCodes();
        testGetWildfireOrgUnits();
        testGetReportingPeriodCodes();
    }

    void testGetForestAreaCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        ForestAreaCodeModel fac1 = new ForestAreaCodeModel();
        fac1.setForestAreaCode(exampleId1);

        ForestAreaCodeModel fac2 = new ForestAreaCodeModel();
        fac2.setForestAreaCode(exampleId2);

        List<ForestAreaCodeModel> facList = Arrays.asList(fac1, fac2);
        CollectionModel<ForestAreaCodeModel> facModel = CollectionModel.of(facList);

        when(codesService.getAllForestAreaCodes()).thenReturn(facModel);

        mockMvc.perform(get("/codes/" + CodeTables.FOREST_AREA_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetGeneralScopeCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        GeneralScopeCodeModel fac1 = new GeneralScopeCodeModel();
        fac1.setGeneralScopeCode(exampleId1);

        GeneralScopeCodeModel fac2 = new GeneralScopeCodeModel();
        fac2.setGeneralScopeCode(exampleId2);

        List<GeneralScopeCodeModel> facList = Arrays.asList(fac1, fac2);
        CollectionModel<GeneralScopeCodeModel> facModel = CollectionModel.of(facList);

        when(codesService.getAllGeneralScopeCodes()).thenReturn(facModel);

        mockMvc.perform(get("/codes/" + CodeTables.GENERAL_SCOPE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetProjectTypeCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        ProjectTypeCodeModel projectTypeCodeModel1 = new ProjectTypeCodeModel();
        projectTypeCodeModel1.setProjectTypeCode(exampleId1);

        ProjectTypeCodeModel projectTypeCodeModel2 = new ProjectTypeCodeModel();
        projectTypeCodeModel2.setProjectTypeCode(exampleId2);

        List<ProjectTypeCodeModel> projectTypeCodeModelList = Arrays.asList(projectTypeCodeModel1, projectTypeCodeModel2);
        CollectionModel<ProjectTypeCodeModel> projectTypeCodeModels = CollectionModel.of(projectTypeCodeModelList);

        when(codesService.getAllProjectTypeCodes()).thenReturn(projectTypeCodeModels);

        mockMvc.perform(get("/codes/" + CodeTables.PROJECT_TYPE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetProposalTypeCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        ProposalTypeCodeModel ptc1 = new ProposalTypeCodeModel();
        ptc1.setProposalTypeCode(exampleId1);

        ProposalTypeCodeModel ptc2 = new ProposalTypeCodeModel();
        ptc2.setProposalTypeCode(exampleId2);

        List<ProposalTypeCodeModel> ptcList = Arrays.asList(ptc1, ptc2);
        CollectionModel<ProposalTypeCodeModel> ptcModel = CollectionModel.of(ptcList);

        when(codesService.getAllProposalTypeCodes()).thenReturn(ptcModel);

        mockMvc.perform(get("/codes/" + CodeTables.PROPOSAL_TYPE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

    void testGetObjectiveTypeCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        ObjectiveTypeCodeModel otc1 = new ObjectiveTypeCodeModel();
        otc1.setObjectiveTypeCode(exampleId1);

        ObjectiveTypeCodeModel otc2 = new ObjectiveTypeCodeModel();
        otc1.setObjectiveTypeCode(exampleId2);

        List<ObjectiveTypeCodeModel> otcList = Arrays.asList(otc1, otc2);
        CollectionModel<ObjectiveTypeCodeModel> otcModel = CollectionModel.of(otcList);

        when(codesService.getAllObjectiveTypeCodes()).thenReturn(otcModel);

        mockMvc.perform(get("/codes/" + CodeTables.OBJECTIVE_TYPE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetActivityStatusCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        ActivityStatusCodeModel asc1 = new ActivityStatusCodeModel();
        asc1.setActivityStatusCode(exampleId1);

        ActivityStatusCodeModel asc2 = new ActivityStatusCodeModel();
        asc1.setActivityStatusCode(exampleId2);

        List<ActivityStatusCodeModel> ascList = Arrays.asList(asc1, asc2);
        CollectionModel<ActivityStatusCodeModel> ascModel = CollectionModel.of(ascList);

        when(codesService.getAllActivityStatusCodes()).thenReturn(ascModel);

        mockMvc.perform(get("/codes/" + CodeTables.ACTIVITY_STATUS_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetRiskRatingCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        RiskRatingCodeModel rrc1 = new RiskRatingCodeModel();
        rrc1.setRiskRatingCode(exampleId1);

        RiskRatingCodeModel rrc2 = new RiskRatingCodeModel();
        rrc1.setRiskRatingCode(exampleId2);

        List<RiskRatingCodeModel> rrcList = Arrays.asList(rrc1, rrc2);
        CollectionModel<RiskRatingCodeModel> rrcModel = CollectionModel.of(rrcList);

        when(codesService.getAllRiskRatingCodes()).thenReturn(rrcModel);

        mockMvc.perform(get("/codes/" + CodeTables.RISK_RATING_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetContractPhaseCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        ContractPhaseCodeModel cpc1 = new ContractPhaseCodeModel();
        cpc1.setContractPhaseCode(exampleId1);

        ContractPhaseCodeModel cpc2 = new ContractPhaseCodeModel();
        cpc1.setContractPhaseCode(exampleId2);

        List<ContractPhaseCodeModel> cpcList = Arrays.asList(cpc1, cpc2);
        CollectionModel<ContractPhaseCodeModel> cpcModel = CollectionModel.of(cpcList);

        when(codesService.getAllContractPhaseCodes()).thenReturn(cpcModel);

        mockMvc.perform(get("/codes/" + CodeTables.CONTRACT_PHASE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetActivityCategoryCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        ActivityCategoryCodeModel acc1 = new ActivityCategoryCodeModel();
        acc1.setActivityCategoryCode(exampleId1);

        ActivityCategoryCodeModel acc2 = new ActivityCategoryCodeModel();
        acc2.setActivityCategoryCode(exampleId2);

        List<ActivityCategoryCodeModel> accList = Arrays.asList(acc1, acc2);
        CollectionModel<ActivityCategoryCodeModel> accModel = CollectionModel.of(accList);

        when(codesService.getAllActivityCategoryCodes()).thenReturn(accModel);


        mockMvc.perform(get("/codes/" + CodeTables.ACTIVITY_CATEGORY_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetAncillaryFundingSourceCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        AncillaryFundingSourceCodeModel acc1 = new AncillaryFundingSourceCodeModel();
        acc1.setAncillaryFundingSourceGuid(exampleId1);

        AncillaryFundingSourceCodeModel acc2 = new AncillaryFundingSourceCodeModel();
        acc2.setAncillaryFundingSourceGuid(exampleId2);

        List<AncillaryFundingSourceCodeModel> accList = Arrays.asList(acc1, acc2);
        CollectionModel<AncillaryFundingSourceCodeModel> accModel = CollectionModel.of(accList);

        when(codesService.getAllAncillaryFundingSourceCodes()).thenReturn(accModel);

        mockMvc.perform(get("/codes/" + CodeTables.ANCILLARY_FUNDING_SOURCE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetPlanFiscalStatusCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        PlanFiscalStatusCodeModel acc1 = new PlanFiscalStatusCodeModel();
        acc1.setPlanFiscalStatusCode(exampleId1);

        PlanFiscalStatusCodeModel acc2 = new PlanFiscalStatusCodeModel();
        acc2.setPlanFiscalStatusCode(exampleId2);

        List<PlanFiscalStatusCodeModel> accList = Arrays.asList(acc1, acc2);
        CollectionModel<PlanFiscalStatusCodeModel> accModel = CollectionModel.of(accList);

        when(codesService.getAllPlanFiscalStatusCodes()).thenReturn(accModel);

        mockMvc.perform(get("/codes/" + CodeTables.PLAN_FISCAL_STATUS_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetFundingSourceCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();
        
        FundingSourceCodeModel acc1 = new FundingSourceCodeModel();
        acc1.setFundingSourceGuid(exampleId1);
        
        FundingSourceCodeModel acc2 = new FundingSourceCodeModel();
        acc2.setFundingSourceGuid(exampleId2);
        
        List<FundingSourceCodeModel> accList = Arrays.asList(acc1, acc2);
        CollectionModel<FundingSourceCodeModel> accModel = CollectionModel.of(accList);
        
        when(codesService.getAllFundingSourceCodes()).thenReturn(accModel);
        
        mockMvc.perform(get("/codes/" + CodeTables.FUNDING_SOURCE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
                }

    void testGetSilvicultureBaseCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        SilvicultureBaseCodeModel sbc1 = new SilvicultureBaseCodeModel();
        sbc1.setSilvicultureBaseGuid(exampleId1);

        SilvicultureBaseCodeModel sbc2 = new SilvicultureBaseCodeModel();
        sbc2.setSilvicultureBaseGuid(exampleId2);

        List<SilvicultureBaseCodeModel> sbcList = Arrays.asList(sbc1, sbc2);
        CollectionModel<SilvicultureBaseCodeModel> sbcModel = CollectionModel.of(sbcList);

        when(codesService.getAllSilvicultureBaseCodes()).thenReturn(sbcModel);

        mockMvc.perform(get("/codes/" + CodeTables.SILVICULTURE_BASE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetSilvicultureMethodCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        SilvicultureMethodCodeModel sbm1 = new SilvicultureMethodCodeModel();
        sbm1.setSilvicultureMethodGuid(exampleId1);

        SilvicultureMethodCodeModel sbm2 = new SilvicultureMethodCodeModel();
        sbm2.setSilvicultureMethodGuid(exampleId2);

        List<SilvicultureMethodCodeModel> sbmList = Arrays.asList(sbm1, sbm2);
        CollectionModel<SilvicultureMethodCodeModel> sbmModel = CollectionModel.of(sbmList);

        when(codesService.getAllSilvicultureMethodCodes()).thenReturn(sbmModel);

        mockMvc.perform(get("/codes/" + CodeTables.SILVICULTURE_METHOD_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetSilvicultureTechniqueCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        SilvicultureTechniqueCodeModel stc1 = new SilvicultureTechniqueCodeModel();
        stc1.setSilvicultureTechniqueGuid(exampleId1);

        SilvicultureTechniqueCodeModel stc2 = new SilvicultureTechniqueCodeModel();
        stc2.setSilvicultureTechniqueGuid(exampleId2);

        List<SilvicultureTechniqueCodeModel> stcList = Arrays.asList(stc1, stc2);
        CollectionModel<SilvicultureTechniqueCodeModel> stcModel = CollectionModel.of(stcList);

        when(codesService.getAllSilvicultureTechniqueCodes()).thenReturn(stcModel);

        mockMvc.perform(get("/codes/" + CodeTables.SILVICULTURE_TECHNIQUE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetProjectStatusCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        ProjectStatusCodeModel psc1 = new ProjectStatusCodeModel();
        psc1.setProjectStatusCode(exampleId1);

        ProjectStatusCodeModel psc2 = new ProjectStatusCodeModel();
        psc1.setProjectStatusCode(exampleId2);

        List<ProjectStatusCodeModel> ascList = Arrays.asList(psc1, psc2);
        CollectionModel<ProjectStatusCodeModel> pscModel = CollectionModel.of(ascList);

        when(codesService.getAllProjectStatusCodes()).thenReturn(pscModel);

        mockMvc.perform(get("/codes/" + CodeTables.PROJECT_STATUS_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetWuiRiskClassCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        WUIRiskClassRankModel wui1 = new WUIRiskClassRankModel();
        wui1.setWuiRiskClassCode(exampleId1);

        WUIRiskClassRankModel wui2 = new WUIRiskClassRankModel();
        wui2.setWuiRiskClassCode(exampleId2);

        List<WUIRiskClassRankModel> wuiList = Arrays.asList(wui1, wui2);
        CollectionModel<WUIRiskClassRankModel> wuiModel = CollectionModel.of(wuiList);

        when(codesService.getAllWuiRiskClassCodes()).thenReturn(wuiModel);

        mockMvc.perform(get("/codes/" + CodeTables.WUI_RISK_CLASS_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetEvaluationCriteriaCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        EvaluationCriteriaCodeModel obj1 = new EvaluationCriteriaCodeModel();
        obj1.setEvaluationCriteriaGuid(UUID.fromString(exampleId1));
        obj1.setCriteriaLabel("Objective A");

        EvaluationCriteriaCodeModel obj2 = new EvaluationCriteriaCodeModel();
        obj2.setEvaluationCriteriaGuid(UUID.fromString(exampleId2));
        obj2.setCriteriaLabel("Objective B");

        List<EvaluationCriteriaCodeModel> objList = Arrays.asList(obj1, obj2);
        CollectionModel<EvaluationCriteriaCodeModel> objModel = CollectionModel.of(objList);

        when(codesService.getAllEvaluationCriteriaCodes()).thenReturn(objModel);

        mockMvc.perform(get("/codes/" + CodeTables.EVALUATION_CRITERIA_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetWildfireOrgUnits() throws Exception {
        Integer exampleId1 = 1001;
        Integer exampleId2 = 2001;

        WildfireOrgUnitModel obj1 = new WildfireOrgUnitModel();
        obj1.setOrgUnitIdentifier(exampleId1);
        obj1.setOrgUnitName("Coastal Fire Centre");

        WildfireOrgUnitModel obj2 = new WildfireOrgUnitModel();
        obj2.setOrgUnitIdentifier(exampleId2);
        obj2.setOrgUnitName("Kamloops Fire Centre");

        List<WildfireOrgUnitModel> objList = Arrays.asList(obj1, obj2);
        CollectionModel<WildfireOrgUnitModel> objModel = CollectionModel.of(objList);

        when(codesService.getAllWildfireOrgUnits()).thenReturn(objModel);

        mockMvc.perform(get("/codes/" + CodeTables.WILDFIRE_ORG_UNIT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetReportingPeriodCodes() throws Exception {
        when(codesService.getAllReportingPeriodCodes()).thenReturn(CollectionModel.empty());
        mockMvc.perform(get("/codes/{codeTable}", CodeTables.REPORTING_PERIOD_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetCodesById() throws Exception {
        String ptID = UUID.randomUUID().toString();
        ProjectTypeCodeModel projectTypeCode = new ProjectTypeCodeModel();
        projectTypeCode.setProjectTypeCode(ptID);

        when(codesService.getProjectTypeCodeById(ptID)).thenReturn(projectTypeCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.PROJECT_TYPE_CODE, ptID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String gsID = UUID.randomUUID().toString();
        GeneralScopeCodeModel generalScopeCode = new GeneralScopeCodeModel();
        generalScopeCode.setGeneralScopeCode(gsID);

        when(codesService.getGeneralScopeCodeById(gsID)).thenReturn(generalScopeCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.GENERAL_SCOPE_CODE, gsID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String faID = UUID.randomUUID().toString();
        ForestAreaCodeModel forestAreaCode = new ForestAreaCodeModel();
        forestAreaCode.setForestAreaCode(faID);

        when(codesService.getForestAreaCodeById(faID)).thenReturn(forestAreaCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.FOREST_AREA_CODE, faID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String otID = UUID.randomUUID().toString();
        ObjectiveTypeCodeModel objectiveTypeCode = new ObjectiveTypeCodeModel();
        objectiveTypeCode.setObjectiveTypeCode(otID);

        when(codesService.getObjectiveTypeCodeById(otID)).thenReturn(objectiveTypeCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.OBJECTIVE_TYPE_CODE, otID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String asID = UUID.randomUUID().toString();
        ActivityStatusCodeModel activityStatusCode = new ActivityStatusCodeModel();
        activityStatusCode.setActivityStatusCode(asID);

        when(codesService.getActivityStatusCodeById(asID)).thenReturn(activityStatusCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.ACTIVITY_STATUS_CODE, asID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String rrID = UUID.randomUUID().toString();
        RiskRatingCodeModel riskRatingCode = new RiskRatingCodeModel();
        riskRatingCode.setRiskRatingCode(rrID);

        when(codesService.getRiskRatingCodeById(rrID)).thenReturn(riskRatingCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.RISK_RATING_CODE, rrID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String cpID = UUID.randomUUID().toString();
        ContractPhaseCodeModel contractPhaseCode = new ContractPhaseCodeModel();
        contractPhaseCode.setContractPhaseCode(cpID);

        when(codesService.getContractPhaseCodeById(cpID)).thenReturn(contractPhaseCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.CONTRACT_PHASE_CODE, cpID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String acID = UUID.randomUUID().toString();
        ActivityCategoryCodeModel activityCategoryCode = new ActivityCategoryCodeModel();
        activityCategoryCode.setActivityCategoryCode(acID);

        when(codesService.getActivityCategoryCodeById(acID)).thenReturn(activityCategoryCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.ACTIVITY_CATEGORY_CODE, acID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String afsID = UUID.randomUUID().toString();
        AncillaryFundingSourceCodeModel ancillaryFundingSourceCode = new AncillaryFundingSourceCodeModel();
        ancillaryFundingSourceCode.setAncillaryFundingSourceGuid(afsID);

        when(codesService.getAncillaryFundingSourceCodeById(afsID)).thenReturn(ancillaryFundingSourceCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.ANCILLARY_FUNDING_SOURCE_CODE, afsID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


        String pfsID = UUID.randomUUID().toString();
        PlanFiscalStatusCodeModel planFiscalStatusCode = new PlanFiscalStatusCodeModel();
        planFiscalStatusCode.setPlanFiscalStatusCode(pfsID);

        when(codesService.getPlanFiscalStatusCodeById(pfsID)).thenReturn(planFiscalStatusCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.PLAN_FISCAL_STATUS_CODE, pfsID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


        String fsID = UUID.randomUUID().toString();
        FundingSourceCodeModel fundingSourceCode = new FundingSourceCodeModel();
        fundingSourceCode.setFundingSourceGuid(afsID);

        when(codesService.getFundingSourceCodeById(fsID)).thenReturn(fundingSourceCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.FUNDING_SOURCE_CODE, fsID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String soncID = UUID.randomUUID().toString();
        SourceObjectNameCodeModel sourceObjectNameCode = new SourceObjectNameCodeModel();
        sourceObjectNameCode.setSourceObjectNameCode(soncID);

        when(codesService.getSourceObjectNameCodeById(soncID)).thenReturn(sourceObjectNameCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.SOURCE_OBJECT_NAME_CODE, soncID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String actcID = UUID.randomUUID().toString();
        AttachmentContentTypeCodeModel attachmentContentTypeCode = new AttachmentContentTypeCodeModel();
        attachmentContentTypeCode.setAttachmentContentTypeCode(actcID);

        when(codesService.getAttachmentContentTypeCodeById(actcID)).thenReturn(attachmentContentTypeCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.ATTACHMENT_CONTENT_TYPE_CODE, actcID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String sbcID = UUID.randomUUID().toString();
        SilvicultureBaseCodeModel silvicultureBaseCode = new SilvicultureBaseCodeModel();
        silvicultureBaseCode.setSilvicultureBaseCode(sbcID);

        when(codesService.getSilvicultureBaseCodeById(sbcID)).thenReturn(silvicultureBaseCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.SILVICULTURE_BASE_CODE, sbcID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String smcID = UUID.randomUUID().toString();
        SilvicultureMethodCodeModel silvicultureMethodCode = new SilvicultureMethodCodeModel();
        silvicultureMethodCode.setSilvicultureMethodCode(smcID);

        when(codesService.getSilvicultureMethodCodeById(smcID)).thenReturn(silvicultureMethodCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.SILVICULTURE_METHOD_CODE, smcID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String stcID = UUID.randomUUID().toString();
        SilvicultureTechniqueCodeModel silvicultureTechniqueCode = new SilvicultureTechniqueCodeModel();
        silvicultureTechniqueCode.setSilvicultureTechniqueCode(stcID);

        when(codesService.getSilvicultureTechniqueCodeById(stcID)).thenReturn(silvicultureTechniqueCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.SILVICULTURE_TECHNIQUE_CODE, stcID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String propID = UUID.randomUUID().toString();
        ProposalTypeCodeModel proposalTypeCode = new ProposalTypeCodeModel();
        proposalTypeCode.setProposalTypeCode(propID);
        when(codesService.getProposalTypeCodeById(propID)).thenReturn(proposalTypeCode);
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.PROPOSAL_TYPE_CODE, propID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String psID = UUID.randomUUID().toString();
        ProjectStatusCodeModel projectStatusCode = new ProjectStatusCodeModel();
        projectStatusCode.setProjectStatusCode(psID);

        when(codesService.getProjectStatusCodeById(psID)).thenReturn(projectStatusCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.PROJECT_STATUS_CODE, psID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Integer wouID = 1001;
        WildfireOrgUnitModel wildfireOrgUnit = new WildfireOrgUnitModel();
        wildfireOrgUnit.setOrgUnitIdentifier(wouID);

        when(codesService.getWildfireOrgUnitById(String.valueOf(wouID))).thenReturn(wildfireOrgUnit);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.WILDFIRE_ORG_UNIT, wouID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testCodesNotFound() throws Exception {
        String nonExistentId = "non-existent-id";

        // Mock service to return null for non-existent IDs
        when(codesService.getForestAreaCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getGeneralScopeCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getProjectTypeCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getObjectiveTypeCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getActivityStatusCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getActivityCategoryCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getPlanFiscalStatusCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getAncillaryFundingSourceCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getFundingSourceCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getSourceObjectNameCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getAttachmentContentTypeCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getSilvicultureBaseCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getSilvicultureMethodCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getSilvicultureTechniqueCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getProjectStatusCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getWildfireOrgUnitById(nonExistentId)).thenReturn(null);

        // Test valid code tables with non-existent ID
        mockMvc.perform(get("/codes/{codeTable}/{id}", "forestAreaCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "generalScopeCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "projectTypeCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "objectTypeCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "activityStatusCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "activityCategoryCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "planFiscalStatusCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "ancillaryFundingSourceCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "fundingSourceCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "sourceObjectNameCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "attachmentContentTypeCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "silvicultureBaseCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "silvicultureMethodCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "silvicultureTechniqueCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "activityStatusCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "wildfireOrgUnits", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        // Test invalid code table - should also return 404 since it hits the default case
        mockMvc.perform(get("/codes/{codeTable}/{id}", "invalidCodeTable", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetCodes_ServiceException() throws Exception {
        when(codesService.getAllForestAreaCodes()).thenThrow(new ServiceException("Service error"));

        mockMvc.perform(get("/codes/{codeTable}", CodeTables.FOREST_AREA_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetCodeById_ServiceException() throws Exception {
        String id = UUID.randomUUID().toString();
        when(codesService.getProjectTypeCodeById(id)).thenThrow(new ServiceException("Service error"));

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.PROJECT_TYPE_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetForestAreaCodes_VerifyServiceCall() throws Exception {
        // Mock the service return value
        when(codesService.getAllForestAreaCodes()).thenReturn(CollectionModel.empty());

        // Perform the request
        mockMvc.perform(get("/codes/{codeTable}", CodeTables.FOREST_AREA_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify that the correct service method is called
        verify(codesService, times(1)).getAllForestAreaCodes();
        verifyNoMoreInteractions(codesService); // Ensure no other service methods were called
    }

    @Test
    @WithMockUser
    void testGetGeneralScopeCodes_VerifyServiceCall() throws Exception {
        when(codesService.getAllGeneralScopeCodes()).thenReturn(CollectionModel.empty());

        mockMvc.perform(get("/codes/{codeTable}", CodeTables.GENERAL_SCOPE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(codesService, times(1)).getAllGeneralScopeCodes();
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetProgramAreaCodes_VerifyServiceCall() throws Exception {
        when(codesService.getAllProgramAreaCodes()).thenReturn(CollectionModel.empty());

        mockMvc.perform(get("/codes/{codeTable}", CodeTables.PROGRAM_AREA_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(codesService, times(1)).getAllProgramAreaCodes();
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetForestAreaCodeById_VerifyServiceCall() throws Exception {
        String id = UUID.randomUUID().toString();
        when(codesService.getForestAreaCodeById(id)).thenReturn(null);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.FOREST_AREA_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getForestAreaCodeById(id);
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetGeneralAreaCodeById_VerifyServiceCall() throws Exception {
        String id = UUID.randomUUID().toString();

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.GENERAL_SCOPE_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getGeneralScopeCodeById(id);
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetProjectTypeAreaCodeById_VerifyServiceCall() throws Exception {
        String id = UUID.randomUUID().toString();

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.PROJECT_TYPE_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getProjectTypeCodeById(id);
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetProposalTypeCodeById_VerifyServiceCall() throws Exception {
        String id = UUID.randomUUID().toString();

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.PROPOSAL_TYPE_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getProposalTypeCodeById(id);
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetProgramAreaCodeById_VerifyServiceCall() throws Exception {
        String id = UUID.randomUUID().toString();

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.PROGRAM_AREA_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getProgramAreaCodeById(id);
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetForestRegionCodes() throws Exception {
        when(codesService.getAllForestRegionCodes()).thenReturn(CollectionModel.empty());
        mockMvc.perform(get("/codes/{codeTable}", CodeTables.FOREST_REGION_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(codesService, times(1)).getAllForestRegionCodes();
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetForestRegionCodeById_VerifyServiceCall() throws Exception {
        Integer id = 1;
        when(codesService.getForestRegionCodeById(id)).thenReturn(null);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.FOREST_REGION_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getForestRegionCodeById(anyInt());
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetForestDistrictCodeById_VerifyServiceCall() throws Exception {
        Integer id = 1;
        when(codesService.getForestDistrictCodeById(id)).thenReturn(null);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.FOREST_DISTRICT_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getForestDistrictCodeById(anyInt());
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetForestDistrictCodes() throws Exception {
        when(codesService.getAllForestDistrictCodes()).thenReturn(CollectionModel.empty());
        mockMvc.perform(get("/codes/{codeTable}", CodeTables.FOREST_DISTRICT_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(codesService, times(1)).getAllForestDistrictCodes();
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetSourceObjectNameCodes() throws Exception {
        when(codesService.getAllSourceObjectNameCodes()).thenReturn(CollectionModel.empty());
        mockMvc.perform(get("/codes/{codeTable}", CodeTables.SOURCE_OBJECT_NAME_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(codesService, times(1)).getAllSourceObjectNameCodes();
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetAttachmentContentTypeCodes() throws Exception {
        when(codesService.getAllAttachmentContentTypeCodes()).thenReturn(CollectionModel.empty());
        mockMvc.perform(get("/codes/{codeTable}", CodeTables.ATTACHMENT_CONTENT_TYPE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(codesService, times(1)).getAllAttachmentContentTypeCodes();
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetAllSilvicultureBaseCodes() throws Exception {
        when(codesService.getAllSilvicultureBaseCodes()).thenReturn(CollectionModel.empty());
        mockMvc.perform(get("/codes/{codeTable}", CodeTables.SILVICULTURE_BASE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(codesService, times(1)).getAllSilvicultureBaseCodes();
        verifyNoMoreInteractions(codesService);
    }


    @Test
    @WithMockUser
    void testGetBCParksRegionCodes() throws Exception {
        // GIVEN
        BCParksRegionCodeModel bcparksRegionCodeModel = new BCParksRegionCodeModel();
        bcparksRegionCodeModel.setBcParksOrgUnitTypeCode("REGION");
        bcparksRegionCodeModel.setOrgUnitName("Region 1");
        bcparksRegionCodeModel.setOrgUnitId(1);
        bcparksRegionCodeModel.setEffectiveDate(new Date());
        bcparksRegionCodeModel.setExpiryDate(new Date());
        bcparksRegionCodeModel.setCharacterAlias("R1");
        bcparksRegionCodeModel.setIntegerAlias(1);

        CollectionModel<BCParksRegionCodeModel> bcparksRegionCodeModelCollection = CollectionModel.of(Arrays.asList(bcparksRegionCodeModel));
        when(codesService.getAllBCParksRegionCodes()).thenReturn(bcparksRegionCodeModelCollection);

        // WHEN
        mockMvc.perform(get("/codes/{codeTable}", CodeTables.BC_PARKS_REGION_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // THEN
                .andExpect(jsonPath("$._embedded.bcParksRegionCode[0].bcParksOrgUnitTypeCode").value("REGION"))
                .andExpect(jsonPath("$._embedded.bcParksRegionCode[0].orgUnitName").value("Region 1"))
                .andExpect(jsonPath("$._embedded.bcParksRegionCode[0].orgUnitId").value(1))
                .andExpect(jsonPath("$._embedded.bcParksRegionCode[0].characterAlias").value("R1"))
                .andExpect(jsonPath("$._embedded.bcParksRegionCode[0].integerAlias").value(1));

        verify(codesService, times(1)).getAllBCParksRegionCodes();
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetBCParksRegionCodesById_Success() throws Exception {
        // Given
        BCParksRegionCodeModel bcparksRegionCodeModel = new BCParksRegionCodeModel();
        bcparksRegionCodeModel.setBcParksOrgUnitTypeCode("REGION");
        bcparksRegionCodeModel.setOrgUnitName("Region 1");
        bcparksRegionCodeModel.setOrgUnitId(1);
        bcparksRegionCodeModel.setEffectiveDate(new Date());
        bcparksRegionCodeModel.setExpiryDate(new Date());
        bcparksRegionCodeModel.setCharacterAlias("R1");
        bcparksRegionCodeModel.setIntegerAlias(1);

        when(codesService.getBCParksRegionCodeById(anyInt())).thenReturn(bcparksRegionCodeModel);

        // When
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.BC_PARKS_REGION_CODE, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bcParksOrgUnitTypeCode").value("REGION"))
                .andExpect(jsonPath("$.orgUnitName").value("Region 1"))
                .andExpect(jsonPath("$.orgUnitId").value(1))
                .andExpect(jsonPath("$.characterAlias").value("R1"))
                .andExpect(jsonPath("$.integerAlias").value(1));
    }

    @Test
    @WithMockUser
    void testGetBCParksRegionCodesById_NotFound() throws Exception {
        // Given
        when(codesService.getBCParksRegionCodeById(anyInt())).thenReturn(null);

        // When
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.BC_PARKS_REGION_CODE, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetBCParksRegionCodesById_ServiceException() throws Exception {
        // Given
        when(codesService.getBCParksRegionCodeById(anyInt())).thenThrow(new ServiceException("Service error"));

        // When
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.BC_PARKS_REGION_CODE, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void getBCParksSectionCodes() throws Exception {
        // GIVEN
        BCParksSectionCodeModel bcparksSectionCodeModel = new BCParksSectionCodeModel();
        bcparksSectionCodeModel.setBcParksOrgUnitTypeCode("SECTION");
        bcparksSectionCodeModel.setOrgUnitName("Section 1");
        bcparksSectionCodeModel.setOrgUnitId(1);
        bcparksSectionCodeModel.setEffectiveDate(new Date());
        bcparksSectionCodeModel.setExpiryDate(new Date());
        bcparksSectionCodeModel.setCharacterAlias("S1");
        bcparksSectionCodeModel.setIntegerAlias(1);

        CollectionModel<BCParksSectionCodeModel> bcparksSectionCodeCollectionModel = CollectionModel.of(Arrays.asList(bcparksSectionCodeModel));

        when(codesService.getAllBCParksSectionCodes()).thenReturn(bcparksSectionCodeCollectionModel);

        // WHEN
        mockMvc.perform(get("/codes/{codeTable}", CodeTables.BC_PARKS_SECTION_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bcParksSectionCode[0].bcParksOrgUnitTypeCode").value("SECTION"))
                .andExpect(jsonPath("$._embedded.bcParksSectionCode[0].orgUnitName").value("Section 1"))
                .andExpect(jsonPath("$._embedded.bcParksSectionCode[0].orgUnitId").value(1))
                .andExpect(jsonPath("$._embedded.bcParksSectionCode[0].characterAlias").value("S1"))
                .andExpect(jsonPath("$._embedded.bcParksSectionCode[0].integerAlias").value(1));

        verify(codesService, times(1)).getAllBCParksSectionCodes();
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetBCParksSectionCodesById_Success() throws Exception {
        // Given
        BCParksSectionCodeModel bcparksSectionCodeModel = new BCParksSectionCodeModel();
        bcparksSectionCodeModel.setBcParksOrgUnitTypeCode("SECTION");
        bcparksSectionCodeModel.setOrgUnitName("Section 1");
        bcparksSectionCodeModel.setOrgUnitId(1);
        bcparksSectionCodeModel.setEffectiveDate(new Date());
        bcparksSectionCodeModel.setExpiryDate(new Date());
        bcparksSectionCodeModel.setCharacterAlias("S1");
        bcparksSectionCodeModel.setIntegerAlias(1);

        when(codesService.getBCParksSectionCodeById(anyInt())).thenReturn(bcparksSectionCodeModel);

        // When
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.BC_PARKS_SECTION_CODE, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bcParksOrgUnitTypeCode").value("SECTION"))
                .andExpect(jsonPath("$.orgUnitName").value("Section 1"))
                .andExpect(jsonPath("$.orgUnitId").value(1))
                .andExpect(jsonPath("$.characterAlias").value("S1"))
                .andExpect(jsonPath("$.integerAlias").value(1));
    }

    @Test
    @WithMockUser
    void testGetBCParksSectionCodesById_NotFound() throws Exception {
        // Given
        when(codesService.getBCParksSectionCodeById(anyInt())).thenReturn(null);

        // When
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.BC_PARKS_SECTION_CODE, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetBCParksSectionCodesById_ServiceException() throws Exception {
        // Given
        when(codesService.getBCParksSectionCodeById(anyInt())).thenThrow(new ServiceException("Service error"));

        // When
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.BC_PARKS_SECTION_CODE, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetReportingPeriodCodeById_VerifyServiceCall() throws Exception {
        String id = UUID.randomUUID().toString();
        when(codesService.getReportingPeriodCodeById(id)).thenReturn(null);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.REPORTING_PERIOD_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getReportingPeriodCodeById(id);
    }

}
