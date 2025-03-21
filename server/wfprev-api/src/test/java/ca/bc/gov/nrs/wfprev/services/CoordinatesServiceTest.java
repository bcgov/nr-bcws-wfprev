package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;
import org.springframework.hateoas.CollectionModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    void testUpdateProjectCoordinates_Success() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();

        // Create a project model
        ProjectModel projectModel = new ProjectModel();
        projectModel.setProjectGuid(projectGuid);
        projectModel.setLatitude(BigDecimal.ZERO);
        projectModel.setLongitude(BigDecimal.ZERO);

        // Create a test polygon
        PGpoint[] points = new PGpoint[] {
                new PGpoint(-123.37, 48.42),
                new PGpoint(-123.36, 48.42),
                new PGpoint(-123.36, 48.43),
                new PGpoint(-123.37, 48.43),
                new PGpoint(-123.37, 48.42)
        };
        PGpolygon polygon = new PGpolygon(points);

        // Mock the project service to return our project
        when(projectService.getProjectById(projectGuid)).thenReturn(projectModel);

        CoordinatesService spyService = spy(coordinatesService);
        doReturn(Collections.singletonList(polygon)).when(spyService).getAllPolygonsForProject(projectGuid);

        // Mock the updateProject method
        when(projectService.updateProject(any(ProjectModel.class))).thenReturn(projectModel);

        // Act
        spyService.updateProjectCoordinates(projectGuid);

        // Assert
        assertEquals(new BigDecimal("48.424"), projectModel.getLatitude());
        assertEquals(new BigDecimal("-123.3660000"), projectModel.getLongitude());

        // Verify the updateProject method was called
        verify(projectService).updateProject(projectModel);
    }

    @Test
    void testUpdateProjectCoordinates_HighPrecisionValues() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();

        // Create a project model
        ProjectModel projectModel = new ProjectModel();
        projectModel.setProjectGuid(projectGuid);

        // Create a test polygon with high precision coordinates
        PGpoint[] points = new PGpoint[] {
                new PGpoint(-123.3712345678, 48.4298765432),
                new PGpoint(-123.3612345678, 48.4298765432),
                new PGpoint(-123.3612345678, 48.4398765432),
                new PGpoint(-123.3712345678, 48.4398765432),
                new PGpoint(-123.3712345678, 48.4298765432)
        };
        PGpolygon polygon = new PGpolygon(points);

        when(projectService.getProjectById(projectGuid)).thenReturn(projectModel);

        CoordinatesService spyService = spy(coordinatesService);
        doReturn(Collections.singletonList(polygon)).when(spyService).getAllPolygonsForProject(projectGuid);

        when(projectService.updateProject(any(ProjectModel.class))).thenReturn(projectModel);

        // Act
        spyService.updateProjectCoordinates(projectGuid);

        // Assert
        // Verify the coordinates are truncated to 7 decimal places
        assertEquals(7, projectModel.getLatitude().scale());
        assertEquals(7, projectModel.getLongitude().scale());

        // Verify the updateProject method was called
        verify(projectService).updateProject(projectModel);
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

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            spyService.updateProjectCoordinates(projectGuid);
        });

        // Verify the updateProject method was NOT called
        verify(projectService, never()).updateProject(any(ProjectModel.class));
    }

    @Test
    void testCalculateCentroid_MultiplePolygons() {
        // Arrange
        // Create two test polygons
        PGpoint[] points1 = new PGpoint[] {
                new PGpoint(0, 0),
                new PGpoint(1, 0),
                new PGpoint(1, 1),
                new PGpoint(0, 1),
                new PGpoint(0, 0)
        };
        PGpolygon polygon1 = new PGpolygon(points1);

        PGpoint[] points2 = new PGpoint[] {
                new PGpoint(2, 2),
                new PGpoint(3, 2),
                new PGpoint(3, 3),
                new PGpoint(2, 3),
                new PGpoint(2, 2)
        };
        PGpolygon polygon2 = new PGpolygon(points2);

        List<PGpolygon> polygons = Arrays.asList(polygon1, polygon2);

        // Act
        PGpoint centroid = coordinatesService.calculateCentroid(polygons);

        // Assert
        // With 2 squares (each with 5 points including the closing point), the simple average would be:
        // x: (0+1+1+0+0+2+3+3+2+2)/10 = 14/10 = 1.4
        // y: (0+0+1+1+0+2+2+3+3+2)/10 = 14/10 = 1.4
        assertEquals(1.4, centroid.x, 0.0001);
        assertEquals(1.4, centroid.y, 0.0001);
    }

    @Test
    void testCalculateCentroid_EmptyPolygons() {
        // Arrange
        List<PGpolygon> polygons = new ArrayList<>();
        polygons.add(new PGpolygon(new PGpoint[0]));  // Empty polygon

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            coordinatesService.calculateCentroid(polygons);
        });
    }

    @Test
    void testCalculateCentroid_NullPoints() {
        // Arrange
        List<PGpolygon> polygons = new ArrayList<>();
        polygons.add(new PGpolygon((PGpoint[])null));  // Polygon with null points

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            coordinatesService.calculateCentroid(polygons);
        });
    }

    @Test
    void testGetAllPolygonsForProject_Success() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();

        // Create project boundary with polygon
        PGpoint[] boundaryPoints = new PGpoint[] {
                new PGpoint(0, 0),
                new PGpoint(1, 0),
                new PGpoint(1, 1),
                new PGpoint(0, 1),
                new PGpoint(0, 0)
        };
        PGpolygon boundaryPolygon = new PGpolygon(boundaryPoints);

        ProjectBoundaryModel projectBoundary = new ProjectBoundaryModel();
        projectBoundary.setBoundaryGeometry(boundaryPolygon);

        // Create activity boundary with polygon
        PGpoint[] activityPoints = new PGpoint[] {
                new PGpoint(2, 2),
                new PGpoint(3, 2),
                new PGpoint(3, 3),
                new PGpoint(2, 3),
                new PGpoint(2, 2)
        };
        PGpolygon activityPolygon = new PGpolygon(activityPoints);

        ActivityBoundaryModel activityBoundary = new ActivityBoundaryModel();
        activityBoundary.setGeometry(activityPolygon);

        // Set up project fiscal and activity models
        ProjectFiscalModel projectFiscal = new ProjectFiscalModel();
        String fiscalGuid = UUID.randomUUID().toString();
        projectFiscal.setProjectPlanFiscalGuid(fiscalGuid);

        ActivityModel activity = new ActivityModel();
        String activityGuid = UUID.randomUUID().toString();
        activity.setActivityGuid(activityGuid);

        // Mock the services
        when(projectBoundaryService.getAllProjectBoundaries(projectGuid))
                .thenReturn(CollectionModel.of(Collections.singletonList(projectBoundary)));

        when(projectFiscalService.getAllProjectFiscals(projectGuid))
                .thenReturn(CollectionModel.of(Collections.singletonList(projectFiscal)));

        when(activityService.getAllActivities(projectGuid, fiscalGuid))
                .thenReturn(CollectionModel.of(Collections.singletonList(activity)));

        when(activityBoundaryService.getAllActivityBoundaries(projectGuid, fiscalGuid, activityGuid))
                .thenReturn(CollectionModel.of(Collections.singletonList(activityBoundary)));

        // Act
        List<PGpolygon> result = coordinatesService.getAllPolygonsForProject(projectGuid);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(boundaryPolygon));
        assertTrue(result.contains(activityPolygon));

        // Verify service calls
        verify(projectBoundaryService).getAllProjectBoundaries(projectGuid);
        verify(projectFiscalService).getAllProjectFiscals(projectGuid);
        verify(activityService).getAllActivities(projectGuid, fiscalGuid);
        verify(activityBoundaryService).getAllActivityBoundaries(projectGuid, fiscalGuid, activityGuid);
    }

    @Test
    void testGetAllPolygonsForProject_NoPolygons() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();

        // Mock empty collections
        when(projectBoundaryService.getAllProjectBoundaries(projectGuid))
                .thenReturn(CollectionModel.empty());

        when(projectFiscalService.getAllProjectFiscals(projectGuid))
                .thenReturn(CollectionModel.empty());

        // Act
        List<PGpolygon> result = coordinatesService.getAllPolygonsForProject(projectGuid);

        // Assert
        assertTrue(result.isEmpty());

        // Verify service calls
        verify(projectBoundaryService).getAllProjectBoundaries(projectGuid);
        verify(projectFiscalService).getAllProjectFiscals(projectGuid);
        verify(activityService, never()).getAllActivities(anyString(), anyString());
        verify(activityBoundaryService, never()).getAllActivityBoundaries(anyString(), anyString(), anyString());
    }

    @Test
    void testGetAllPolygonsForProject_NestedStructure() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();

        // Create multiple fiscals, activities and boundaries
        ProjectFiscalModel fiscal1 = new ProjectFiscalModel();
        String fiscal1Guid = UUID.randomUUID().toString();
        fiscal1.setProjectPlanFiscalGuid(fiscal1Guid);

        ProjectFiscalModel fiscal2 = new ProjectFiscalModel();
        String fiscal2Guid = UUID.randomUUID().toString();
        fiscal2.setProjectPlanFiscalGuid(fiscal2Guid);

        ActivityModel activity1 = new ActivityModel();
        String activity1Guid = UUID.randomUUID().toString();
        activity1.setActivityGuid(activity1Guid);

        ActivityModel activity2 = new ActivityModel();
        String activity2Guid = UUID.randomUUID().toString();
        activity2.setActivityGuid(activity2Guid);

        // Create polygons
        PGpolygon polygon1 = new PGpolygon(new PGpoint[]{
                new PGpoint(0, 0),
                new PGpoint(1, 0),
                new PGpoint(1, 1),
                new PGpoint(0, 1),
                new PGpoint(0, 0)
        });

        PGpolygon polygon2 = new PGpolygon(new PGpoint[]{
                new PGpoint(2, 2),
                new PGpoint(3, 2),
                new PGpoint(3, 3),
                new PGpoint(2, 3),
                new PGpoint(2, 2)
        });

        ActivityBoundaryModel boundary1 = new ActivityBoundaryModel();
        boundary1.setGeometry(polygon1);

        ActivityBoundaryModel boundary2 = new ActivityBoundaryModel();
        boundary2.setGeometry(polygon2);

        // No project boundaries
        when(projectBoundaryService.getAllProjectBoundaries(projectGuid))
                .thenReturn(CollectionModel.empty());

        // Two fiscals
        when(projectFiscalService.getAllProjectFiscals(projectGuid))
                .thenReturn(CollectionModel.of(Arrays.asList(fiscal1, fiscal2)));

        // Activities for fiscal1
        when(activityService.getAllActivities(projectGuid, fiscal1Guid))
                .thenReturn(CollectionModel.of(Collections.singletonList(activity1)));

        // Activities for fiscal2
        when(activityService.getAllActivities(projectGuid, fiscal2Guid))
                .thenReturn(CollectionModel.of(Collections.singletonList(activity2)));

        // Boundaries for activity1
        when(activityBoundaryService.getAllActivityBoundaries(projectGuid, fiscal1Guid, activity1Guid))
                .thenReturn(CollectionModel.of(Collections.singletonList(boundary1)));

        // Boundaries for activity2
        when(activityBoundaryService.getAllActivityBoundaries(projectGuid, fiscal2Guid, activity2Guid))
                .thenReturn(CollectionModel.of(Collections.singletonList(boundary2)));

        // Act
        List<PGpolygon> result = coordinatesService.getAllPolygonsForProject(projectGuid);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(polygon1));
        assertTrue(result.contains(polygon2));

        verify(activityService).getAllActivities(projectGuid, fiscal1Guid);
        verify(activityService).getAllActivities(projectGuid, fiscal2Guid);
        verify(activityBoundaryService).getAllActivityBoundaries(projectGuid, fiscal1Guid, activity1Guid);
        verify(activityBoundaryService).getAllActivityBoundaries(projectGuid, fiscal2Guid, activity2Guid);
    }
}