package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
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

    Map<String, Object> createPolygonFeature(@NotNull PGpolygon polygon, Map<String, Object> properties) {
        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");

        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "Polygon");

        // Convert PGpolygon to GeoJSON coordinates
        List<List<double[]>> rings = new ArrayList<>();
        List<double[]> exteriorRing = new ArrayList<>();

        // Parse the points from the polygon
        org.postgresql.geometric.PGpoint[] points = polygon.points;

        // Create a single ring (exterior)
        for (org.postgresql.geometric.PGpoint point : points) {
            exteriorRing.add(new double[]{point.x, point.y});
        }

        // Close the ring if it's not already closed
        if (points.length > 0 &&
                (points[0].x != points[points.length - 1].x ||
                        points[0].y != points[points.length - 1].y)) {
            exteriorRing.add(new double[]{points[0].x, points[0].y});
        }

        rings.add(exteriorRing);
        geometry.put("coordinates", rings);

        feature.put("geometry", geometry);
        feature.put("properties", properties);

        return feature;
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
