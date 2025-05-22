package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectFiscalResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectFiscalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectFiscalServiceTest {

    private ProjectFiscalRepository projectFiscalRepository;
    private ProjectFiscalService projectFiscalService;
    private ProjectFiscalResourceAssembler projectFiscalResourceAssembler;
    private ProjectService projectService;
    private ProjectResourceAssembler projectResourceAssembler;
    private ProjectEntity projectEntity;

    @BeforeEach
    void setup() {
        projectFiscalRepository = mock(ProjectFiscalRepository.class);
        projectFiscalResourceAssembler = mock(ProjectFiscalResourceAssembler.class);
        projectService = mock(ProjectService.class);
        projectResourceAssembler = mock(ProjectResourceAssembler.class);
        projectEntity = mock(ProjectEntity.class);
        projectFiscalService = new ProjectFiscalService(projectFiscalRepository, projectFiscalResourceAssembler, projectService, projectResourceAssembler);
    }
    @Test
    void testGetAllProjectFiscals_Empty() {
        // GIVEN I have no project fiscals
        UUID projectGuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

        // Update the mock to use the new repository method
        when(projectFiscalRepository.findAllByProject_ProjectGuid(projectGuid))
                .thenReturn(Collections.emptyList());
        when(projectFiscalResourceAssembler.toCollectionModel(Collections.emptyList()))
                .thenReturn(CollectionModel.of(Collections.emptyList()));

        // WHEN I get all project fiscals
        CollectionModel<ProjectFiscalModel> allProjectFiscals =
                projectFiscalService.getAllProjectFiscals(projectGuid.toString());

        // THEN I should get an empty list
        assertEquals(0, allProjectFiscals.getContent().size());
    }

    @Test
    void testGetAllProjectFiscals_NotEmpty() {
        // GIVEN I have some project fiscals
        ProjectFiscalModel projectFiscalModel1 = new ProjectFiscalModel();
        projectFiscalModel1.setProjectPlanFiscalGuid("456e7890-e89b-12d3-a456-426614174001");
        projectFiscalModel1.setProjectGuid("123e4567-e89b-12d3-a456-426614174000");
        projectFiscalModel1.setActivityCategoryCode("ACTIVITY_CODE_1");
        projectFiscalModel1.setFiscalYear(2021L);
        projectFiscalModel1.setAncillaryFundingSourceGuid("789e1234-e89b-12d3-a456-426614174002");
        projectFiscalModel1.setProjectPlanStatusCode("PLAN_STATUS_1");
        projectFiscalModel1.setPlanFiscalStatusCode("FISCAL_STATUS_1");
        projectFiscalModel1.setProposalTypeCode("NEW");
        projectFiscalModel1.setEndorsementCode("ENDORSEMENT_CODE_1");
        projectFiscalModel1.setProjectFiscalName("Test Project Fiscal 1");
        projectFiscalModel1.setProjectFiscalDescription("Description of Test Project Fiscal 1");
        projectFiscalModel1.setBusinessAreaComment("Business area comment example 1");
        projectFiscalModel1.setEstimatedClwrrAllocAmount(BigDecimal.valueOf(10000));
        projectFiscalModel1.setTotalCostEstimateAmount(BigDecimal.valueOf(50000));
        projectFiscalModel1.setCfsProjectCode("CFS-123");
        projectFiscalModel1.setFiscalForecastAmount(BigDecimal.valueOf(15000));
        projectFiscalModel1.setFiscalAncillaryFundAmount(BigDecimal.valueOf(5000));
        projectFiscalModel1.setFiscalPlannedProjectSizeHa(BigDecimal.valueOf(50));
        projectFiscalModel1.setFiscalPlannedCostPerHaAmt(BigDecimal.valueOf(300));
        projectFiscalModel1.setFiscalReportedSpendAmount(BigDecimal.valueOf(12000));
        projectFiscalModel1.setFiscalActualAmount(BigDecimal.valueOf(10000));
        projectFiscalModel1.setFiscalCompletedSizeHa(BigDecimal.valueOf(45));
        projectFiscalModel1.setFiscalActualCostPerHaAmt(BigDecimal.valueOf(220));
        projectFiscalModel1.setFirstNationsDelivPartInd(true);
        projectFiscalModel1.setFirstNationsEngagementInd(true);
        projectFiscalModel1.setFirstNationsPartner("First Nations Partner Name 1");
        projectFiscalModel1.setOtherPartner("Other Partner Name 1");
        projectFiscalModel1.setResultsNumber("RESULT12345_1");
        projectFiscalModel1.setResultsOpeningId("RESULT_OPEN_ID_1");
        projectFiscalModel1.setResultsContactEmail("contact1@example.com");
        projectFiscalModel1.setSubmittedByName("Submitter Name 1");
        projectFiscalModel1.setSubmittedByUserGuid("user-guid-12345-1");
        projectFiscalModel1.setSubmittedByUserUserid("user123-1");
        projectFiscalModel1.setSubmissionTimestamp(new Date());
        projectFiscalModel1.setEndorsementEvalTimestamp(new Date());
        projectFiscalModel1.setEndorserName("Endorser Name 1");
        projectFiscalModel1.setEndorserUserGuid("endorser-guid-12345-1");
        projectFiscalModel1.setEndorserUserUserid("endorser123-1");
        projectFiscalModel1.setEndorsementTimestamp(new Date());
        projectFiscalModel1.setEndorsementComment("Endorsement Comment 1");
        projectFiscalModel1.setIsApprovedInd(true);
        projectFiscalModel1.setApproverName("Approver Name 1");
        projectFiscalModel1.setApproverUserGuid("approver-guid-12345-1");
        projectFiscalModel1.setApproverUserUserid("approver123-1");
        projectFiscalModel1.setApprovedTimestamp(new Date());
        projectFiscalModel1.setAccomplishmentsComment("Accomplishments Comment 1");
        projectFiscalModel1.setIsDelayedInd(false);
        projectFiscalModel1.setDelayRationale(null);
        projectFiscalModel1.setAbandonedRationale("Abandoned due to lack of resources");
        projectFiscalModel1.setLastProgressUpdateTimestamp(new Date());

        ProjectFiscalModel projectFiscalModel2 = new ProjectFiscalModel();
        projectFiscalModel2.setProjectPlanFiscalGuid("456e7890-e89b-12d3-a456-426614174002");
        projectFiscalModel2.setProjectGuid("123e4567-e89b-12d3-a456-426614174001");
        projectFiscalModel2.setActivityCategoryCode("ACTIVITY_CODE_2");
        projectFiscalModel2.setFiscalYear(2022L);
        projectFiscalModel2.setAncillaryFundingSourceGuid("789e1234-e89b-12d3-a456-426614174003");
        projectFiscalModel2.setProjectPlanStatusCode("PLAN_STATUS_2");
        projectFiscalModel2.setPlanFiscalStatusCode("FISCAL_STATUS_2");
        projectFiscalModel2.setProposalTypeCode("NEW");
        projectFiscalModel2.setEndorsementCode("ENDORSEMENT_CODE_2");
        projectFiscalModel2.setProjectFiscalName("Test Project Fiscal 2");
        projectFiscalModel2.setProjectFiscalDescription("Description of Test Project Fiscal 2");
        projectFiscalModel2.setBusinessAreaComment("Business area comment example 2");
        projectFiscalModel2.setEstimatedClwrrAllocAmount(BigDecimal.valueOf(20000));
        projectFiscalModel2.setTotalCostEstimateAmount(BigDecimal.valueOf(60000));
        projectFiscalModel2.setCfsProjectCode("CFS-456");
        projectFiscalModel2.setFiscalForecastAmount(BigDecimal.valueOf(25000));
        projectFiscalModel2.setFiscalAncillaryFundAmount(BigDecimal.valueOf(7000));
        projectFiscalModel2.setFiscalPlannedProjectSizeHa(BigDecimal.valueOf(70));
        projectFiscalModel2.setFiscalPlannedCostPerHaAmt(BigDecimal.valueOf(400));
        projectFiscalModel2.setFiscalReportedSpendAmount(BigDecimal.valueOf(22000));
        projectFiscalModel2.setFiscalActualAmount(BigDecimal.valueOf(20000));
        projectFiscalModel2.setFiscalCompletedSizeHa(BigDecimal.valueOf(65));
        projectFiscalModel2.setFiscalActualCostPerHaAmt(BigDecimal.valueOf(320));
        projectFiscalModel2.setFirstNationsDelivPartInd(false);
        projectFiscalModel2.setFirstNationsEngagementInd(false);
        projectFiscalModel2.setFirstNationsPartner(null);
        projectFiscalModel2.setOtherPartner("Other Partner Name 2");
        projectFiscalModel2.setResultsNumber("RESULT12345_2");
        projectFiscalModel2.setResultsOpeningId("RESULT_OPEN_ID_2");
        projectFiscalModel2.setResultsContactEmail("contact2@example.com");
        projectFiscalModel2.setSubmittedByName("Submitter Name 2");
        projectFiscalModel2.setSubmittedByUserGuid("user-guid-12345-2");
        projectFiscalModel2.setSubmittedByUserUserid("user123-2");
        projectFiscalModel2.setSubmissionTimestamp(new Date());
        projectFiscalModel2.setEndorsementEvalTimestamp(new Date());
        projectFiscalModel2.setEndorserName("Endorser Name 2");
        projectFiscalModel2.setEndorserUserGuid("endorser-guid-12345-2");
        projectFiscalModel2.setEndorserUserUserid("endorser123-2");
        projectFiscalModel2.setEndorsementTimestamp(new Date());
        projectFiscalModel2.setEndorsementComment("Endorsement Comment 2");
        projectFiscalModel2.setIsApprovedInd(true);
        projectFiscalModel2.setApproverName("Approver Name 2");
        projectFiscalModel2.setApproverUserGuid("approver-guid-12345-2");
        projectFiscalModel2.setApproverUserUserid("approver123-2");
        projectFiscalModel2.setApprovedTimestamp(new Date());
        projectFiscalModel2.setAccomplishmentsComment("Accomplishments Comment 2");
        projectFiscalModel2.setIsDelayedInd(true);
        projectFiscalModel2.setDelayRationale("Delay due to unforeseen circumstances");
        projectFiscalModel2.setAbandonedRationale(null);
        projectFiscalModel2.setLastProgressUpdateTimestamp(new Date());
        List<ProjectFiscalEntity> entities = Arrays.asList(
                ProjectFiscalEntity.builder()
                        .projectPlanFiscalGuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"))
                        .project(ProjectEntity.builder()
                                .projectGuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                                .projectName("Project 1")
                                .build())
                        .activityCategoryCode("ACTIVITY_1")
                        .fiscalYear(BigDecimal.valueOf(2023))
                        .projectPlanStatusCode("ACTIVE")
                        .planFiscalStatusCode("DRAFT")
                        .proposalTypeCode("NEW")
                        .projectFiscalName("Fiscal Plan 1")
                        .totalCostEstimateAmount(BigDecimal.valueOf(100000))
                        .isApprovedInd(true)
                        .build(),
                ProjectFiscalEntity.builder()
                        .projectPlanFiscalGuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174002"))
                        .project(ProjectEntity.builder()
                                .projectGuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174002"))
                                .projectName("Project 2")
                                .build())
                        .activityCategoryCode("ACTIVITY_2")
                        .fiscalYear(BigDecimal.valueOf(2024))
                        .projectPlanStatusCode("DRAFT")
                        .planFiscalStatusCode("PROPOSED")
                        .proposalTypeCode("NEW")
                        .projectFiscalName("Fiscal Plan 2")
                        .totalCostEstimateAmount(BigDecimal.valueOf(200000))
                        .isApprovedInd(false)
                        .build()
        );

        UUID projectGuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

        // Update the mock to use the new repository method
        when(projectFiscalRepository.findAllByProject_ProjectGuid(projectGuid))
                .thenReturn(entities);
        when(projectFiscalResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(Arrays.asList(projectFiscalModel1, projectFiscalModel2)));

        // WHEN I get all project fiscals
        CollectionModel<ProjectFiscalModel> allProjectFiscals =
                projectFiscalService.getAllProjectFiscals(projectGuid.toString());

        // THEN I should get a list with some elements
        Assertions.assertNotEquals(0, allProjectFiscals.getContent().size());
    }

    @Test
    void testCreateProjectFiscal_Success() {
        // GIVEN a valid ProjectFiscalModel
        ProjectFiscalModel projectFiscalModel = new ProjectFiscalModel();
        projectFiscalModel.setProjectPlanFiscalGuid("742ae759-e984-4092-8d85-0c65102c7562");
        projectFiscalModel.setProjectGuid("123e4567-e89b-12d3-a456-426614174000");
        projectFiscalModel.setActivityCategoryCode("ACTIVITY_CODE_1");
        projectFiscalModel.setFiscalYear(2021L);
        projectFiscalModel.setAncillaryFundingSourceGuid("789e1234-e89b-12d3-a456-426614174002");

        ProjectFiscalEntity projectFiscalEntity = new ProjectFiscalEntity();
        projectFiscalEntity.setProjectPlanFiscalGuid(UUID.fromString("742ae759-e984-4092-8d85-0c65102c7562"));
        ProjectEntity entity = new ProjectEntity();
        entity.setProjectGuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        projectFiscalEntity.setProject(entity);
        projectFiscalEntity.setActivityCategoryCode("ACTIVITY_CODE_1");
        projectFiscalEntity.setFiscalYear(new BigDecimal(2021));


        ProjectFiscalEntity savedEntity = new ProjectFiscalEntity();
        savedEntity.setProjectPlanFiscalGuid(UUID.fromString("742ae759-e984-4092-8d85-0c65102c7562"));
        savedEntity.setProject(entity);
        savedEntity.setActivityCategoryCode("ACTIVITY_CODE_1");
        savedEntity.setFiscalYear(new BigDecimal(2021));

        ProjectFiscalModel savedModel = new ProjectFiscalModel();
        savedModel.setProjectPlanFiscalGuid("742ae759-e984-4092-8d85-0c65102c7562");
        savedModel.setProjectGuid("123e4567-e89b-12d3-a456-426614174000");
        savedModel.setActivityCategoryCode("ACTIVITY_CODE_1");
        savedModel.setFiscalYear(2021L);

        // Mock the dependencies
        when(projectFiscalResourceAssembler.toEntity(eq(projectFiscalModel), any()))
                .thenReturn(projectFiscalEntity);
        when(projectFiscalRepository.save(eq(projectFiscalEntity)))
                .thenReturn(savedEntity);
        when(projectFiscalResourceAssembler.toModel(eq(savedEntity)))
                .thenReturn(savedModel);

        // WHEN the createProjectFiscal method is called
        ProjectFiscalModel result = projectFiscalService.createProjectFiscal(projectFiscalModel);

        // THEN the result should match the expected ProjectFiscalModel
        assertEquals(savedModel, result);

        // Verify the interactions
        verify(projectFiscalResourceAssembler).toEntity(eq(projectFiscalModel), any());
        verify(projectFiscalRepository).save(eq(projectFiscalEntity));
        verify(projectFiscalResourceAssembler).toModel(eq(savedEntity));
    }

    @Test
    void testUpdateProjectFiscal_Success() {
        //GIVEN I have a project fiscal model
        ProjectFiscalModel projectFiscalModel = new ProjectFiscalModel();
        projectFiscalModel.setProjectPlanFiscalGuid("456e7890-e89b-12d3-a456-426614174001");
        projectFiscalModel.setProjectGuid("123e4567-e89b-12d3-a456-426614174000");
        projectFiscalModel.setActivityCategoryCode("ACTIVITY_CODE_1");
        projectFiscalModel.setFiscalYear(2021L);
        projectFiscalModel.setAncillaryFundingSourceGuid("789e1234-e89b-12d3-a456-426614174002");
        projectFiscalModel.setProjectPlanStatusCode("PLAN_STATUS_1");
        projectFiscalModel.setPlanFiscalStatusCode("FISCAL_STATUS_1");
        projectFiscalModel.setProposalTypeCode("NEW");
        projectFiscalModel.setEndorsementCode("ENDORSEMENT_CODE_1");
        projectFiscalModel.setProjectFiscalName("Test Project Fiscal 1");
        projectFiscalModel.setProjectFiscalDescription("Description of Test Project Fiscal 1");
        projectFiscalModel.setBusinessAreaComment("Business area comment example 1");
        projectFiscalModel.setEstimatedClwrrAllocAmount(BigDecimal.valueOf(10000));
        projectFiscalModel.setTotalCostEstimateAmount(BigDecimal.valueOf(50000));
        projectFiscalModel.setCfsProjectCode("CFS-123");
        projectFiscalModel.setFiscalForecastAmount(BigDecimal.valueOf(20000));
        projectFiscalModel.setFiscalAncillaryFundAmount(BigDecimal.valueOf(5000));
        projectFiscalModel.setFiscalPlannedProjectSizeHa(BigDecimal.valueOf(50));
        projectFiscalModel.setFiscalPlannedCostPerHaAmt(BigDecimal.valueOf(300));
        projectFiscalModel.setFiscalReportedSpendAmount(BigDecimal.valueOf(12000));
        projectFiscalModel.setFiscalActualAmount(BigDecimal.valueOf(10000));
        projectFiscalModel.setFiscalCompletedSizeHa(BigDecimal.valueOf(45));
        projectFiscalModel.setFiscalActualCostPerHaAmt(BigDecimal.valueOf(220));
        projectFiscalModel.setFirstNationsDelivPartInd(true);
        projectFiscalModel.setFirstNationsEngagementInd(true);
        projectFiscalModel.setFirstNationsPartner("First Nations Partner Name 1");
        projectFiscalModel.setOtherPartner("Other Partner Name 1");
        projectFiscalModel.setResultsNumber("RESULT12345_1");
        projectFiscalModel.setResultsOpeningId("RESULT_OPEN_ID_1");
        projectFiscalModel.setResultsContactEmail("test@test.com");
        projectFiscalModel.setSubmittedByName("Submitter Name 1");
        projectFiscalModel.setSubmittedByUserGuid("user-guid-12345-1");
        projectFiscalModel.setSubmittedByUserUserid("user123-1");
        projectFiscalModel.setSubmissionTimestamp(new Date());
        projectFiscalModel.setEndorsementEvalTimestamp(new Date());
        projectFiscalModel.setEndorserName("Endorser Name 1");
        projectFiscalModel.setEndorserUserGuid("endorser-guid-12345-1");
        projectFiscalModel.setEndorserUserUserid("endorser123-1");
        projectFiscalModel.setEndorsementTimestamp(new Date());
        projectFiscalModel.setEndorsementComment("Endorsement Comment 1");
        projectFiscalModel.setIsApprovedInd(true);
        projectFiscalModel.setApproverName("Approver Name 1");
        projectFiscalModel.setApproverUserGuid("approver-guid-12345-1");
        projectFiscalModel.setApproverUserUserid("approver123-1");
        projectFiscalModel.setApprovedTimestamp(new Date());
        projectFiscalModel.setAccomplishmentsComment("Accomplishments Comment 1");
        projectFiscalModel.setIsDelayedInd(false);
        projectFiscalModel.setDelayRationale(null);
        projectFiscalModel.setAbandonedRationale("Abandoned due to lack of resources");
        projectFiscalModel.setLastProgressUpdateTimestamp(new Date());

        ProjectFiscalEntity mockedEntity = ProjectFiscalEntity.builder()
                .projectPlanFiscalGuid(UUID.fromString("456e7890-e89b-12d3-a456-426614174001"))
                .activityCategoryCode("ACTIVITY_CODE_1")
                .fiscalYear(BigDecimal.valueOf(2021))
                .projectPlanStatusCode("PLAN_STATUS_1")
                .planFiscalStatusCode("FISCAL_STATUS_1")
                .proposalTypeCode("NEW")
                .projectFiscalName("Test Project Fiscal 1")
                .build();

        //WHEN I update a project fiscal
        //Mock assembler behavior
        when(projectFiscalResourceAssembler.toEntity(projectFiscalModel, projectEntity)).thenReturn(mockedEntity);
        when(projectFiscalResourceAssembler.toModel(mockedEntity)).thenReturn(projectFiscalModel);

        when(projectFiscalRepository.findById(UUID.fromString("456e7890-e89b-12d3-a456-426614174001"))).thenReturn(java.util.Optional.of(mockedEntity));
        when(projectFiscalResourceAssembler.updateEntity(projectFiscalModel, mockedEntity)).thenReturn(mockedEntity);
        when(projectFiscalRepository.saveAndFlush(any(ProjectFiscalEntity.class))).thenReturn(mockedEntity);

        //Mock repository behavior
        ProjectFiscalModel updatedProjectFiscal = projectFiscalService.updateProjectFiscal(projectFiscalModel);

        //THEN I should get the updated project fiscal
        assertEquals(projectFiscalModel, updatedProjectFiscal);
    }

    @Test
    void testUpdateProjectFiscal_NotFoundOnGet() {
        // GIVEN I have a project fiscal model
        ProjectFiscalModel projectFiscalModel = new ProjectFiscalModel();
        projectFiscalModel.setProjectPlanFiscalGuid("456e7890-e89b-12d3-a456-426614174001");
        projectFiscalModel.setProjectGuid("123e4567-e89b-12d3-a456-426614174000");
        projectFiscalModel.setActivityCategoryCode("ACTIVITY_CODE_1");
        projectFiscalModel.setFiscalYear(2021L);
        projectFiscalModel.setAncillaryFundingSourceGuid("789e1234-e89b-12d3-a456-426614174002");
        projectFiscalModel.setProjectPlanStatusCode("PLAN_STATUS_1");
        projectFiscalModel.setPlanFiscalStatusCode("FISCAL_STATUS_1");
        projectFiscalModel.setProposalTypeCode("NEW");
        projectFiscalModel.setEndorsementCode("ENDORSEMENT_CODE_1");
        projectFiscalModel.setProjectFiscalName("Test Project Fiscal 1");
        projectFiscalModel.setProjectFiscalDescription("Description of Test Project Fiscal 1");
        projectFiscalModel.setBusinessAreaComment("Business area comment example 1");
        projectFiscalModel.setEstimatedClwrrAllocAmount(BigDecimal.valueOf(10000));
        projectFiscalModel.setTotalCostEstimateAmount(BigDecimal.valueOf(50000));
        projectFiscalModel.setCfsProjectCode("CFS-123");
        projectFiscalModel.setFiscalActualAmount(BigDecimal.valueOf(15000));
        projectFiscalModel.setFiscalAncillaryFundAmount(BigDecimal.valueOf(5000));
        projectFiscalModel.setFiscalPlannedProjectSizeHa(BigDecimal.valueOf(50));
        projectFiscalModel.setFiscalPlannedCostPerHaAmt(BigDecimal.valueOf(300));
        projectFiscalModel.setFiscalReportedSpendAmount(BigDecimal.valueOf(12000));
        projectFiscalModel.setFiscalActualAmount(BigDecimal.valueOf(10000));
        projectFiscalModel.setFiscalCompletedSizeHa(BigDecimal.valueOf(45));
        projectFiscalModel.setFiscalActualCostPerHaAmt(BigDecimal.valueOf(220));
        projectFiscalModel.setFirstNationsDelivPartInd(true);
        projectFiscalModel.setFirstNationsEngagementInd(true);
        projectFiscalModel.setFirstNationsPartner("First Nations Partner Name 1");
        projectFiscalModel.setOtherPartner("Other Partner Name 1");
        projectFiscalModel.setResultsNumber("RESULT12345_1");
        projectFiscalModel.setResultsOpeningId("RESULT_OPEN_ID_1");


        // WHEN I update a project fiscal
        // Mock assembler behavior
        when(projectFiscalResourceAssembler.toEntity(projectFiscalModel, projectEntity)).thenReturn(null);
        when(projectFiscalResourceAssembler.toModel(null)).thenReturn(null);

        when(projectFiscalRepository.findById(UUID.fromString("456e7890-e89b-12d3-a456-426614174001"))).thenReturn(java.util.Optional.empty());

        // THEN I should get an EntityNotFoundException
        assertThrows(EntityNotFoundException.class, () -> projectFiscalService.updateProjectFiscal(projectFiscalModel));

    }

    @Test
    void testUpdateProjectFiscal_NotFoundOnSave() {
        // GIVEN I have a project fiscal model
        ProjectFiscalModel projectFiscalModel = new ProjectFiscalModel();
        projectFiscalModel.setProjectPlanFiscalGuid("456e7890-e89b-12d3-a456-426614174001");
        projectFiscalModel.setProjectGuid("123e4567-e89b-12d3-a456-426614174000");
        projectFiscalModel.setActivityCategoryCode("ACTIVITY_CODE_1");
        projectFiscalModel.setFiscalYear(2021L);
        projectFiscalModel.setAncillaryFundingSourceGuid("789e1234-e89b-12d3-a456-426614174002");
        projectFiscalModel.setProjectPlanStatusCode("PLAN_STATUS_1");
        projectFiscalModel.setPlanFiscalStatusCode("FISCAL_STATUS_1");
        projectFiscalModel.setProposalTypeCode("NEW");
        projectFiscalModel.setEndorsementCode("ENDORSEMENT_CODE_1");
        projectFiscalModel.setProjectFiscalName("Test Project Fiscal 1");
        projectFiscalModel.setProjectFiscalDescription("Description of Test Project Fiscal 1");
        projectFiscalModel.setBusinessAreaComment("Business area comment example 1");
        projectFiscalModel.setEstimatedClwrrAllocAmount(BigDecimal.valueOf(10000));
        projectFiscalModel.setTotalCostEstimateAmount(BigDecimal.valueOf(50000));
        projectFiscalModel.setCfsProjectCode("CFS-123");
        projectFiscalModel.setFiscalForecastAmount(BigDecimal.valueOf(20000));
        projectFiscalModel.setFiscalAncillaryFundAmount(BigDecimal.valueOf(5000));
        projectFiscalModel.setFiscalPlannedProjectSizeHa(BigDecimal.valueOf(50));
        projectFiscalModel.setFiscalPlannedCostPerHaAmt(BigDecimal.valueOf(300));
        projectFiscalModel.setFiscalReportedSpendAmount(BigDecimal.valueOf(12000));
        projectFiscalModel.setFiscalActualAmount(BigDecimal.valueOf(10000));
        projectFiscalModel.setFiscalCompletedSizeHa(BigDecimal.valueOf(45));
        projectFiscalModel.setFiscalActualCostPerHaAmt(BigDecimal.valueOf(220));
        projectFiscalModel.setFirstNationsDelivPartInd(true);
        projectFiscalModel.setFirstNationsEngagementInd(true);
        projectFiscalModel.setFirstNationsPartner("First Nations Partner Name 1");
        projectFiscalModel.setOtherPartner("Other Partner Name 1");
        projectFiscalModel.setResultsNumber("RESULT12345_1");
        projectFiscalModel.setResultsOpeningId("RESULT_OPEN_ID_1");

        ProjectFiscalEntity mockedEntity = ProjectFiscalEntity.builder()
                .projectPlanFiscalGuid(UUID.fromString("456e7890-e89b-12d3-a456-426614174001"))
                .activityCategoryCode("ACTIVITY_CODE_1")
                .fiscalYear(BigDecimal.valueOf(2021))
                .projectPlanStatusCode("PLAN_STATUS_1")
                .planFiscalStatusCode("FISCAL_STATUS_1")
                .proposalTypeCode("NEW")
                .projectFiscalName("Test Project Fiscal 1")
                .build();


        // WHEN I update a project fiscal
        when(projectFiscalResourceAssembler.toEntity(projectFiscalModel, projectEntity)).thenReturn(mockedEntity);
        when(projectFiscalResourceAssembler.toModel(mockedEntity)).thenReturn(projectFiscalModel);

        when(projectFiscalRepository.findById(UUID.fromString("456e7890-e89b-12d3-a456-426614174001"))).thenReturn(java.util.Optional.of(mockedEntity));
        when(projectFiscalResourceAssembler.updateEntity(projectFiscalModel, mockedEntity)).thenReturn(mockedEntity);
        when(projectFiscalRepository.saveAndFlush(any(ProjectFiscalEntity.class))).thenThrow(new EntityNotFoundException("Project not found: 456e7890-e89b-12d3-a456-426614174001"));

        // THEN I should get an EntityNotFoundException
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> projectFiscalService.updateProjectFiscal(projectFiscalModel));
        Assertions.assertEquals("Project not found: 456e7890-e89b-12d3-a456-426614174001", exception.getMessage());
    }

    @Test
    void testUpdateProjectFiscal_DataIntegrityViolationException() {
        // GIVEN I have a project fiscal model
        ProjectFiscalModel projectFiscalModel = new ProjectFiscalModel();
        projectFiscalModel.setProjectPlanFiscalGuid("456e7890-e89b-12d3-a456-426614174001");
        projectFiscalModel.setProjectGuid("123e4567-e89b-12d3-a456-426614174000");
        projectFiscalModel.setActivityCategoryCode("ACTIVITY_CODE_1");
        projectFiscalModel.setFiscalYear(2021L);
        projectFiscalModel.setAncillaryFundingSourceGuid("789e1234-e89b-12d3-a456-426614174002");
        projectFiscalModel.setProjectPlanStatusCode("PLAN_STATUS_1");
        projectFiscalModel.setPlanFiscalStatusCode("FISCAL_STATUS_1");
        projectFiscalModel.setProposalTypeCode("NEW");
        projectFiscalModel.setEndorsementCode("ENDORSEMENT_CODE_1");
        projectFiscalModel.setProjectFiscalName("Test Project Fiscal 1");
        projectFiscalModel.setProjectFiscalDescription("Description of Test Project Fiscal 1");
        projectFiscalModel.setBusinessAreaComment("Business area comment example 1");
        projectFiscalModel.setEstimatedClwrrAllocAmount(BigDecimal.valueOf(10000));
        projectFiscalModel.setTotalCostEstimateAmount(BigDecimal.valueOf(50000));
        projectFiscalModel.setCfsProjectCode("CFS-123");
        projectFiscalModel.setFiscalForecastAmount(BigDecimal.valueOf(20000));
        projectFiscalModel.setFiscalAncillaryFundAmount(BigDecimal.valueOf(5000));
        projectFiscalModel.setFiscalPlannedProjectSizeHa(BigDecimal.valueOf(50));
        projectFiscalModel.setFiscalPlannedCostPerHaAmt(BigDecimal.valueOf(300));
        projectFiscalModel.setFiscalReportedSpendAmount(BigDecimal.valueOf(12000));
        projectFiscalModel.setFiscalActualAmount(BigDecimal.valueOf(10000));
        projectFiscalModel.setFiscalCompletedSizeHa(BigDecimal.valueOf(45));
        projectFiscalModel.setFiscalActualCostPerHaAmt(BigDecimal.valueOf(220));
        projectFiscalModel.setFirstNationsDelivPartInd(true);
        projectFiscalModel.setFirstNationsEngagementInd(true);
        projectFiscalModel.setFirstNationsPartner("First Nations Partner Name 1");
        projectFiscalModel.setOtherPartner("Other Partner Name 1");
        projectFiscalModel.setResultsNumber("RESULT12345_1");
        projectFiscalModel.setResultsOpeningId("RESULT_OPEN_ID_1");
        projectFiscalModel.setResultsContactEmail("contact1@example.com");
        projectFiscalModel.setSubmittedByName("Submitter Name 1");
        projectFiscalModel.setSubmittedByUserGuid("user-guid-12345-1");
        projectFiscalModel.setSubmittedByUserUserid("user123-1");
        projectFiscalModel.setSubmissionTimestamp(new Date());
        projectFiscalModel.setEndorsementEvalTimestamp(new Date());
        projectFiscalModel.setEndorserName("Endorser Name 1");
        projectFiscalModel.setEndorserUserGuid("endorser-guid-12345-1");
        projectFiscalModel.setEndorserUserUserid("endorser123-1");
        projectFiscalModel.setEndorsementTimestamp(new Date());
        projectFiscalModel.setEndorsementComment("Endorsement Comment 1");
        projectFiscalModel.setIsApprovedInd(true);
        projectFiscalModel.setApproverName("Approver Name 1");
        projectFiscalModel.setApproverUserGuid("approver-guid-12345-1");
        projectFiscalModel.setApproverUserUserid("approver123-1");
        projectFiscalModel.setApprovedTimestamp(new Date());
        projectFiscalModel.setAccomplishmentsComment("Accomplishments Comment 1");
        projectFiscalModel.setIsDelayedInd(false);
        projectFiscalModel.setDelayRationale(null);
        projectFiscalModel.setAbandonedRationale("Abandoned due to lack of resources");
        projectFiscalModel.setLastProgressUpdateTimestamp(new Date());

        // WHEN I update a project fiscal
        // AND the repository throws a DataIntegrityViolationException
        when(projectFiscalResourceAssembler.toEntity(projectFiscalModel, projectEntity)).thenReturn(null);
        when(projectFiscalResourceAssembler.toModel(null)).thenReturn(null);

        when(projectFiscalRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(new ProjectFiscalEntity()));
        when(projectFiscalResourceAssembler.updateEntity(any(ProjectFiscalModel.class), any(ProjectFiscalEntity.class))).thenReturn(new ProjectFiscalEntity());
        when(projectFiscalRepository.saveAndFlush(any(ProjectFiscalEntity.class))).thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        // THEN I should get a DataIntegrityViolationException
        assertThrows(DataIntegrityViolationException.class,
                () -> projectFiscalService.updateProjectFiscal(projectFiscalModel));
    }

    @Test
    void testGetAProjectFiscal_Success() {
        //GIVEN I have a project fiscal model
        ProjectFiscalModel projectFiscalModel = new ProjectFiscalModel();
        projectFiscalModel.setProjectPlanFiscalGuid("456e7890-e89b-12d3-a456-426614174001");

        //WHEN I get a project fiscal
        when(projectFiscalRepository.findById(UUID.fromString("456e7890-e89b-12d3-a456-426614174001"))).thenReturn(Optional.of(new ProjectFiscalEntity()));
        when(projectFiscalResourceAssembler.toModel(any(ProjectFiscalEntity.class))).thenReturn(projectFiscalModel);

        ProjectFiscalModel projectFiscal = projectFiscalService.getProjectFiscal("456e7890-e89b-12d3-a456-426614174001");

        //THEN I should get the project fiscal
        assertEquals(projectFiscalModel, projectFiscal);
    }

    @Test
    void testGetAProjectFiscal_EntityNotFound() {
        // GIVEN I have a project fiscal model
        ProjectFiscalModel projectFiscalModel = new ProjectFiscalModel();
        projectFiscalModel.setProjectPlanFiscalGuid("456e7890-e89b-12d3-a456-426614174001");

        // WHEN I get a project fiscal
        when(projectFiscalRepository.findById(UUID.fromString("456e7890-e89b-12d3-a456-426614174001"))).thenReturn(Optional.empty());

        // THEN I should get an EntityNotFoundException
        assertThrows(EntityNotFoundException.class, () -> projectFiscalService.getProjectFiscal("456e7890-e89b-12d3-a456-426614174001"));
    }

    @Test
    void testDeleteAProjectFiscal_Success() {
        // GIVEN a valid project fiscal ID
        UUID projectFiscalGuid = UUID.fromString("456e7890-e89b-12d3-a456-426614174001");
        when(projectFiscalRepository.findById(projectFiscalGuid)).thenReturn(Optional.of(new ProjectFiscalEntity()));

        // WHEN the deleteProjectFiscal method is called
        projectFiscalService.deleteProjectFiscal("456e7890-e89b-12d3-a456-426614174001");

        // THEN verify the repository interactions
        verify(projectFiscalRepository).findById(projectFiscalGuid);
        verify(projectFiscalRepository).deleteById(projectFiscalGuid);
        verifyNoMoreInteractions(projectFiscalRepository); // Ensure no other interactions occur
    }

    @Test
    void testDeleteAProjectFiscal_ResourceNotFound() {
        // GIVEN a project fiscal ID that does not exist
        UUID projectFiscalGuid = UUID.fromString("456e7890-e89b-12d3-a456-426614174001");
        when(projectFiscalRepository.findById(projectFiscalGuid)).thenReturn(Optional.empty());

        // WHEN the deleteProjectFiscal method is called
        // THEN an EntityNotFoundException should be thrown
        assertThrows(EntityNotFoundException.class, () ->
                projectFiscalService.deleteProjectFiscal("456e7890-e89b-12d3-a456-426614174001"));

        // Verify the repository's deleteById is never called
        verify(projectFiscalRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteAProjectFiscal_InvalidUUID() {
        // GIVEN an invalid UUID
        String invalidUuid = "invalid-uuid";

        // WHEN the deleteProjectFiscal method is called
        // THEN an IllegalArgumentException should be thrown
        assertThrows(IllegalArgumentException.class, () ->
                projectFiscalService.deleteProjectFiscal(invalidUuid));

        // Verify the repository is never interacted with
        verifyNoInteractions(projectFiscalRepository);
    }

    @Test
    void testDeleteAProjectFiscal_DatabaseError() {
        // GIVEN a valid project fiscal ID and a database error during deletion
        UUID projectFiscalGuid = UUID.fromString("456e7890-e89b-12d3-a456-426614174001");

        // Mock findById to return an entity, ensuring deleteById is called
        when(projectFiscalRepository.findById(projectFiscalGuid)).thenReturn(Optional.of(new ProjectFiscalEntity()));

        // Mock deleteById to throw a DataIntegrityViolationException
        doThrow(new DataIntegrityViolationException("Database error")).when(projectFiscalRepository).deleteById(projectFiscalGuid);

        // WHEN the deleteProjectFiscal method is called
        // THEN a DataIntegrityViolationException should be thrown
        assertThrows(DataIntegrityViolationException.class, () ->
                projectFiscalService.deleteProjectFiscal("456e7890-e89b-12d3-a456-426614174001"));

        // Verify the repository's findById and deleteById methods are called
        verify(projectFiscalRepository).findById(projectFiscalGuid);
        verify(projectFiscalRepository).deleteById(projectFiscalGuid);
    }
}
