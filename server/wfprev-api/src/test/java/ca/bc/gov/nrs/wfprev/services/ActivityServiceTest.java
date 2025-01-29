package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.ActivityResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.*;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.data.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActivityServiceTest {

    private ActivityRepository activityRepository;
    private ActivityResourceAssembler activityResourceAssembler;
    private ProjectFiscalRepository projectFiscalRepository;
    private ProjectFiscalService projectFiscalService;
    private ActivityStatusCodeRepository activityStatusCodeRepository;
    private ContractPhaseCodeRepository contractPhaseCodeRepository;
    private RiskRatingCodeRepository riskRatingCodeRepository;
    private ActivityService activityService;

    @BeforeEach
    void setup() {
        activityRepository = mock(ActivityRepository.class);
        activityResourceAssembler = mock(ActivityResourceAssembler.class);
        projectFiscalRepository = mock(ProjectFiscalRepository.class);
        projectFiscalService = mock(ProjectFiscalService.class);
        activityStatusCodeRepository = mock(ActivityStatusCodeRepository.class);
        contractPhaseCodeRepository = mock(ContractPhaseCodeRepository.class);
        riskRatingCodeRepository = mock(RiskRatingCodeRepository.class);

        activityService = new ActivityService(
                activityRepository,
                activityResourceAssembler,
                projectFiscalRepository,
                projectFiscalService,
                activityStatusCodeRepository,
                contractPhaseCodeRepository,
                riskRatingCodeRepository
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

        ProjectFiscalModel projectFiscalModel = new ProjectFiscalModel();
        projectFiscalModel.setProjectGuid(projectGuid);
        projectFiscalModel.setProjectPlanFiscalGuid(fiscalGuid);

        ProjectFiscalEntity projectFiscalEntity = ProjectFiscalEntity.builder()
                .projectPlanFiscalGuid(UUID.fromString(fiscalGuid))
                .build();

        ActivityEntity activityEntity = new ActivityEntity();
        ActivityEntity savedEntity = new ActivityEntity();
        ActivityModel savedModel = new ActivityModel();

        when(projectFiscalService.getProjectFiscal(fiscalGuid)).thenReturn(projectFiscalModel);
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
        activityService.deleteActivity(projectGuid, fiscalGuid, activityGuid);

        // THEN
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

        ProjectFiscalModel projectFiscalModel = new ProjectFiscalModel();
        projectFiscalModel.setProjectGuid("different-project-guid");

        when(projectFiscalService.getProjectFiscal(fiscalGuid))
                .thenReturn(projectFiscalModel);

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
}