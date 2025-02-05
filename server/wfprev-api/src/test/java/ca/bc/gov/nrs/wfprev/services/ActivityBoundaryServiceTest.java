package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.ActivityBoundaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

    @BeforeEach
    void setup() {
        activityBoundaryRepository = mock(ActivityBoundaryRepository.class);
        activityBoundaryResourceAssembler = mock(ActivityBoundaryResourceAssembler.class);
        activityRepository = mock(ActivityRepository.class);
        activityService = mock(ActivityService.class);

        activityBoundaryService = new ActivityBoundaryService(
                activityBoundaryRepository,
                activityBoundaryResourceAssembler,
                activityRepository,
                activityService
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
    void testCreateActivityBoundary_Success() {
        // GIVEN
        String activityGuid = "789e1234-e89b-12d3-a456-426614174002";

        ActivityBoundaryModel boundaryModel = new ActivityBoundaryModel();
        boundaryModel.setBoundarySizeHa(new BigDecimal("50.0"));

        ActivityEntity activityEntity = new ActivityEntity();
        activityEntity.setActivityGuid(UUID.fromString(activityGuid));

        ActivityBoundaryEntity boundaryEntity = new ActivityBoundaryEntity();

        when(activityService.getActivity(any(), any(), eq(activityGuid))).thenReturn(null);
        when(activityRepository.findById(UUID.fromString(activityGuid)))
                .thenReturn(Optional.of(activityEntity));
        when(activityBoundaryResourceAssembler.toEntity(eq(boundaryModel)))
                .thenReturn(boundaryEntity);
        when(activityBoundaryRepository.save(any(ActivityBoundaryEntity.class)))
                .thenReturn(boundaryEntity);
        when(activityBoundaryResourceAssembler.toModel(boundaryEntity))
                .thenReturn(boundaryModel);

        // WHEN
        ActivityBoundaryModel result = activityBoundaryService.createActivityBoundary(
                "project-guid", "fiscal-guid", activityGuid, boundaryModel);

        // THEN
        assertNotNull(result);
        verify(activityBoundaryRepository).save(any(ActivityBoundaryEntity.class));
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

        when(activityService.getActivity(any(), any(), eq(activityGuid))).thenReturn(null);
        when(activityBoundaryRepository.findByActivityBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                activityBoundaryService.updateActivityBoundary("project-guid", "fiscal-guid", activityGuid, boundaryModel));
        assertTrue(thrown.getMessage().contains("Activity Boundary not found"));
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
        verify(activityBoundaryRepository).deleteByActivityGuid(UUID.fromString(boundaryGuid));
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
}
