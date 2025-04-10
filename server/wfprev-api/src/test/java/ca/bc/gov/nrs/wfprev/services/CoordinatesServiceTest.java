package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.hateoas.CollectionModel;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoordinatesServiceTest {

    private CoordinatesService coordinatesService;
    private ProjectService projectService;
    private ProjectBoundaryService projectBoundaryService;
    private ProjectFiscalService projectFiscalService;
    private ActivityService activityService;
    private ActivityBoundaryService activityBoundaryService;

    @BeforeEach
    void setUp() {
        projectService = mock(ProjectService.class);
        projectBoundaryService = mock(ProjectBoundaryService.class);
        projectFiscalService = mock(ProjectFiscalService.class);
        activityService = mock(ActivityService.class);
        activityBoundaryService = mock(ActivityBoundaryService.class);

        coordinatesService = new CoordinatesService(
                projectService,
                projectBoundaryService,
                projectFiscalService,
                activityService,
                activityBoundaryService
        );
    }

    @Test
    void testUpdateProjectCoordinates_WithValidPolygons() {
        String projectGuid = UUID.randomUUID().toString();

        ProjectModel projectModel = new ProjectModel();
        projectModel.setProjectGuid(projectGuid);

        when(projectService.getProjectById(projectGuid)).thenReturn(projectModel);

        // Create a sample MultiPolygon
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coords = new Coordinate[] {
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0),
                new Coordinate(0, 0)
        };
        LinearRing ring = factory.createLinearRing(coords);
        Polygon polygon = factory.createPolygon(ring);
        MultiPolygon multiPolygon = factory.createMultiPolygon(new Polygon[] { polygon });

        CoordinatesService spyService = spy(coordinatesService);
        doReturn(List.of(multiPolygon)).when(spyService).getAllPolygonsForProject(projectGuid);

        // Act
        spyService.updateProjectCoordinates(projectGuid);

        // Assert: verify updateProject was called
        verify(projectService).updateProject(projectModel);

        // Check that coordinates were set
        BigDecimal expected = new BigDecimal("0.5");
        assertEquals(expected, projectModel.getLatitude());
        assertEquals(expected, projectModel.getLongitude());
    }


    @Test
    void testUpdateProjectCoordinates_NoPolygons() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();

        ProjectModel projectModel = new ProjectModel();
        projectModel.setProjectGuid(projectGuid);

        when(projectService.getProjectById(projectGuid)).thenReturn(projectModel);

        CoordinatesService spyService = spy(coordinatesService);
        doReturn(Collections.emptyList()).when(spyService).getAllPolygonsForProject(projectGuid);

        // Act
        spyService.updateProjectCoordinates(projectGuid);

        // Verify the updateProject method was NOT called
        verify(projectService, never()).updateProject(any(ProjectModel.class));
    }

    @Test
    void testUpdateProjectCoordinates_NoProject() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();

        ProjectModel projectModel = new ProjectModel();
        projectModel.setProjectGuid(projectGuid);

        when(projectService.getProjectById(projectGuid)).thenReturn(null);

        CoordinatesService spyService = spy(coordinatesService);
        doReturn(Collections.emptyList()).when(spyService).getAllPolygonsForProject(projectGuid);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            spyService.updateProjectCoordinates(projectGuid);
        });

        // Verify the updateProject method was NOT called
        verify(projectService, never()).updateProject(any(ProjectModel.class));
    }

    @Test
    void testGetAllPolygonsForProject_ReturnsPolygons() {
        String projectGuid = UUID.randomUUID().toString();

        GeometryFactory factory = new GeometryFactory();
        MultiPolygon mockPolygon = factory.createMultiPolygon(null);

        ProjectBoundaryModel boundaryModel = mock(ProjectBoundaryModel.class);
        when(boundaryModel.getBoundaryGeometry()).thenReturn(mockPolygon);
        CollectionModel<ProjectBoundaryModel> boundaryCollection = CollectionModel.of(List.of(boundaryModel));

        when(projectBoundaryService.getAllProjectBoundaries(projectGuid)).thenReturn(boundaryCollection);

        ProjectFiscalModel fiscalModel = mock(ProjectFiscalModel.class);
        String fiscalGuid = UUID.randomUUID().toString();
        when(fiscalModel.getProjectPlanFiscalGuid()).thenReturn(fiscalGuid);
        CollectionModel<ProjectFiscalModel> fiscalCollection = CollectionModel.of(List.of(fiscalModel));
        when(projectFiscalService.getAllProjectFiscals(projectGuid)).thenReturn(fiscalCollection);

        ActivityModel activityModel = mock(ActivityModel.class);
        when(activityModel.getActivityGuid()).thenReturn("activity-guid");
        CollectionModel<ActivityModel> activityCollection = CollectionModel.of(List.of(activityModel));
        when(activityService.getAllActivities(projectGuid, fiscalGuid)).thenReturn(activityCollection);

        ActivityBoundaryModel activityBoundaryModel = mock(ActivityBoundaryModel.class);
        when(activityBoundaryModel.getGeometry()).thenReturn(mockPolygon);
        CollectionModel<ActivityBoundaryModel> activityBoundaryCollection = CollectionModel.of(List.of(activityBoundaryModel));
        when(activityBoundaryService.getAllActivityBoundaries(projectGuid, fiscalGuid, "activity-guid"))
                .thenReturn(activityBoundaryCollection);

        // Act
        List<MultiPolygon> result = coordinatesService.getAllPolygonsForProject(projectGuid);

        // Assert
        assertEquals(2, result.size()); // one from project, one from activity
        verify(projectBoundaryService).getAllProjectBoundaries(projectGuid);
        verify(activityService).getAllActivities(projectGuid, fiscalGuid);
    }

}