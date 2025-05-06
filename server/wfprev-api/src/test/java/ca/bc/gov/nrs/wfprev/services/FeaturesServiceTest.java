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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeaturesServiceTest {

    @Mock
    private ProjectBoundaryRepository projectBoundaryRepository;

    @Mock
    private ActivityBoundaryRepository activityBoundaryRepository;

    @InjectMocks
    private FeaturesService featuresService;

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
    void testGetAllFeatures() {
        when(projectBoundaryRepository.findAll()).thenReturn(List.of(projectBoundaryEntity));
        when(activityBoundaryRepository.findAll()).thenReturn(List.of(activityBoundaryEntity));

        Map<String, Object> result = featuresService.getAllFeatures();

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
        Map<String, Object> feature = featuresService.createPointFeature(point, properties);

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
        Map<String, Object> feature = featuresService.createPolygonFeature(multiPolygon, properties);

        assertEquals("Feature", feature.get("type"));
        assertEquals("MultiPolygon", ((Map<?, ?>) feature.get("geometry")).get("type"));
    }

    @Test
    void testCreatePolygonFeature_WithInteriorHole() {
        GeometryFactory geometryFactory = new GeometryFactory();

        // Define the exterior ring (outer boundary)
        Coordinate[] exteriorCoords = {
                new Coordinate(-123.3656, 48.4284),
                new Coordinate(-123.3657, 48.4285),
                new Coordinate(-123.3658, 48.4284),
                new Coordinate(-123.3656, 48.4284) // Closing the polygon
        };
        LinearRing exteriorRing = geometryFactory.createLinearRing(exteriorCoords);

        // Define the interior ring (hole)
        Coordinate[] interiorCoords = {
                new Coordinate(-123.36565, 48.42845),
                new Coordinate(-123.36570, 48.42846),
                new Coordinate(-123.36572, 48.42844),
                new Coordinate(-123.36565, 48.42845) // Closing the hole
        };
        LinearRing interiorRing = geometryFactory.createLinearRing(interiorCoords);

        // Create a polygon with a hole
        Polygon polygonWithHole = geometryFactory.createPolygon(exteriorRing, new LinearRing[]{interiorRing});

        // Create a MultiPolygon from the polygon with a hole
        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(new Polygon[]{polygonWithHole});

        // Create properties for the feature
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "Test Polygon with Hole");

        // Call the method under test
        Map<String, Object> feature = featuresService.createPolygonFeature(multiPolygon, properties);

        // Assertions
        assertNotNull(feature);
        assertEquals("Feature", feature.get("type"));

        Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
        assertNotNull(geometry);
        assertEquals("MultiPolygon", geometry.get("type"));

        List<List<List<double[]>>> coordinates = (List<List<List<double[]>>>) geometry.get("coordinates");

        // Assert that we have one polygon in the MultiPolygon
        assertEquals(1, coordinates.size());

        // Assert that the polygon has both an exterior and interior ring
        assertEquals(2, coordinates.get(0).size()); // 1 exterior + 1 interior (hole)

        // Validate that the hole coordinates exist
        List<double[]> holeCoordinates = coordinates.get(0).get(1);
        assertNotNull(holeCoordinates);
        assertFalse(holeCoordinates.isEmpty());

        // Validate first coordinate in the hole matches expected
        assertArrayEquals(new double[]{-123.36565, 48.42845}, holeCoordinates.get(0), 1e-6);
    }
//
//    @Test
//    void testCreateProjectProperties() {
//        Map<String, Object> properties = featuresService.createProjectProperties(projectBoundaryEntity);
//        assertEquals(projectGuid, properties.get("project_boundary_guid"));
//    }

    @Test
    void testCreateActivityProperties() {
        Map<String, Object> properties = featuresService.createActivityProperties(activityBoundaryEntity);
        assertEquals(activityGuid, properties.get("activity_boundary_guid"));
    }
}