package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoJsonFeaturesServiceTest {

    @Mock
    private ProjectBoundaryRepository projectBoundaryRepository;

    @Mock
    private ActivityBoundaryRepository activityBoundaryRepository;

    @InjectMocks
    private GeoJsonFeaturesService geoJsonFeaturesService;

    private ProjectBoundaryEntity projectBoundaryEntity;
    private ActivityBoundaryEntity activityBoundaryEntity;

    private final String projectGuid = "2ca37892-b8a0-4ccd-9f91-992421b90121";
    private final String activityGuid = "2ca37892-b8a0-4ccd-9f91-992421b90122";

    @BeforeEach
    void setUp() {
        // Create a mock point geometry
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(-123.3656, 48.4284));

        projectBoundaryEntity = new ProjectBoundaryEntity();
        projectBoundaryEntity.setProjectBoundaryGuid(UUID.fromString(projectGuid));
        projectBoundaryEntity.setLocationGeometry(point);

        // Create a mock polygon geometry (PGpolygon)
        PGpoint[] pgPoints = {
                new PGpoint(1.0, 2.0),
                new PGpoint(3.0, 4.0),
                new PGpoint(5.0, 6.0),
                new PGpoint(1.0, 2.0) // Closing the polygon
        };
        PGpolygon polygon = new PGpolygon(pgPoints);
        projectBoundaryEntity.setBoundaryGeometry(polygon);

        activityBoundaryEntity = new ActivityBoundaryEntity();
        activityBoundaryEntity.setActivityBoundaryGuid(UUID.fromString(activityGuid));
        activityBoundaryEntity.setGeometry(polygon);
    }

    @Test
    void testGetAllFeaturesGeoJson() {
        when(projectBoundaryRepository.findAll()).thenReturn(List.of(projectBoundaryEntity));
        when(activityBoundaryRepository.findAll()).thenReturn(List.of(activityBoundaryEntity));

        Map<String, Object> result = geoJsonFeaturesService.getAllFeaturesGeoJson();

        assertNotNull(result);
        assertEquals("FeatureCollection", result.get("type"));

        List<?> features = (List<?>) result.get("features");
        assertEquals(3, features.size()); // One point, one polygon from project, one polygon from activity
    }

    @Test
    void testCreatePointFeature() {
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(-123.3656, 48.4284));

        Map<String, Object> properties = Map.of("key", "value");
        Map<String, Object> feature = geoJsonFeaturesService.createPointFeature(point, properties);

        assertEquals("Feature", feature.get("type"));
        assertEquals("Point", ((Map<?, ?>) feature.get("geometry")).get("type"));
    }

    @Test
    void testCreatePolygonFeature() {
        PGpoint[] pgPoints = {
                new PGpoint(1.0, 2.0),
                new PGpoint(3.0, 4.0),
                new PGpoint(5.0, 6.0),
                new PGpoint(1.0, 2.0) // Closing the polygon
        };
        PGpolygon polygon = new PGpolygon(pgPoints);

        Map<String, Object> properties = Map.of("key", "value");
        Map<String, Object> feature = geoJsonFeaturesService.createPolygonFeature(polygon, properties);

        assertEquals("Feature", feature.get("type"));
        assertEquals("Polygon", ((Map<?, ?>) feature.get("geometry")).get("type"));
    }

    @Test
    void testCreateProjectProperties() {
        Map<String, Object> properties = geoJsonFeaturesService.createProjectProperties(projectBoundaryEntity);
        assertEquals(projectGuid, properties.get("project_boundary_guid"));
    }

    @Test
    void testCreateActivityProperties() {
        Map<String, Object> properties = geoJsonFeaturesService.createActivityProperties(activityBoundaryEntity);
        assertEquals(activityGuid, properties.get("activity_boundary_guid"));
    }
}