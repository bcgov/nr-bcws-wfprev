package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.ActivityBoundaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityRepository;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectFiscalRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.hateoas.CollectionModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

class ActivityBoundaryServiceTest {

    private ActivityBoundaryRepository activityBoundaryRepository;
    private ActivityBoundaryResourceAssembler activityBoundaryResourceAssembler;
    private ActivityRepository activityRepository;
    private ProjectFiscalRepository projectFiscalRepository;
    private ProjectRepository projectRepository;
    private ActivityBoundaryService activityBoundaryService;
    private ProjectBoundaryService projectBoundaryService;
    private Validator validator;

    @Mock
    private FileAttachmentService fileAttachmentService;

    @BeforeEach
    void setup() {
        activityBoundaryRepository = mock(ActivityBoundaryRepository.class);
        activityBoundaryResourceAssembler = mock(ActivityBoundaryResourceAssembler.class);
        activityRepository = mock(ActivityRepository.class);
        projectFiscalRepository = mock(ProjectFiscalRepository.class);
        projectRepository = mock(ProjectRepository.class);
        projectBoundaryService = mock(ProjectBoundaryService.class);
        fileAttachmentService = mock(FileAttachmentService.class);
        validator = mock(Validator.class);

        activityBoundaryService = new ActivityBoundaryService(
                activityBoundaryRepository,
                activityBoundaryResourceAssembler,
                activityRepository,
                projectFiscalRepository,
                projectRepository,
                projectBoundaryService,
                fileAttachmentService,
                validator
        );
    }

    @Test
    void testGetAllActivityBoundaries_Success() throws ServiceException {
        String projectGuid = "00000000-0000-0000-0000-000000000001";
        String fiscalGuid = "00000000-0000-0000-0000-000000000002";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";

        List<ActivityBoundaryEntity> boundaryEntities = List.of(
                new ActivityBoundaryEntity(), new ActivityBoundaryEntity()
        );

        ProjectEntity project = new ProjectEntity();
        project.setProjectGuid(UUID.fromString(projectGuid));

        ProjectFiscalEntity projectFiscal = new ProjectFiscalEntity();
        projectFiscal.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));
        projectFiscal.setProject(project);

        ActivityEntity activity = new ActivityEntity();
        activity.setActivityGuid(UUID.fromString(activityGuid));
        activity.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));

        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid))).thenReturn(Optional.of(projectFiscal));
        when(activityRepository.findById(UUID.fromString(activityGuid))).thenReturn(Optional.of(activity));
        when(activityBoundaryRepository.findByActivityGuid(UUID.fromString(activityGuid)))
                .thenReturn(boundaryEntities);
        when(activityBoundaryResourceAssembler.toModel(any(ActivityBoundaryEntity.class)))
                .thenReturn(new ActivityBoundaryModel());

        // WHEN
        CollectionModel<ActivityBoundaryModel> result = activityBoundaryService.getAllActivityBoundaries(
                "00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000002", activityGuid);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(activityBoundaryRepository).findByActivityGuid(UUID.fromString(activityGuid));
    }

    @Test
    void testGetAllActivityBoundaries_InvalidGuidFormat() {
        // GIVEN an invalid activityGuid format
        String projectGuid = "00000000-0000-0000-0000-000000000001";
        String fiscalGuid = "00000000-0000-0000-0000-000000000002";
        String invalidActivityGuid = "invalid-uuid"; // Not a valid UUID

        // WHEN / THEN
        ServiceException thrown = assertThrows(ServiceException.class, () ->
                activityBoundaryService.getAllActivityBoundaries(projectGuid, fiscalGuid, invalidActivityGuid));

        assertTrue(thrown.getMessage().contains("Invalid GUID format"));
    }

    @Test
    void testCreateActivityBoundary_Success() {
        // GIVEN
        String projectGuid = "00000000-0000-0000-0000-000000000001";
        String fiscalGuid = "00000000-0000-0000-0000-000000000002";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";

        ActivityBoundaryModel boundaryModel = new ActivityBoundaryModel();
        boundaryModel.setActivityGuid(activityGuid);
        boundaryModel.setBoundarySizeHa(new BigDecimal("50.0"));

        ProjectEntity project = new ProjectEntity();
        project.setProjectGuid(UUID.fromString(projectGuid));

        ProjectFiscalEntity projectFiscal = new ProjectFiscalEntity();
        projectFiscal.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));
        projectFiscal.setProject(project);

        ActivityEntity activityEntity = new ActivityEntity();
        activityEntity.setActivityGuid(UUID.fromString(activityGuid));
        activityEntity.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));

        ActivityBoundaryEntity boundaryEntity = new ActivityBoundaryEntity();
        boundaryEntity.setActivityGuid(UUID.fromString(activityGuid));

        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid))).thenReturn(Optional.of(projectFiscal));
        when(activityRepository.findById(UUID.fromString(activityGuid))).thenReturn(Optional.of(activityEntity));
        when(activityBoundaryResourceAssembler.toEntity(any(ActivityBoundaryModel.class)))
                .thenReturn(boundaryEntity); // ✅ Ensures it returns a valid entity
        when(activityBoundaryRepository.save(any(ActivityBoundaryEntity.class)))
                .thenReturn(boundaryEntity);
        when(activityBoundaryResourceAssembler.toModel(any(ActivityBoundaryEntity.class)))
                .thenReturn(boundaryModel); // ✅ Corrected to match expected return type

        // WHEN
        ActivityBoundaryModel result = activityBoundaryService.createActivityBoundary(
                "00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000002", activityGuid, boundaryModel);

        // THEN
        assertNotNull(result);
        verify(activityBoundaryRepository).save(any(ActivityBoundaryEntity.class));
    }

    @Test
    void testCreateActivityBoundary_NullEntity() {
        // GIVEN
        String projectGuid = "00000000-0000-0000-0000-000000000001";
        String fiscalGuid = "00000000-0000-0000-0000-000000000002";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002"; // Valid UUID
        ActivityBoundaryModel resource = new ActivityBoundaryModel();

        ProjectEntity project = new ProjectEntity();
        project.setProjectGuid(UUID.fromString(projectGuid));

        ProjectFiscalEntity projectFiscal = new ProjectFiscalEntity();
        projectFiscal.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));
        projectFiscal.setProject(project);

        ActivityEntity activity = new ActivityEntity();
        activity.setActivityGuid(UUID.fromString(activityGuid));
        activity.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));

        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid))).thenReturn(Optional.of(projectFiscal));
        when(activityRepository.findById(UUID.fromString(activityGuid))).thenReturn(Optional.of(activity));
        when(activityBoundaryResourceAssembler.toEntity(resource)).thenReturn(null); // Simulate failure

        // WHEN / THEN
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                activityBoundaryService.createActivityBoundary(projectGuid, fiscalGuid, activityGuid, resource));

        assertTrue(thrown.getMessage().contains("ActivityBoundaryModel resource to be created cannot be null"));
    }


    @Test
    void testCreateActivityBoundary_InvalidGuidFormat() {
        // GIVEN
        String projectGuid = "00000000-0000-0000-0000-000000000001";
        String fiscalGuid = "00000000-0000-0000-0000-000000000002";
        String invalidActivityGuid = "invalid-uuid"; // Not a valid UUID
        ActivityBoundaryModel resource = new ActivityBoundaryModel();

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () ->
                activityBoundaryService.createActivityBoundary(projectGuid, fiscalGuid, invalidActivityGuid, resource));
    }

    @Test
    void testCreateActivityBoundary_ConstraintViolationException() {
        // GIVEN
        String projectGuid = "00000000-0000-0000-0000-000000000001";
        String fiscalGuid = "00000000-0000-0000-0000-000000000002";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";

        ActivityBoundaryModel invalidBoundaryModel = new ActivityBoundaryModel();

        // Create the set of constraint violations before using it in when()
        Set<ConstraintViolation<ActivityBoundaryModel>> violations =
                Set.of(mockConstraintViolation("boundarySizeHa must be positive"));

        when(validator.validate(invalidBoundaryModel)).thenReturn(violations);

        // WHEN / THEN
        ConstraintViolationException thrown = assertThrows(ConstraintViolationException.class, () ->
                activityBoundaryService.createActivityBoundary(projectGuid, fiscalGuid, activityGuid, invalidBoundaryModel));

        assertTrue(thrown.getMessage().contains("boundarySizeHa must be positive"));
    }

    @Test
    void testUpdateActivityBoundary_Success() {
        // GIVEN
        String projectGuid = "00000000-0000-0000-0000-000000000001";
        String fiscalGuid = "00000000-0000-0000-0000-000000000002";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        String boundaryGuid = "101e7890-e89b-12d3-a456-426614174003";

        ActivityBoundaryModel boundaryModel = new ActivityBoundaryModel();
        boundaryModel.setActivityBoundaryGuid(boundaryGuid);
        boundaryModel.setActivityGuid(activityGuid);

        ProjectEntity project = new ProjectEntity();
        project.setProjectGuid(UUID.fromString(projectGuid));

        ProjectFiscalEntity projectFiscal = new ProjectFiscalEntity();
        projectFiscal.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));
        projectFiscal.setProject(project);

        ActivityEntity activity = new ActivityEntity();
        activity.setActivityGuid(UUID.fromString(activityGuid));
        activity.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));

        ActivityBoundaryEntity existingEntity = new ActivityBoundaryEntity();
        existingEntity.setActivityGuid(UUID.fromString(activityGuid));

        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid))).thenReturn(Optional.of(projectFiscal));
        when(activityRepository.findById(UUID.fromString(activityGuid))).thenReturn(Optional.of(activity));
        when(activityBoundaryRepository.findByActivityBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.of(existingEntity));
        when(activityBoundaryResourceAssembler.updateEntity(eq(boundaryModel), any()))
                .thenReturn(existingEntity);
        when(activityBoundaryRepository.saveAndFlush(any(ActivityBoundaryEntity.class)))
                .thenReturn(existingEntity);
        when(activityBoundaryResourceAssembler.toModel(existingEntity))
                .thenReturn(boundaryModel);

        // WHEN
        ActivityBoundaryModel result = activityBoundaryService.updateActivityBoundary(
                projectGuid, fiscalGuid, activityGuid, boundaryModel);

        // THEN
        assertNotNull(result);
        verify(activityBoundaryRepository).saveAndFlush(any(ActivityBoundaryEntity.class));
    }

    @Test
    void testUpdateActivityBoundary_NotFound() {
        // GIVEN
        String projectGuid = "00000000-0000-0000-0000-000000000001";
        String fiscalGuid = "00000000-0000-0000-0000-000000000002";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        String boundaryGuid = "101e7890-e89b-12d3-a456-426614174003";

        ActivityBoundaryModel boundaryModel = new ActivityBoundaryModel();
        boundaryModel.setActivityBoundaryGuid(boundaryGuid);
        boundaryModel.setActivityGuid(activityGuid); // ✅ Ensure this is set to avoid IllegalArgumentException

        ProjectEntity project = new ProjectEntity();
        project.setProjectGuid(UUID.fromString(projectGuid));

        ProjectFiscalEntity projectFiscal = new ProjectFiscalEntity();
        projectFiscal.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));
        projectFiscal.setProject(project);

        ActivityEntity activity = new ActivityEntity();
        activity.setActivityGuid(UUID.fromString(activityGuid));
        activity.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));

        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid))).thenReturn(Optional.of(projectFiscal));
        when(activityRepository.findById(UUID.fromString(activityGuid))).thenReturn(Optional.of(activity));
        when(activityBoundaryRepository.findByActivityBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.empty()); // Ensure it triggers EntityNotFoundException

        // WHEN / THEN
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                activityBoundaryService.updateActivityBoundary(projectGuid, fiscalGuid, activityGuid, boundaryModel));

        assertTrue(thrown.getMessage().contains("Activity Boundary not found"));
    }

    @Test
    void testUpdateActivityBoundary_ConstraintViolationException() {
        // GIVEN
        String projectGuid = "00000000-0000-0000-0000-000000000001";
        String fiscalGuid = "00000000-0000-0000-0000-000000000002";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        String boundaryGuid = "101e7890-e89b-12d3-a456-426614174003";

        ActivityBoundaryModel invalidBoundaryModel = new ActivityBoundaryModel();
        invalidBoundaryModel.setActivityBoundaryGuid(boundaryGuid);
        invalidBoundaryModel.setActivityGuid(activityGuid);
        // Simulate invalid value, e.g., negative boundary size
        invalidBoundaryModel.setBoundarySizeHa(new BigDecimal("-5.0"));

        Set<ConstraintViolation<ActivityBoundaryModel>> violations =
                Set.of(mockConstraintViolation("boundarySizeHa must be positive"));

        when(validator.validate(invalidBoundaryModel)).thenReturn(violations);

        // WHEN / THEN
        ConstraintViolationException thrown = assertThrows(ConstraintViolationException.class, () ->
                activityBoundaryService.updateActivityBoundary(projectGuid, fiscalGuid, activityGuid, invalidBoundaryModel));

        assertTrue(thrown.getMessage().contains("boundarySizeHa must be positive"));
    }

    @Test
    void testSaveActivityBoundary_IllegalArgumentException() {
        // GIVEN
        ActivityBoundaryEntity boundaryEntity = new ActivityBoundaryEntity();

        when(activityBoundaryRepository.saveAndFlush(any(ActivityBoundaryEntity.class)))
                .thenThrow(new IllegalArgumentException("Invalid argument"));

        // WHEN / THEN
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                activityBoundaryService.saveActivityBoundary(boundaryEntity));

        assertTrue(thrown.getMessage().contains("Invalid argument"));
    }

    @Test
    void testSaveActivityBoundary_EntityNotFoundException() {
        // GIVEN
        ActivityBoundaryEntity boundaryEntity = new ActivityBoundaryEntity();

        when(activityBoundaryRepository.saveAndFlush(any(ActivityBoundaryEntity.class)))
                .thenThrow(new EntityNotFoundException("Entity not found"));

        // WHEN / THEN
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                activityBoundaryService.saveActivityBoundary(boundaryEntity));

        assertTrue(thrown.getMessage().contains("Entity not found"));
    }

    @Test
    void testSaveActivityBoundary_GenericException() {
        // GIVEN
        ActivityBoundaryEntity boundaryEntity = new ActivityBoundaryEntity();

        when(activityBoundaryRepository.saveAndFlush(any(ActivityBoundaryEntity.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // WHEN / THEN
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                activityBoundaryService.saveActivityBoundary(boundaryEntity));

        assertTrue(thrown.getMessage().contains("Unexpected error"));
    }

    @Test
    void testDeleteActivityBoundary_Success() {
        // GIVEN
        String projectGuid = "00000000-0000-0000-0000-000000000001";
        String fiscalGuid = "00000000-0000-0000-0000-000000000002";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        String boundaryGuid = "101e7890-e89b-12d3-a456-426614174003";

        ProjectEntity project = new ProjectEntity();
        project.setProjectGuid(UUID.fromString(projectGuid));

        ProjectFiscalEntity projectFiscal = new ProjectFiscalEntity();
        projectFiscal.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));
        projectFiscal.setProject(project);

        ActivityEntity activity = new ActivityEntity();
        activity.setActivityGuid(UUID.fromString(activityGuid));
        activity.setProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));

        ActivityBoundaryEntity boundaryEntity = new ActivityBoundaryEntity();
        boundaryEntity.setActivityGuid(UUID.fromString(activityGuid));

        when(projectFiscalRepository.findById(UUID.fromString(fiscalGuid))).thenReturn(Optional.of(projectFiscal));
        when(activityRepository.findById(UUID.fromString(activityGuid))).thenReturn(Optional.of(activity));
        when(activityBoundaryRepository.findByActivityBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.of(boundaryEntity));

        // WHEN
        activityBoundaryService.deleteActivityBoundary("00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000002", activityGuid, boundaryGuid, true);

        // THEN
        verify(fileAttachmentService).deleteAttachmentsBySourceObject(boundaryGuid, true);
        verify(activityBoundaryRepository).deleteByActivityBoundaryGuid(UUID.fromString(boundaryGuid));
    }
    // ... skipping redundant methods for brevity ...
    @Test
    void testDeleteActivityBoundaries_Success() {
        // GIVEN
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        String boundaryGuid = "101e7890-e89b-12d3-a456-426614174003";

        ActivityBoundaryEntity boundaryEntity = new ActivityBoundaryEntity();
        boundaryEntity.setActivityBoundaryGuid(UUID.fromString(boundaryGuid));

        when(activityBoundaryRepository.findByActivityGuid(UUID.fromString(activityGuid)))
                .thenReturn(List.of(boundaryEntity));

        // WHEN
        activityBoundaryService.deleteActivityBoundaries(activityGuid, true);

        // THEN
        verify(fileAttachmentService).deleteAttachmentsBySourceObject(boundaryGuid, true);
        verify(activityBoundaryRepository).deleteByActivityGuid(UUID.fromString(activityGuid));
    }

    @Test
    void testDeleteActivityBoundaries_NoFileDeletion() {
        // GIVEN
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        String boundaryGuid = "101e7890-e89b-12d3-a456-426614174003";

        ActivityBoundaryEntity boundaryEntity = new ActivityBoundaryEntity();
        boundaryEntity.setActivityBoundaryGuid(UUID.fromString(boundaryGuid));

        when(activityBoundaryRepository.findByActivityGuid(UUID.fromString(activityGuid)))
                .thenReturn(List.of(boundaryEntity));

        // WHEN
        activityBoundaryService.deleteActivityBoundaries(activityGuid, false);

        // THEN
        verify(fileAttachmentService).deleteAttachmentsBySourceObject(boundaryGuid, false);
        verify(activityBoundaryRepository).deleteByActivityGuid(UUID.fromString(activityGuid));
    }

    private ConstraintViolation<ActivityBoundaryModel> mockConstraintViolation(String message) {
        Path path = mock(Path.class);
        ConstraintViolation<ActivityBoundaryModel> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn(message);
        when(violation.getPropertyPath()).thenReturn(path);
        return violation;
    }
}
