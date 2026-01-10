package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.ActivityBoundaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

class ActivityBoundaryServiceTest {

    private ActivityBoundaryRepository activityBoundaryRepository;
    private ActivityBoundaryResourceAssembler activityBoundaryResourceAssembler;
    private ActivityRepository activityRepository;
    private ActivityService activityService;
    private ActivityBoundaryService activityBoundaryService;
    private ProjectBoundaryService projectBoundaryService;
    private Validator validator;

    @BeforeEach
    void setup() {
        activityBoundaryRepository = mock(ActivityBoundaryRepository.class);
        activityBoundaryResourceAssembler = mock(ActivityBoundaryResourceAssembler.class);
        activityRepository = mock(ActivityRepository.class);
        activityService = mock(ActivityService.class);
        projectBoundaryService = mock(ProjectBoundaryService.class);
        validator = mock(Validator.class);

        activityBoundaryService = new ActivityBoundaryService(
                activityBoundaryRepository,
                activityBoundaryResourceAssembler,
                activityRepository,
                activityService,
                projectBoundaryService,
                validator
        );
    }

    @Test
    void testGetAllActivityBoundaries_Success() throws ServiceException {
        // GIVEN
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";

        List<ActivityBoundaryEntity> boundaryEntities = List.of(
                new ActivityBoundaryEntity(), new ActivityBoundaryEntity()
        );

        when(activityService.getActivity(any(), any(), eq(activityGuid))).thenReturn(null);
        when(activityBoundaryRepository.findByActivityGuid(UUID.fromString(activityGuid)))
                .thenReturn(boundaryEntities);
        when(activityBoundaryResourceAssembler.toModel(any(ActivityBoundaryEntity.class)))
                .thenReturn(new ActivityBoundaryModel());

        // WHEN
        CollectionModel<ActivityBoundaryModel> result = activityBoundaryService.getAllActivityBoundaries(
                "project-guid", "fiscal-guid", activityGuid);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(activityBoundaryRepository).findByActivityGuid(UUID.fromString(activityGuid));
    }

    @Test
    void testGetAllActivityBoundaries_InvalidGuidFormat() {
        // GIVEN an invalid activityGuid format
        String projectGuid = "project-guid";
        String fiscalGuid = "fiscal-guid";
        String invalidActivityGuid = "invalid-uuid"; // Not a valid UUID

        // WHEN / THEN
        ServiceException thrown = assertThrows(ServiceException.class, () ->
                activityBoundaryService.getAllActivityBoundaries(projectGuid, fiscalGuid, invalidActivityGuid));

        assertTrue(thrown.getMessage().contains("Invalid GUID format"));
    }

    @Test
    void testCreateActivityBoundary_Success() {
        // GIVEN
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";

        ActivityBoundaryModel boundaryModel = new ActivityBoundaryModel();
        boundaryModel.setActivityGuid(activityGuid);
        boundaryModel.setBoundarySizeHa(new BigDecimal("50.0"));

        ActivityEntity activityEntity = new ActivityEntity();
        activityEntity.setActivityGuid(UUID.fromString(activityGuid));

        ActivityBoundaryEntity boundaryEntity = new ActivityBoundaryEntity();
        boundaryEntity.setActivityGuid(UUID.fromString(activityGuid));

        when(activityService.getActivity(any(), any(), eq(activityGuid))).thenReturn(new ActivityModel());
        when(activityRepository.findById(UUID.fromString(activityGuid))).thenReturn(Optional.of(activityEntity));
        when(activityBoundaryResourceAssembler.toEntity(any(ActivityBoundaryModel.class)))
                .thenReturn(boundaryEntity); // ✅ Ensures it returns a valid entity
        when(activityBoundaryRepository.save(any(ActivityBoundaryEntity.class)))
                .thenReturn(boundaryEntity);
        when(activityBoundaryResourceAssembler.toModel(any(ActivityBoundaryEntity.class)))
                .thenReturn(boundaryModel); // ✅ Corrected to match expected return type

        // WHEN
        ActivityBoundaryModel result = activityBoundaryService.createActivityBoundary(
                "project-guid", "fiscal-guid", activityGuid, boundaryModel);

        // THEN
        assertNotNull(result);
        verify(activityBoundaryRepository).save(any(ActivityBoundaryEntity.class));
    }

    @Test
    void testCreateActivityBoundary_NullEntity() {
        // GIVEN
        String projectGuid = "project-guid";
        String fiscalGuid = "fiscal-guid";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002"; // Valid UUID
        ActivityBoundaryModel resource = new ActivityBoundaryModel();

        when(activityService.getActivity(any(), any(), eq(activityGuid))).thenReturn(new ActivityModel());
        when(activityRepository.findById(UUID.fromString(activityGuid)))
                .thenReturn(Optional.of(new ActivityEntity()));
        when(activityBoundaryResourceAssembler.toEntity(resource)).thenReturn(null); // Simulate failure

        // WHEN / THEN
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                activityBoundaryService.createActivityBoundary(projectGuid, fiscalGuid, activityGuid, resource));

        assertTrue(thrown.getMessage().contains("ActivityBoundaryModel resource to be created cannot be null"));
    }


    @Test
    void testCreateActivityBoundary_InvalidGuidFormat() {
        // GIVEN
        String projectGuid = "project-guid";
        String fiscalGuid = "fiscal-guid";
        String invalidActivityGuid = "invalid-uuid"; // Not a valid UUID
        ActivityBoundaryModel resource = new ActivityBoundaryModel();

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () ->
                activityBoundaryService.createActivityBoundary(projectGuid, fiscalGuid, invalidActivityGuid, resource));
    }

    @Test
    void testCreateActivityBoundary_ConstraintViolationException() {
        // GIVEN
        String projectGuid = "project-guid";
        String fiscalGuid = "fiscal-guid";
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
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        String boundaryGuid = "101e7890-e89b-12d3-a456-426614174003";

        ActivityBoundaryModel boundaryModel = new ActivityBoundaryModel();
        boundaryModel.setActivityBoundaryGuid(boundaryGuid);
        boundaryModel.setActivityGuid(activityGuid);

        ActivityBoundaryEntity existingEntity = new ActivityBoundaryEntity();
        existingEntity.setActivityGuid(UUID.fromString(activityGuid));

        when(activityService.getActivity(any(), any(), eq(activityGuid))).thenReturn(null);
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
                "project-guid", "fiscal-guid", activityGuid, boundaryModel);

        // THEN
        assertNotNull(result);
        verify(activityBoundaryRepository).saveAndFlush(any(ActivityBoundaryEntity.class));
    }

    @Test
    void testUpdateActivityBoundary_NotFound() {
        // GIVEN
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        String boundaryGuid = "101e7890-e89b-12d3-a456-426614174003";

        ActivityBoundaryModel boundaryModel = new ActivityBoundaryModel();
        boundaryModel.setActivityBoundaryGuid(boundaryGuid);
        boundaryModel.setActivityGuid(activityGuid); // ✅ Ensure this is set to avoid IllegalArgumentException

        when(activityService.getActivity(any(), any(), eq(activityGuid))).thenReturn(new ActivityModel());
        when(activityBoundaryRepository.findByActivityBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.empty()); // Ensure it triggers EntityNotFoundException

        // WHEN / THEN
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                activityBoundaryService.updateActivityBoundary("project-guid", "fiscal-guid", activityGuid, boundaryModel));

        assertTrue(thrown.getMessage().contains("Activity Boundary not found"));
    }

    @Test
    void testUpdateActivityBoundary_ConstraintViolationException() {
        // GIVEN
        String projectGuid = "project-guid";
        String fiscalGuid = "fiscal-guid";
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
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        String boundaryGuid = "101e7890-e89b-12d3-a456-426614174003";

        ActivityBoundaryEntity boundaryEntity = new ActivityBoundaryEntity();
        boundaryEntity.setActivityGuid(UUID.fromString(activityGuid));

        when(activityService.getActivity(any(), any(), eq(activityGuid))).thenReturn(null);
        when(activityBoundaryRepository.findByActivityBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.of(boundaryEntity));

        // WHEN
        activityBoundaryService.deleteActivityBoundary("project-guid", "fiscal-guid", activityGuid, boundaryGuid);

        // THEN
        verify(activityBoundaryRepository).deleteByActivityBoundaryGuid(UUID.fromString(boundaryGuid));
    }

    @Test
    void testDeleteActivityBoundary_NotFound() {
        // GIVEN
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        String boundaryGuid = "101e7890-e89b-12d3-a456-426614174003";

        when(activityService.getActivity(any(), any(), eq(activityGuid))).thenReturn(null);
        when(activityBoundaryRepository.findByActivityBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                activityBoundaryService.deleteActivityBoundary("project-guid", "fiscal-guid", activityGuid, boundaryGuid));
        assertTrue(thrown.getMessage().contains("Activity Boundary not found"));
    }

    @Test
    void testGetActivityBoundary_Success() {
        // GIVEN
        String projectGuid = "project-guid";
        String fiscalGuid = "fiscal-guid";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        String boundaryGuid = "101e7890-e89b-12d3-a456-426614174003";

        ActivityBoundaryEntity boundaryEntity = new ActivityBoundaryEntity();
        boundaryEntity.setActivityGuid(UUID.fromString(activityGuid));

        ActivityBoundaryModel boundaryModel = new ActivityBoundaryModel();

        when(activityService.getActivity(projectGuid, fiscalGuid, activityGuid)).thenReturn(new ActivityModel());
        when(activityBoundaryRepository.findByActivityBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.of(boundaryEntity));
        when(activityBoundaryResourceAssembler.toModel(boundaryEntity)).thenReturn(boundaryModel);

        // WHEN
        ActivityBoundaryModel result = activityBoundaryService.getActivityBoundary(projectGuid, fiscalGuid, activityGuid, boundaryGuid);

        // THEN
        assertNotNull(result);
        verify(activityBoundaryRepository).findByActivityBoundaryGuid(UUID.fromString(boundaryGuid));
        verify(activityBoundaryResourceAssembler).toModel(boundaryEntity);
    }

    @Test
    void testGetActivityBoundary_NotFound() {
        // GIVEN
        String projectGuid = "project-guid";
        String fiscalGuid = "fiscal-guid";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        String boundaryGuid = "101e7890-e89b-12d3-a456-426614174003";

        when(activityService.getActivity(projectGuid, fiscalGuid, activityGuid)).thenReturn(new ActivityModel());
        when(activityBoundaryRepository.findByActivityBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.empty()); // Simulate not found

        // WHEN / THEN
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                activityBoundaryService.getActivityBoundary(projectGuid, fiscalGuid, activityGuid, boundaryGuid));

        assertTrue(thrown.getMessage().contains("Activity Boundary not found"));
    }

    @Test
    void testGetActivityBoundary_BoundaryDoesNotBelongToActivity() {
        // GIVEN
        String projectGuid = "project-guid";
        String fiscalGuid = "fiscal-guid";
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";
        String boundaryGuid = "101e7890-e89b-12d3-a456-426614174003";
        String differentActivityGuid = "555e1234-e89b-12d3-a456-426614174999"; // Different activity

        ActivityBoundaryEntity boundaryEntity = new ActivityBoundaryEntity();
        boundaryEntity.setActivityGuid(UUID.fromString(differentActivityGuid)); // Belongs to another activity

        when(activityService.getActivity(projectGuid, fiscalGuid, activityGuid)).thenReturn(new ActivityModel());
        when(activityBoundaryRepository.findByActivityBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.of(boundaryEntity));

        // WHEN / THEN
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                activityBoundaryService.getActivityBoundary(projectGuid, fiscalGuid, activityGuid, boundaryGuid));

        assertTrue(thrown.getMessage().contains("does not belong to Activity"));
    }

    @Test
    void testDeleteActivityBoundaries_Success() {
        // GIVEN
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";

        // WHEN
        activityBoundaryService.deleteActivityBoundaries(activityGuid);

        // THEN
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
