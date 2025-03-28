package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.postgresql.geometric.PGpolygon;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeoJsonFeaturesService implements CommonService {

    private final ProjectBoundaryRepository projectBoundaryRepository;
    private final ActivityBoundaryRepository activityBoundaryRepository;

    public GeoJsonFeaturesService(
            ProjectBoundaryRepository projectBoundaryRepository,
            ActivityBoundaryRepository activityBoundaryRepository) {
        this.projectBoundaryRepository = projectBoundaryRepository;
        this.activityBoundaryRepository = activityBoundaryRepository;
    }

    public Map<String, Object> getAllFeaturesGeoJson() {
        List<ProjectBoundaryEntity> projectBoundaries = projectBoundaryRepository.findAll();
        List<ActivityBoundaryEntity> activityBoundaries = activityBoundaryRepository.findAll();

        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "FeatureCollection");
        List<Map<String, Object>> features = new ArrayList<>();

        // Process project boundary points
        for (ProjectBoundaryEntity pb : projectBoundaries) {
            if (pb.getLocationGeometry() != null) {
                features.add(createPointFeature(
                        pb.getLocationGeometry(),
                        createProjectProperties(pb)
                ));
            }

            // Process project boundary polygons
            if (pb.getBoundaryGeometry() != null) {
                features.add(createPolygonFeature(
                        pb.getBoundaryGeometry(),
                        createProjectProperties(pb)
                ));
            }
        }

        // Process activity boundaries
        for (ActivityBoundaryEntity ab : activityBoundaries) {
            if (ab.getGeometry() != null) {
                features.add(createPolygonFeature(
                        ab.getGeometry(),
                        createActivityProperties(ab)
                ));
            }
        }

        featureCollection.put("features", features);
        return featureCollection;
    }

    Map<String, Object> createPointFeature(@NotNull Point point, Map<String, Object> properties) {
        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");

        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "Point");

        // JTS Point coordinates: x=longitude, y=latitude
        double[] coordinates = new double[]{point.getX(), point.getY()};
        geometry.put("coordinates", coordinates);

        feature.put("geometry", geometry);
        feature.put("properties", properties);

        return feature;
    }

        public static Map<String, Object> createPolygonFeature(@NotNull MultiPolygon multiPolygon, Map<String, Object> properties) {
            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");

            Map<String, Object> geometry = new HashMap<>();
            geometry.put("type", "MultiPolygon"); // <-- Changed from "Polygon" to "MultiPolygon"

            List<List<List<double[]>>> polygons = new ArrayList<>();

            // Iterate over each Polygon in the MultiPolygon
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                List<List<double[]>> rings = new ArrayList<>();

                // Process the exterior ring
                rings.add(extractCoordinates(polygon.getExteriorRing()));

                // Process interior rings (holes)
                for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
                    rings.add(extractCoordinates(polygon.getInteriorRingN(j)));
                }

                polygons.add(rings);
            }

            geometry.put("coordinates", polygons);
            feature.put("geometry", geometry);
            feature.put("properties", properties);

            return feature;
        }

        private static List<double[]> extractCoordinates(LinearRing linearRing) {
            List<double[]> coordinates = new ArrayList<>();
            Coordinate[] coords = linearRing.getCoordinates();
            for (Coordinate coord : coords) {
                coordinates.add(new double[]{coord.x, coord.y});
            }

            // Ensure the ring is closed
            if (coords.length > 0 &&
                    (coords[0].x != coords[coords.length - 1].x ||
                            coords[0].y != coords[coords.length - 1].y)) {
                coordinates.add(new double[]{coords[0].x, coords[0].y});
            }

            return coordinates;
        }

    Map<String, Object> createProjectProperties(ProjectBoundaryEntity pb) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("project_boundary_guid", pb.getProjectBoundaryGuid().toString());

        return properties;
    }

    Map<String, Object> createActivityProperties(ActivityBoundaryEntity ab) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("activity_boundary_guid", ab.getActivityBoundaryGuid().toString());

        return properties;
    }


}
