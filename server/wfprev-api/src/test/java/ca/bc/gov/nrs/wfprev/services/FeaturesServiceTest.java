package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeaturesServiceTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private FeaturesService featuresService;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<ProjectEntity> projectQuery;

    @Mock
    private CriteriaQuery<ProjectFiscalEntity> fiscalQuery;

    @Mock
    private CriteriaQuery<ActivityEntity> activityQuery;

    @Mock
    private CriteriaQuery<ActivityBoundaryEntity> boundaryQuery;

    @Mock
    private CriteriaQuery<ProjectBoundaryEntity> projectBoundaryQuery;

    @Mock
    private Root<ProjectEntity> projectRoot;

    @Mock
    private Root<ProjectFiscalEntity> fiscalRoot;

    @Mock
    private Root<ActivityEntity> activityRoot;

    @Mock
    private Root<ActivityBoundaryEntity> boundaryRoot;

    @Mock
    private Root<ProjectBoundaryEntity> projectBoundaryRoot;

    @Mock
    private MultiPolygon mockMultiPolygon;

    @Mock
    private Point mockPoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllFeatures() {
        // Arrange
        FeatureQueryParams params = new FeatureQueryParams();
        params.setProgramAreaGuids(Collections.singletonList(UUID.randomUUID()));
        params.setFiscalYears(Collections.singletonList("2022"));
        params.setActivityCategoryCodes(Collections.singletonList("CATEGORY"));
        params.setPlanFiscalStatusCodes(Collections.singletonList("STATUS"));
        params.setSearchText("searchText");

        ProjectEntity mockProject = new ProjectEntity();
        mockProject.setProjectGuid(UUID.randomUUID());

        List<ProjectEntity> mockProjects = Collections.singletonList(mockProject);

        FeaturesService spyService = spy(featuresService);
        doReturn(mockProjects).when(spyService).findFilteredProjects(params);
        doAnswer(invocation -> null).when(spyService).addProjectBoundaries(any(), any());
        doAnswer(invocation -> null).when(spyService).addProjectFiscals(any(), any(), any());

        // Act
        Map<String, Object> result = spyService.getAllFeatures(params);

        // Assert
        assertNotNull(result);
        assertEquals(1, ((List<?>) result.get("projects")).size());
    }

    @Test
    void testAddProjectBoundaries() {
        // Arrange
        ProjectEntity project = new ProjectEntity();
        project.setProjectGuid(UUID.randomUUID());

        ProjectBoundaryEntity mockBoundary = new ProjectBoundaryEntity();
        mockBoundary.setBoundaryGeometry(mockMultiPolygon);
        mockBoundary.setLocationGeometry(mockPoint);

        FeaturesService spyService = spy(featuresService);
        doReturn(mockBoundary).when(spyService).findLatestProjectBoundary(any());

        Map<String, Object> projectMap = new HashMap<>();

        // Act
        spyService.addProjectBoundaries(project, projectMap);

        // Assert
        assertTrue(projectMap.containsKey("projectBoundaries"));
    }

    @Test
    void testAddProjectFiscals() {
        // Arrange
        FeatureQueryParams params = new FeatureQueryParams();
        params.setFiscalYears(Collections.singletonList("2025"));
        params.setActivityCategoryCodes(Collections.singletonList("CATEGORY"));
        params.setPlanFiscalStatusCodes(Collections.singletonList("STATUS"));

        ProjectEntity project = new ProjectEntity();
        project.setProjectGuid(UUID.randomUUID());

        ProjectFiscalEntity mockFiscal = new ProjectFiscalEntity();
        mockFiscal.setProject(project); // Initialize the project field
        List<ProjectFiscalEntity> mockFiscals = Collections.singletonList(mockFiscal);

        FeaturesService spyService = spy(featuresService);
        doReturn(mockFiscals).when(spyService).findFilteredProjectFiscals(any(), any(), any(), any());
        doAnswer(invocation -> null).when(spyService).addActivitiesToFiscal(any(), any());

        Map<String, Object> projectMap = new HashMap<>();

        // Act
        spyService.addProjectFiscals(project, params, projectMap);

        // Assert
        assertTrue(projectMap.containsKey("projectFiscals"));
    }

    @Test
    void testAddActivitiesToFiscal() {
        // Arrange
        ProjectFiscalEntity fiscal = new ProjectFiscalEntity();
        fiscal.setProjectPlanFiscalGuid(UUID.randomUUID());

        ActivityEntity mockActivity = new ActivityEntity();
        List<ActivityEntity> mockActivities = Collections.singletonList(mockActivity);

        FeaturesService spyService = spy(featuresService);
        doReturn(mockActivities).when(spyService).findActivitiesByProjectFiscal(any());
        doAnswer(invocation -> null).when(spyService).addActivityBoundaries(any(), any());

        Map<String, Object> fiscalMap = new HashMap<>();

        // Act
        spyService.addActivitiesToFiscal(fiscal, fiscalMap);

        // Assert
        assertTrue(fiscalMap.containsKey("activities"));
    }

    @Test
    void testAddActivityBoundaries() {
        // Arrange
        ActivityEntity activity = new ActivityEntity();
        activity.setActivityGuid(UUID.randomUUID());

        ActivityBoundaryEntity mockBoundary = new ActivityBoundaryEntity();
        List<ActivityBoundaryEntity> mockBoundaries = Collections.singletonList(mockBoundary);

        FeaturesService spyService = spy(featuresService);
        doReturn(mockBoundaries).when(spyService).findActivityBoundaries(any());

        Map<String, Object> activityMap = new HashMap<>();

        // Act
        spyService.addActivityBoundaries(activity, activityMap);

        // Assert
        assertTrue(activityMap.containsKey("activityBoundaries"));
    }

    @Test
    void testCreatePolygonFeature() {
        // Arrange
        Map<String, Object> properties = new HashMap<>();

        // Act
        Map<String, Object> result = FeaturesService.createPolygonFeature(mockMultiPolygon, properties);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("geometry"));
    }

    @Test
    void testCreatePointFeature() {
        // Arrange
        Map<String, Object> properties = new HashMap<>();
        when(mockPoint.getX()).thenReturn(123.456);
        when(mockPoint.getY()).thenReturn(789.123);

        // Act
        Map<String, Object> result = featuresService.createPointFeature(mockPoint, properties);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("geometry"));
    }
}