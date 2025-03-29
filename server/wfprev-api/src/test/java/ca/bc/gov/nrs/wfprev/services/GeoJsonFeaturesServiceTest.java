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
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
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

        // Create a mock MultiPolygon geometry
        Coordinate[] coordinates = {
                new Coordinate(1.0, 2.0),
                new Coordinate(3.0, 4.0),
                new Coordinate(5.0, 6.0),
                new Coordinate(1.0, 2.0) // Closing the polygon
        };

        LinearRing shell = geometryFactory.createLinearRing(coordinates);
        Polygon polygon = geometryFactory.createPolygon(shell);

        // Create a MultiPolygon from a single Polygon
        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(new Polygon[]{polygon});

        projectBoundaryEntity.setBoundaryGeometry(multiPolygon);

        activityBoundaryEntity = new ActivityBoundaryEntity();
        activityBoundaryEntity.setActivityBoundaryGuid(UUID.fromString(activityGuid));
        activityBoundaryEntity.setGeometry(multiPolygon);
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
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = {
                new Coordinate(1.0, 2.0),
                new Coordinate(3.0, 4.0),
                new Coordinate(5.0, 6.0),
                new Coordinate(1.0, 2.0)
        };
        LinearRing shell = geometryFactory.createLinearRing(coordinates);
        Polygon polygon = geometryFactory.createPolygon(shell);

        // Create a MultiPolygon from a single Polygon
        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(new Polygon[]{polygon});

        Map<String, Object> properties = Map.of("key", "value");
        Map<String, Object> feature = geoJsonFeaturesService.createPolygonFeature(multiPolygon, properties);

        assertEquals("Feature", feature.get("type"));
        assertEquals("MultiPolygon", ((Map<?, ?>) feature.get("geometry")).get("type"));
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