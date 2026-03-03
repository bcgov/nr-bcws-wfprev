package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.ActivityResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ContractPhaseCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.entities.RiskRatingCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.data.models.ActivityStatusCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ContractPhaseCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.data.models.RiskRatingCodeModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityStatusCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ContractPhaseCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectFiscalRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.RiskRatingCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

class ActivityServiceTest {

    private ActivityRepository activityRepository;
    private ActivityResourceAssembler activityResourceAssembler;
    private ProjectFiscalRepository projectFiscalRepository;
    private ActivityStatusCodeRepository activityStatusCodeRepository;
    private ContractPhaseCodeRepository contractPhaseCodeRepository;
    private RiskRatingCodeRepository riskRatingCodeRepository;
    private ActivityBoundaryService activityBoundaryService;
    private ActivityService activityService;
    private Validator validator;

    @Mock
    private FileAttachmentService fileAttachmentService;

    @BeforeEach
    void setup() {
        activityRepository = mock(ActivityRepository.class);
        activityResourceAssembler = mock(ActivityResourceAssembler.class);
        projectFiscalRepository = mock(ProjectFiscalRepository.class);
        activityStatusCodeRepository = mock(ActivityStatusCodeRepository.class);
        contractPhaseCodeRepository = mock(ContractPhaseCodeRepository.class);
        riskRatingCodeRepository = mock(RiskRatingCodeRepository.class);
        activityBoundaryService = mock(ActivityBoundaryService.class);
        fileAttachmentService = mock(FileAttachmentService.class);
        validator = mock(Validator.class);

        activityService = new ActivityService(
                activityRepository,
                activityResourceAssembler,
                projectFiscalRepository,
                activityStatusCodeRepository,
                contractPhaseCodeRepository,
                riskRatingCodeRepository,
                activityBoundaryService,
                fileAttachmentService,
                validator
        );
    }

    @Test
    void testGetAllActivities_Success() throws ServiceException {
        // GIVEN
        String projectGuid = "123e4567-e89b-12d3-a456-426614174000";
        String fiscalGuid = "456e7890-e89b-12d3-a456-426614174001";

        ProjectEntity projectEntity = ProjectEntity.builder()
                .projectGuid(UUID.fromString(projectGuid))
                .build();

        ProjectFiscalEntity projectFiscalEntity = ProjectFiscalEntity.builder()
                .projectPlanFiscalGuid(UUID.fromString(fiscalGuid))
                .project(projectEntity)
                .build();

        List<ActivityEntity> activities = Arrays.asList(
                ActivityEntity.builder()
                        .activityGuid(UUID.randomUUID())
                        .projectPlanFiscalGuid(UUID.fromString(fiscalGuid))
                        .build(),
                ActivityEntity.builder()
                        .activityGuid(UUID.randomUUID())
                        .projectPlanFiscalGuid(UUID.fromString(fiscalGuid))
                        .build()
        );

        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid)))
                .thenReturn(Optional.of(projectFiscalEntity));
        when(activityRepository.findByProjectPlanFiscalGuid(UUID.fromString(fiscalGuid)))
                .thenReturn(activities);
        when(activityResourceAssembler.toModel(any(ActivityEntity.class)))
                .thenReturn(new ActivityModel());

        // WHEN
        CollectionModel<ActivityModel> result = activityService.getAllActivities(projectGuid, fiscalGuid);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(projectFiscalRepository).findById(UUID.fromString(fiscalGuid));
        verify(activityRepository).findByProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));
    }

    @Test
    void testCreateActivity_Success() {
        // GIVEN
        String projectGuid = "123e4567-e89b-12d3-a456-426614174000";
        String fiscalGuid = "456e7890-e89b-12d3-a456-426614174001";
        ActivityModel activityModel = new ActivityModel();
        activityModel.setProjectPlanFiscalGuid(fiscalGuid);

        ProjectEntity projectEntity = ProjectEntity.builder()
                .projectGuid(UUID.fromString(projectGuid))
                .build();

        ProjectFiscalEntity projectFiscalEntity = ProjectFiscalEntity.builder()
                .projectPlanFiscalGuid(UUID.fromString(fiscalGuid))
                .project(projectEntity)
                .build();

        ActivityEntity activityEntity = new ActivityEntity();
        ActivityEntity savedEntity = new ActivityEntity();
        ActivityModel savedModel = new ActivityModel();

        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid)))
                .thenReturn(Optional.of(projectFiscalEntity));
        when(activityResourceAssembler.toEntity(eq(activityModel))).thenReturn(activityEntity);
        when(activityRepository.save(any(ActivityEntity.class))).thenReturn(savedEntity);
        when(activityResourceAssembler.toModel(savedEntity)).thenReturn(savedModel);

        // WHEN
        ActivityModel result = activityService.createActivity(projectGuid, fiscalGuid, activityModel);

        // THEN
        assertNotNull(result);
        verify(activityRepository).save(any(ActivityEntity.class));
    }

    @Test
    void testUpdateActivity_Success() {
        // GIVEN
        String projectGuid = "123e4567-e89b-12d3-a456-426614174000";
        String fiscalGuid = "456e7890-e89b-12d3-a456-426614174001";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";

        ActivityModel activityModel = new ActivityModel();
        activityModel.setActivityGuid(activityGuid);
        activityModel.setProjectPlanFiscalGuid(fiscalGuid);

        ProjectEntity projectEntity = ProjectEntity.builder()
                .projectGuid(UUID.fromString(projectGuid))
                .build();

        ProjectFiscalEntity projectFiscalEntity = ProjectFiscalEntity.builder()
                .projectPlanFiscalGuid(UUID.fromString(fiscalGuid))
                .project(projectEntity)
                .build();

        ActivityEntity existingEntity = new ActivityEntity();
        existingEntity.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));

        ActivityEntity updatedEntity = new ActivityEntity();
        ActivityModel updatedModel = new ActivityModel();

        when(activityRepository.findById(UUID.fromString(activityGuid)))
                .thenReturn(Optional.of(existingEntity));
        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid)))
                .thenReturn(Optional.of(projectFiscalEntity));
        when(activityResourceAssembler.updateEntity(eq(activityModel), any()))
                .thenReturn(updatedEntity);
        when(activityRepository.saveAndFlush(any(ActivityEntity.class)))
                .thenReturn(updatedEntity);
        when(activityResourceAssembler.toModel(updatedEntity))
                .thenReturn(updatedModel);

        // WHEN
        ActivityModel result = activityService.updateActivity(projectGuid, fiscalGuid, activityModel);

        // THEN
        assertNotNull(result);
        verify(activityRepository).saveAndFlush(any(ActivityEntity.class));
    }

    @Test
    void testGetActivity_Success() {
        // GIVEN
        String projectGuid = "123e4567-e89b-12d3-a456-426614174000";
        String fiscalGuid = "456e7890-e89b-12d3-a456-426614174001";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";

        ProjectEntity projectEntity = ProjectEntity.builder()
                .projectGuid(UUID.fromString(projectGuid))
                .build();

        ProjectFiscalEntity projectFiscalEntity = ProjectFiscalEntity.builder()
                .projectPlanFiscalGuid(UUID.fromString(fiscalGuid))
                .project(projectEntity)
                .build();

        ActivityEntity activityEntity = new ActivityEntity();
        ActivityEntity activityEntityWithGuid = new ActivityEntity();
        activityEntityWithGuid.setActivityGuid(UUID.fromString(activityGuid));
        activityEntity.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));

        ActivityModel activityModel = new ActivityModel();

        when(activityRepository.findById(UUID.fromString(activityGuid)))
                .thenReturn(Optional.of(activityEntity));
        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid)))
                .thenReturn(Optional.of(projectFiscalEntity));
        when(activityResourceAssembler.toModel(activityEntity))
                .thenReturn(activityModel);

        // WHEN
        ActivityModel result = activityService.getActivity(projectGuid, fiscalGuid, activityGuid);

        // THEN
        assertNotNull(result);
        verify(activityRepository).findById(UUID.fromString(activityGuid));
    }

    @Test
    void testDeleteActivity_Success() {
        // GIVEN
        String projectGuid = "123e4567-e89b-12d3-a456-426614174000";
        String fiscalGuid = "456e7890-e89b-12d3-a456-426614174001";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";

        ProjectEntity projectEntity = ProjectEntity.builder()
                .projectGuid(UUID.fromString(projectGuid))
                .build();

        ProjectFiscalEntity projectFiscalEntity = ProjectFiscalEntity.builder()
                .projectPlanFiscalGuid(UUID.fromString(fiscalGuid))
                .project(projectEntity)
                .build();

        ActivityEntity activityEntity = new ActivityEntity();
        activityEntity.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));

        when(activityRepository.findById(UUID.fromString(activityGuid)))
                .thenReturn(Optional.of(activityEntity));
        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid)))
                .thenReturn(Optional.of(projectFiscalEntity));

        // WHEN
        activityService.deleteActivity(projectGuid, fiscalGuid, activityGuid, true);

        // THEN
        verify(fileAttachmentService).deleteAttachmentsBySourceObject(activityGuid, true);
        verify(activityBoundaryService).deleteActivityBoundaries(activityGuid, true);
        verify(activityRepository).deleteById(UUID.fromString(activityGuid));
    }

    @Test
    void testDeleteActivity_Success_NoFiles() {
        // GIVEN
        String projectGuid = "123e4567-e89b-12d3-a456-426614174000";
        String fiscalGuid = "456e7890-e89b-12d3-a456-426614174001";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";

        ProjectEntity projectEntity = ProjectEntity.builder()
                .projectGuid(UUID.fromString(projectGuid))
                .build();

        ProjectFiscalEntity projectFiscalEntity = ProjectFiscalEntity.builder()
                .projectPlanFiscalGuid(UUID.fromString(fiscalGuid))
                .project(projectEntity)
                .build();

        ActivityEntity activityEntity = new ActivityEntity();
        activityEntity.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));

        when(activityRepository.findById(UUID.fromString(activityGuid)))
                .thenReturn(Optional.of(activityEntity));
        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid)))
                .thenReturn(Optional.of(projectFiscalEntity));

        // WHEN
        activityService.deleteActivity(projectGuid, fiscalGuid, activityGuid, false);

        // THEN
        verify(fileAttachmentService).deleteAttachmentsBySourceObject(activityGuid, false);
        verify(activityBoundaryService).deleteActivityBoundaries(activityGuid, false);
        verify(activityRepository).deleteById(UUID.fromString(activityGuid));
    }

    @Test
    void testGetAllActivities_ProjectFiscalNotFound() {
        // GIVEN
        String projectGuid = "123e4567-e89b-12d3-a456-426614174000";
        String fiscalGuid = "456e7890-e89b-12d3-a456-426614174001";

        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid)))
                .thenReturn(Optional.empty());

        // WHEN/THEN
        assertThrows(EntityNotFoundException.class, () ->
                activityService.getAllActivities(projectGuid, fiscalGuid));
    }

    @Test
    void testCreateActivity_ProjectFiscalMismatch() {
        // GIVEN
        String projectGuid = "123e4567-e89b-12d3-a456-426614174000";
        String fiscalGuid = "456e7890-e89b-12d3-a456-426614174001";
        ActivityModel activityModel = new ActivityModel();

        ProjectEntity projectEntity = ProjectEntity.builder()
                .projectGuid(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .build();

        ProjectFiscalEntity projectFiscalEntity = ProjectFiscalEntity.builder()
                .projectPlanFiscalGuid(UUID.fromString(fiscalGuid))
                .project(projectEntity)
                .build();

        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid)))
                .thenReturn(Optional.of(projectFiscalEntity));

        // WHEN/THEN
        assertThrows(EntityNotFoundException.class, () ->
                activityService.createActivity(projectGuid, fiscalGuid, activityModel));
    }

    @Test
    void testUpdateActivity_DataIntegrityViolation() {
        // GIVEN
        String projectGuid = "123e4567-e89b-12d3-a456-426614174000";
        String fiscalGuid = "456e7890-e89b-12d3-a456-426614174001";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";

        ActivityModel activityModel = new ActivityModel();
        activityModel.setActivityGuid(activityGuid);
        activityModel.setProjectPlanFiscalGuid(fiscalGuid);

        ProjectEntity projectEntity = ProjectEntity.builder()
                .projectGuid(UUID.fromString(projectGuid))
                .build();

        ProjectFiscalEntity projectFiscalEntity = ProjectFiscalEntity.builder()
                .projectPlanFiscalGuid(UUID.fromString(fiscalGuid))
                .project(projectEntity)
                .build();

        ActivityEntity existingEntity = new ActivityEntity();
        existingEntity.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));

        when(activityRepository.findById(UUID.fromString(activityGuid)))
                .thenReturn(Optional.of(existingEntity));
        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid)))
                .thenReturn(Optional.of(projectFiscalEntity));
        when(activityResourceAssembler.updateEntity(eq(activityModel), any()))
                .thenReturn(existingEntity);
        when(activityRepository.saveAndFlush(any(ActivityEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        // WHEN/THEN
        assertThrows(DataIntegrityViolationException.class, () ->
                activityService.updateActivity(projectGuid, fiscalGuid, activityModel));
    }

    @Test
    void testAssignAssociatedEntities_AllCodesPresent() {
        // GIVEN
        ActivityModel resource = new ActivityModel();
        ActivityStatusCodeModel statusCode = new ActivityStatusCodeModel();
        statusCode.setActivityStatusCode("STATUS1");
        RiskRatingCodeModel riskCode = new RiskRatingCodeModel();
        riskCode.setRiskRatingCode("RISK1");
        ContractPhaseCodeModel phaseCode = new ContractPhaseCodeModel();
        phaseCode.setContractPhaseCode("PHASE1");

        resource.setActivityStatusCode(statusCode);
        resource.setRiskRatingCode(riskCode);
        resource.setContractPhaseCode(phaseCode);

        ActivityEntity entity = new ActivityEntity();

        ActivityStatusCodeEntity statusEntity = new ActivityStatusCodeEntity();
        RiskRatingCodeEntity riskEntity = new RiskRatingCodeEntity();
        ContractPhaseCodeEntity phaseEntity = new ContractPhaseCodeEntity();

        when(activityStatusCodeRepository.findById("STATUS1")).thenReturn(Optional.of(statusEntity));
        when(riskRatingCodeRepository.findById("RISK1")).thenReturn(Optional.of(riskEntity));
        when(contractPhaseCodeRepository.findById("PHASE1")).thenReturn(Optional.of(phaseEntity));

        // WHEN
        activityService.assignAssociatedEntities(resource, entity);

        // THEN
        assertEquals(statusEntity, entity.getActivityStatusCode());
        assertEquals(riskEntity, entity.getRiskRatingCode());
        assertEquals(phaseEntity, entity.getContractPhaseCode());
        verify(activityStatusCodeRepository).findById("STATUS1");
        verify(riskRatingCodeRepository).findById("RISK1");
        verify(contractPhaseCodeRepository).findById("PHASE1");
    }

    @Test
    void testAssignAssociatedEntities_NullCodes() {
        // GIVEN
        ActivityModel resource = new ActivityModel();
        ActivityEntity entity = new ActivityEntity();

        // WHEN
        activityService.assignAssociatedEntities(resource, entity);

        // THEN
        assertNull(entity.getActivityStatusCode());
        assertNull(entity.getRiskRatingCode());
        assertNull(entity.getContractPhaseCode());
        verifyNoInteractions(activityStatusCodeRepository);
        verifyNoInteractions(riskRatingCodeRepository);
        verifyNoInteractions(contractPhaseCodeRepository);
    }

    @Test
    void testDeleteActivities_Success() {
        // GIVEN
        String fiscalGuid = "456e7890-e89b-12d3-a456-426614174001";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        ActivityEntity activityEntity = new ActivityEntity();
        activityEntity.setActivityGuid(UUID.fromString(activityGuid));

        when(activityRepository.findByProjectPlanFiscalGuid(UUID.fromString(fiscalGuid)))
                .thenReturn(List.of(activityEntity));

        // WHEN
        activityService.deleteActivities(fiscalGuid, true);

        // THEN
        verify(fileAttachmentService).deleteAttachmentsBySourceObject(activityGuid, true);
        verify(activityBoundaryService).deleteActivityBoundaries(activityGuid, true);
        verify(activityRepository).delete(activityEntity);
    }

    @Test
    void testDeleteActivities_NoFileDeletion() {
        // GIVEN
        String fiscalGuid = "456e7890-e89b-12d3-a456-426614174001";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        ActivityEntity activityEntity = new ActivityEntity();
        activityEntity.setActivityGuid(UUID.fromString(activityGuid));

        when(activityRepository.findByProjectPlanFiscalGuid(UUID.fromString(fiscalGuid)))
                .thenReturn(List.of(activityEntity));

        // WHEN
        activityService.deleteActivities(fiscalGuid, false);

        // THEN
        verify(fileAttachmentService).deleteAttachmentsBySourceObject(activityGuid, false);
        verify(activityBoundaryService).deleteActivityBoundaries(activityGuid, false);
        verify(activityRepository).delete(activityEntity);
    }
}