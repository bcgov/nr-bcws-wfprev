package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.common.exceptions.ServiceException;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeaturesServiceTest {

    @Mock private EntityManager entityManager;
    @InjectMocks private FeaturesService featuresService;

    @Mock private CriteriaBuilder criteriaBuilder;
    @Mock private CriteriaQuery<ProjectEntity> projectQuery;
    @Mock private CriteriaQuery<ProjectFiscalEntity> fiscalQuery;
    @Mock private CriteriaQuery<ActivityEntity> activityQuery;
    @Mock private CriteriaQuery<ActivityBoundaryEntity> boundaryQuery;
    @Mock private CriteriaQuery<ProjectBoundaryEntity> projectBoundaryQuery;
    @Mock private Path<Object> path;
    @Mock private CriteriaBuilder.In<Object> inClause;
    @Mock private Root<ProjectEntity> projectRoot;
    @Mock private Root<ProjectFiscalEntity> fiscalRoot;
    @Mock private Root<ActivityEntity> activityRoot;
    @Mock private Root<ActivityBoundaryEntity> boundaryRoot;
    @Mock private Root<ProjectBoundaryEntity> projectBoundaryRoot;
    @Mock private Join<ProjectEntity, ProjectFiscalEntity> fiscalJoin;
    @Mock private Predicate mockPredicate;
    @Mock private MultiPolygon mockMultiPolygon;
    @Mock private Point mockPoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllFeatures() throws ServiceException {
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

        Map<String, Object> result = spyService.getAllFeatures(params);

        assertNotNull(result);
        assertEquals(1, ((List<?>) result.get("projects")).size());
    }

    @Test
    void testAddProjectBoundaries() {
        ProjectEntity project = new ProjectEntity();
        project.setProjectGuid(UUID.randomUUID());

        ProjectBoundaryEntity mockBoundary = new ProjectBoundaryEntity();
        mockBoundary.setBoundaryGeometry(mockMultiPolygon);
        mockBoundary.setLocationGeometry(mockPoint);

        FeaturesService spyService = spy(featuresService);
        doReturn(mockBoundary).when(spyService).findLatestProjectBoundary(any());

        Map<String, Object> projectMap = new HashMap<>();
        spyService.addProjectBoundaries(project, projectMap);

        assertTrue(projectMap.containsKey("projectBoundaries"));
    }

    @Test
    void testAddProjectFiscals() {
        FeatureQueryParams params = new FeatureQueryParams();
        params.setFiscalYears(Collections.singletonList("2025"));
        params.setActivityCategoryCodes(Collections.singletonList("CATEGORY"));
        params.setPlanFiscalStatusCodes(Collections.singletonList("STATUS"));

        ProjectEntity project = new ProjectEntity();
        project.setProjectGuid(UUID.randomUUID());

        ProjectFiscalEntity mockFiscal = new ProjectFiscalEntity();
        mockFiscal.setProject(project);
        List<ProjectFiscalEntity> mockFiscals = Collections.singletonList(mockFiscal);

        FeaturesService spyService = spy(featuresService);
        doReturn(mockFiscals).when(spyService).findFilteredProjectFiscals(any(), any(), any(), any());
        doAnswer(invocation -> null).when(spyService).addActivitiesToFiscal(any(), any());

        Map<String, Object> projectMap = new HashMap<>();
        spyService.addProjectFiscals(project, params, projectMap);

        assertTrue(projectMap.containsKey("projectFiscals"));
    }

    @Test
    void testAddActivitiesToFiscal() {
        ProjectFiscalEntity fiscal = new ProjectFiscalEntity();
        fiscal.setProjectPlanFiscalGuid(UUID.randomUUID());

        ActivityEntity mockActivity = new ActivityEntity();
        List<ActivityEntity> mockActivities = Collections.singletonList(mockActivity);

        FeaturesService spyService = spy(featuresService);
        doReturn(mockActivities).when(spyService).findActivitiesByProjectFiscal(any());
        doAnswer(invocation -> null).when(spyService).addActivityBoundaries(any(), any());

        Map<String, Object> fiscalMap = new HashMap<>();
        spyService.addActivitiesToFiscal(fiscal, fiscalMap);

        assertTrue(fiscalMap.containsKey("activities"));
    }

    @Test
    void testAddActivityBoundaries() {
        ActivityEntity activity = new ActivityEntity();
        activity.setActivityGuid(UUID.randomUUID());

        ActivityBoundaryEntity mockBoundary = new ActivityBoundaryEntity();
        List<ActivityBoundaryEntity> mockBoundaries = Collections.singletonList(mockBoundary);

        FeaturesService spyService = spy(featuresService);
        doReturn(mockBoundaries).when(spyService).findActivityBoundaries(any());

        Map<String, Object> activityMap = new HashMap<>();
        spyService.addActivityBoundaries(activity, activityMap);

        assertTrue(activityMap.containsKey("activityBoundaries"));
    }

    @Test
    void testAddActivityBoundaryGeometry() {
        ActivityBoundaryEntity boundary = new ActivityBoundaryEntity();
        boundary.setGeometry(mockMultiPolygon);

        List<Map<String, Object>> activityBoundaries = new ArrayList<>();
        featuresService.addActivityBoundaryGeometry(boundary, activityBoundaries);

        assertEquals(1, activityBoundaries.size());
        assertTrue(activityBoundaries.get(0).containsKey("activityGeometry"));
    }

    @Test
    void testFindFilteredProjects() {
        FeatureQueryParams params = new FeatureQueryParams();

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(ProjectEntity.class)).thenReturn(projectQuery);
        when(projectQuery.from(ProjectEntity.class)).thenReturn(projectRoot);
        TypedQuery<ProjectEntity> mockQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(projectQuery)).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(Collections.singletonList(new ProjectEntity()));

        List<ProjectEntity> result = featuresService.findFilteredProjects(params);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testFindFilteredProjectFiscals() {
        UUID projectGuid = UUID.randomUUID();
        FeatureQueryParams params = new FeatureQueryParams();
        params.setFiscalYears(List.of("2024"));

        // Mock criteria builder and query
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(ProjectFiscalEntity.class)).thenReturn(fiscalQuery);
        when(fiscalQuery.from(ProjectFiscalEntity.class)).thenReturn(fiscalRoot);

        Path projectPath = mock(Path.class);
        Path guidPath = mock(Path.class);
        Path categoryPath = mock(Path.class);
        Path statusPath = mock(Path.class);
        Path fiscalYearPath = mock(Path.class);

        when(fiscalRoot.get("project")).thenReturn(projectPath);
        when(projectPath.get("projectGuid")).thenReturn(guidPath);
        when(fiscalRoot.get("activityCategoryCode")).thenReturn(categoryPath);
        when(fiscalRoot.get("planFiscalStatusCode")).thenReturn(statusPath);
        when(fiscalRoot.get("fiscalYear")).thenReturn(fiscalYearPath);

        // Mock query behavior
        TypedQuery<ProjectFiscalEntity> mockQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(fiscalQuery)).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(Collections.singletonList(new ProjectFiscalEntity()));

        // Call method under test
        List<ProjectFiscalEntity> result = featuresService.findFilteredProjectFiscals(
                projectGuid,
                params.getFiscalYears(),
                List.of("CATEGORY"),
                List.of("STATUS")
        );

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testFindActivitiesByProjectFiscal() {
        // Arrange
        UUID fiscalGuid = UUID.randomUUID();

        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        CriteriaQuery<ActivityEntity> activityQuery = mock(CriteriaQuery.class);
        Root<ActivityEntity> activityRoot = mock(Root.class);
        TypedQuery<ActivityEntity> mockQuery = mock(TypedQuery.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(ActivityEntity.class)).thenReturn(activityQuery);
        when(activityQuery.from(ActivityEntity.class)).thenReturn(activityRoot);
        when(entityManager.createQuery(activityQuery)).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(Collections.singletonList(new ActivityEntity()));

        // Act
        List<ActivityEntity> result = featuresService.findActivitiesByProjectFiscal(fiscalGuid);

        // Assert
        assertNotNull(result, "Expected a non-null result.");
        assertEquals(1, result.size(), "Expected exactly one result.");
    }

    @Test
    void testFindLatestProjectBoundary() {
        UUID projectGuid = UUID.randomUUID();

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(ProjectBoundaryEntity.class)).thenReturn(projectBoundaryQuery);
        when(projectBoundaryQuery.from(ProjectBoundaryEntity.class)).thenReturn(projectBoundaryRoot);

        TypedQuery<ProjectBoundaryEntity> mockQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(projectBoundaryQuery)).thenReturn(mockQuery);

        when(mockQuery.setMaxResults(1)).thenReturn(mockQuery);

        ProjectBoundaryEntity mockBoundary = new ProjectBoundaryEntity();
        when(mockQuery.getResultList()).thenReturn(List.of(mockBoundary));

        ProjectBoundaryEntity result = featuresService.findLatestProjectBoundary(projectGuid);

        assertNotNull(result);
    }

    @Test
    void testCreatePolygonFeature() {
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> result = FeaturesService.createPolygonFeature(mockMultiPolygon, properties);
        assertNotNull(result);
        assertTrue(result.containsKey("geometry"));
    }

    @Test
    void testCreatePointFeature() {
        Map<String, Object> properties = new HashMap<>();
        when(mockPoint.getX()).thenReturn(123.456);
        when(mockPoint.getY()).thenReturn(789.123);
        Map<String, Object> result = featuresService.createPointFeature(mockPoint, properties);
        assertNotNull(result);
        assertTrue(result.containsKey("geometry"));
    }

    @Test
    void testAddProjectLevelFilters_WithForestRegionOrgUnitIds() {
        // Arrange
        FeatureQueryParams params = new FeatureQueryParams();
        params.setForestRegionOrgUnitIds(List.of("Region1", "Region2"));
        List<Predicate> predicates = new ArrayList<>();

        when(projectRoot.get("forestRegionOrgUnitId")).thenReturn(path);

        // Act
        featuresService.addProjectLevelFilters(projectRoot, predicates, params);

        // Assert
        assertEquals(1, predicates.size(), "Expected one predicate for forestRegionOrgUnitIds");
        verify(projectRoot, times(1)).get("forestRegionOrgUnitId");
    }

    @Test
    void testAddProjectLevelFilters_WithMultipleConditions() {
        // Arrange
        FeatureQueryParams params = new FeatureQueryParams();
        params.setProgramAreaGuids(List.of(UUID.randomUUID()));
        params.setForestRegionOrgUnitIds(List.of("Region1"));
        params.setForestDistrictOrgUnitIds(List.of("District1"));
        params.setFireCentreOrgUnitIds(List.of("Centre1"));
        List<Predicate> predicates = new ArrayList<>();

        // Mocking the paths
        when(projectRoot.get("programAreaGuid")).thenReturn(path);
        when(projectRoot.get("forestRegionOrgUnitId")).thenReturn(path);
        when(projectRoot.get("forestDistrictOrgUnitId")).thenReturn(path);
        when(projectRoot.get("fireCentreOrgUnitId")).thenReturn(path);

        // Act
        featuresService.addProjectLevelFilters(projectRoot, predicates, params);

        // Assert
        assertEquals(4, predicates.size(), "Expected four predicates for all conditions");
        verify(projectRoot, times(1)).get("programAreaGuid");
        verify(projectRoot, times(1)).get("forestRegionOrgUnitId");
        verify(projectRoot, times(1)).get("forestDistrictOrgUnitId");
        verify(projectRoot, times(1)).get("fireCentreOrgUnitId");
    }

    @Test
    void testAddProjectLevelFilters_WithNoConditions() {
        // Arrange
        FeatureQueryParams params = new FeatureQueryParams();
        List<Predicate> predicates = new ArrayList<>();

        // Act
        featuresService.addProjectLevelFilters(projectRoot, predicates, params);

        // Assert
        assertEquals(0, predicates.size(), "Expected no predicates for empty params");
    }

    @Test
    void testAddFiscalAttributeFilters_WithNoFilters() {
        // Arrange
        FeatureQueryParams params = new FeatureQueryParams(); // No filters provided
        List<Predicate> predicates = new ArrayList<>();

        // Act
        featuresService.addFiscalAttributeFilters(criteriaBuilder, projectRoot, predicates, params);

        // Assert
        verify(projectRoot, never()).join(anyString(), any(JoinType.class));
        // Ensure predicates list remains empty
        assertEquals(0, predicates.size(), "Predicates should remain empty when no filters are provided.");
    }

    @Test
    void testAddActivityCategoryCodeFilters_WithValidCodes() {
        // Arrange
        List<String> activityCategoryCodes = List.of("Code1", "Code2");
        List<Predicate> predicates = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Join<ProjectEntity, ProjectFiscalEntity> fiscal = (Join<ProjectEntity, ProjectFiscalEntity>) mock(Join.class);

        @SuppressWarnings("unchecked")
        Path<Object> mockPath = (Path<Object>) mock(Path.class);
        Predicate mockPredicate = mock(Predicate.class);

        when(fiscal.get("activityCategoryCode")).thenReturn(mockPath);
        when(mockPath.in(activityCategoryCodes)).thenReturn(mockPredicate);

        // Act
        featuresService.addActivityCategoryCodeFilters(fiscal, predicates, activityCategoryCodes);

        // Assert
        verify(fiscal, times(1)).get("activityCategoryCode");
        verify(mockPath, times(1)).in(activityCategoryCodes);
        assertEquals(1, predicates.size(), "Expected one predicate to be added.");
        assertTrue(predicates.contains(mockPredicate), "Expected the predicate to be added to the list.");
    }

    @Test
    void testAddActivityCategoryCodeFilters_WithNullOrEmptyCodes() {
        // Arrange
        List<String> activityCategoryCodes = null;
        List<Predicate> predicates = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Join<ProjectEntity, ProjectFiscalEntity> fiscal = (Join<ProjectEntity, ProjectFiscalEntity>) mock(Join.class);

        // Act
        featuresService.addActivityCategoryCodeFilters(fiscal, predicates, activityCategoryCodes);

        // Assert
        verify(fiscal, never()).get(anyString());
        assertEquals(0, predicates.size(), "Expected no predicates to be added.");
    }

    @Test
    void testAddPlanFiscalStatusCodeFilters_WithValidCodes() {
        // Arrange
        List<String> planFiscalStatusCodes = List.of("Status1", "Status2");
        List<Predicate> predicates = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Join<ProjectEntity, ProjectFiscalEntity> fiscal = (Join<ProjectEntity, ProjectFiscalEntity>) mock(Join.class);

        @SuppressWarnings("unchecked")
        Path<Object> mockPath = (Path<Object>) mock(Path.class);
        Predicate mockPredicate = mock(Predicate.class);

        // Mock the behavior of fiscal.get()
        when(fiscal.get("planFiscalStatusCode")).thenReturn(mockPath);
        when(mockPath.in(planFiscalStatusCodes)).thenReturn(mockPredicate);

        // Act
        featuresService.addPlanFiscalStatusCodeFilters(fiscal, predicates, planFiscalStatusCodes);

        // Assert
        verify(fiscal, times(1)).get("planFiscalStatusCode");
        verify(mockPath, times(1)).in(planFiscalStatusCodes);
        assertEquals(1, predicates.size(), "Expected one predicate to be added.");
        assertTrue(predicates.contains(mockPredicate), "Expected the predicate to be added to the list.");
    }

    @Test
    void testAddPlanFiscalStatusCodeFilters_WithNullOrEmptyCodes() {
        // Arrange
        List<String> planFiscalStatusCodes = null; // or Collections.emptyList()
        List<Predicate> predicates = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Join<ProjectEntity, ProjectFiscalEntity> fiscal = (Join<ProjectEntity, ProjectFiscalEntity>) mock(Join.class);

        // Act
        featuresService.addPlanFiscalStatusCodeFilters(fiscal, predicates, planFiscalStatusCodes);

        // Assert
        verify(fiscal, never()).get(anyString());
        assertEquals(0, predicates.size(), "Expected no predicates to be added.");
    }

    @Test
    void testExtractCoordinates_ClosedRing() {
        // Arrange
        Coordinate[] coords = {
                new Coordinate(0, 0),
                new Coordinate(1, 1),
                new Coordinate(2, 0),
                new Coordinate(0, 0) // Closed ring
        };
        LinearRing mockLinearRing = mock(LinearRing.class);
        when(mockLinearRing.getCoordinates()).thenReturn(coords);

        // Act
        List<double[]> result = FeaturesService.extractCoordinates(mockLinearRing);

        // Assert
        assertEquals(4, result.size(), "Expected 4 coordinates for a closed ring.");
        assertArrayEquals(new double[]{0, 0}, result.get(0));
        assertArrayEquals(new double[]{1, 1}, result.get(1));
        assertArrayEquals(new double[]{2, 0}, result.get(2));
        assertArrayEquals(new double[]{0, 0}, result.get(3));
    }

    @Test
    void testExtractCoordinates_OpenRing() {
        // Arrange
        Coordinate[] coords = {
                new Coordinate(0, 0),
                new Coordinate(1, 1),
                new Coordinate(2, 0)
        };
        LinearRing mockLinearRing = mock(LinearRing.class);
        when(mockLinearRing.getCoordinates()).thenReturn(coords);

        // Act
        List<double[]> result = FeaturesService.extractCoordinates(mockLinearRing);

        // Assert
        assertEquals(4, result.size(), "Expected 4 coordinates for an open ring (with closing point added).");
        assertArrayEquals(new double[]{0, 0}, result.get(0));
        assertArrayEquals(new double[]{1, 1}, result.get(1));
        assertArrayEquals(new double[]{2, 0}, result.get(2));
        assertArrayEquals(new double[]{0, 0}, result.get(3));
    }

    @Test
    void testExtractCoordinates_EmptyRing() {
        // Arrange
        Coordinate[] coords = {};
        LinearRing mockLinearRing = mock(LinearRing.class);
        when(mockLinearRing.getCoordinates()).thenReturn(coords);

        // Act
        List<double[]> result = FeaturesService.extractCoordinates(mockLinearRing);

        // Assert
        assertTrue(result.isEmpty(), "Expected an empty list for an empty ring.");
    }

    @Test
    void testAddSearchTextFilters_WithValidSearchText() {
        // Arrange
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<ProjectEntity> project = mock(Root.class);
        List<Predicate> predicates = new ArrayList<>();
        FeatureQueryParams params = mock(FeatureQueryParams.class);

        when(params.getSearchText()).thenReturn("test");

        Predicate mockPredicate = mock(Predicate.class);
        when(cb.like(any(), anyString())).thenReturn(mockPredicate);

        Join<Object, Object> fiscal = mock(Join.class);

        when(project.join(eq("projectFiscals"), eq(JoinType.LEFT))).thenReturn(fiscal);

        when(project.get("projectName")).thenReturn(mock(Path.class));
        when(project.get("projectLead")).thenReturn(mock(Path.class));
        when(project.get("projectDescription")).thenReturn(mock(Path.class));
        when(project.get("closestCommunityName")).thenReturn(mock(Path.class));
        when(project.get("siteUnitName")).thenReturn(mock(Path.class));
        when(project.get("projectNumber")).thenReturn(mock(Path.class));

        when(fiscal.get("projectFiscalName")).thenReturn(mock(Path.class));
        when(fiscal.get("firstNationsPartner")).thenReturn(mock(Path.class));
        when(fiscal.get("otherPartner")).thenReturn(mock(Path.class));

        // Act
        new FeaturesService().addSearchTextFilters(cb, project, predicates, params);

        // Assert
        assertEquals(1, predicates.size(), "Expected one top-level predicate to be added.");
        verify(cb, times(10)).like(any(), eq("%test%"));
    }

    @Test
    void testAddSearchTextFilters_WithNullOrBlankSearchText() {
        // Arrange
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<ProjectEntity> project = mock(Root.class);
        List<Predicate> predicates = new ArrayList<>();
        FeatureQueryParams params = mock(FeatureQueryParams.class);

        // Mock params.getSearchText() to return null
        when(params.getSearchText()).thenReturn(null);

        // Act
        featuresService.addSearchTextFilters(cb, project, predicates, params);

        // Assert
        assertTrue(predicates.isEmpty(), "Expected no predicates to be added when searchText is null.");

        // Test with blank search text
        when(params.getSearchText()).thenReturn("   ");

        // Act again
        new FeaturesService().addSearchTextFilters(cb, project, predicates, params);

        // Assert again
        assertTrue(predicates.isEmpty(), "Expected no predicates to be added when searchText is blank.");
    }

    @Test
    void testAddFiscalAttributeFilters_WithEmptyParams() {
        // Arrange
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<ProjectEntity> project = mock(Root.class);
        List<Predicate> predicates = new ArrayList<>();
        FeatureQueryParams params = mock(FeatureQueryParams.class);

        // Mock empty parameters
        when(params.getFiscalYears()).thenReturn(Collections.emptyList());
        when(params.getActivityCategoryCodes()).thenReturn(Collections.emptyList());
        when(params.getPlanFiscalStatusCodes()).thenReturn(Collections.emptyList());

        // Act
        new FeaturesService().addFiscalAttributeFilters(cb, project, predicates, params);

        // Assert
        verify(project, never()).join(anyString(), any(JoinType.class));
        assertTrue(predicates.isEmpty(), "Expected no predicates to be added.");
    }

    @Test
    void testAddFiscalYearFilters_WithEmptyYears() {
        // Arrange
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Join<ProjectEntity, ProjectFiscalEntity> fiscal = (Join<ProjectEntity, ProjectFiscalEntity>) mock(Join.class);
        List<Predicate> predicates = new ArrayList<>();
        List<String> fiscalYears = Collections.emptyList();

        // Act
        new FeaturesService().addFiscalYearFilters(cb, fiscal, predicates, fiscalYears);

        // Assert
        verify(cb, never()).like(any(), anyString());
        // Verify that no predicates were added
        assertTrue(predicates.isEmpty(), "Expected no predicates to be added.");
    }

    @Test
    void testCreateActivityProperties() {
        UUID guid = UUID.randomUUID();
        UUID fiscalGuid = UUID.randomUUID();
        Date now = new Date();

        ActivityEntity activity = ActivityEntity.builder()
                .activityGuid(guid)
                .projectPlanFiscalGuid(fiscalGuid)
                .activityName("Tree Planting")
                .activityDescription("Planting 500 trees")
                .activityStartDate(now)
                .activityEndDate(now)
                .plannedSpendAmount(new BigDecimal("15000.50"))
                .plannedTreatmentAreaHa(new BigDecimal("12.3456"))
                .reportedSpendAmount(new BigDecimal("13000.00"))
                .completedAreaHa(new BigDecimal("10.1234"))
                .isResultsReportableInd(true)
                .outstandingObligationsInd(false)
                .activityComment("Completed early")
                .isSpatialAddedInd(true)
                .revisionCount(1)
                .createUser("tester")
                .createDate(now)
                .updateUser("tester")
                .updateDate(now)
                .build();

        Map<String, Object> result = featuresService.createActivityProperties(activity);

        assertEquals(guid, result.get("activityGuid"));
        assertEquals(fiscalGuid, result.get("projectPlanFiscalGuid"));
        assertEquals("Tree Planting", result.get("activityName"));
        assertEquals("Planting 500 trees", result.get("activityDescription"));
        assertEquals(now, result.get("activityStartDate"));
        assertEquals(now, result.get("activityEndDate"));
        assertEquals(new BigDecimal("15000.50"), result.get("plannedSpendAmount"));
        assertEquals(new BigDecimal("12.3456"), result.get("plannedTreatmentAreaHa"));
        assertEquals(new BigDecimal("13000.00"), result.get("reportedSpendAmount"));
        assertEquals(new BigDecimal("10.1234"), result.get("completedAreaHa"));
        assertEquals(true, result.get("isResultsReportableInd"));
        assertEquals(false, result.get("outstandingObligationsInd"));
        assertEquals("Completed early", result.get("activityComment"));
        assertEquals(true, result.get("isSpatialAddedInd"));
        assertEquals(1, result.get("revisionCount"));
        assertEquals("tester", result.get("createUser"));
        assertEquals(now, result.get("createDate"));
        assertEquals("tester", result.get("updateUser"));
        assertEquals(now, result.get("updateDate"));
    }

    @Test
    void testAddFiscalYearFilters_WithNullYearString() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        Join<ProjectEntity, ProjectFiscalEntity> fiscal = mock(Join.class);

        @SuppressWarnings("unchecked")
        Path<Object> fiscalYearPath = (Path<Object>) mock(Path.class);

        Predicate isNullPredicate = mock(Predicate.class);

        when(fiscal.get("fiscalYear")).thenReturn(fiscalYearPath);
        when(cb.isNull(fiscalYearPath)).thenReturn(isNullPredicate);

        List<String> fiscalYears = List.of("null");
        List<Predicate> predicates = new ArrayList<>();

        featuresService.addFiscalYearFilters(cb, fiscal, predicates, fiscalYears);
        
        verify(cb, times(1)).isNull(fiscalYearPath);
        assertEquals(1, predicates.size());
    }

    @Test
    void testAddFiscalYearFilters_WithNonNullYear() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        Join<ProjectEntity, ProjectFiscalEntity> fiscal = mock(Join.class);
        @SuppressWarnings("unchecked")
        Path<Object> fiscalYearPath = (Path<Object>) mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> yearAsString = (Expression<String>) mock(Expression.class);

        Predicate likePredicate = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        when(fiscal.get("fiscalYear")).thenReturn(fiscalYearPath);
        when(fiscalYearPath.as(String.class)).thenReturn(yearAsString);
        when(cb.like(yearAsString, "2024%")).thenReturn(likePredicate);
        when(cb.or(likePredicate)).thenReturn(orPredicate);

        List<Predicate> predicates = new ArrayList<>();
        List<String> fiscalYears = List.of("2024");

        featuresService.addFiscalYearFilters(cb, fiscal, predicates, fiscalYears);

        verify(cb).like(yearAsString, "2024%");
        verify(cb).or(likePredicate);
        assertEquals(1, predicates.size());
    }
}
