package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProgramAreaRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectFiscalRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class FeaturesService implements CommonService {

    @PersistenceContext
    private EntityManager entityManager;

    private final ProjectRepository projectRepository;
    private final ProjectFiscalRepository projectFiscalRepository;
    private final ActivityRepository activityRepository;
    private final ProjectBoundaryRepository projectBoundaryRepository;
    private final ActivityBoundaryRepository activityBoundaryRepository;

    public FeaturesService(
            ProjectRepository projectRepository,
            ProjectFiscalRepository projectFiscalRepository,
            ProjectBoundaryRepository projectBoundaryRepository,
            ProgramAreaRepository programAreaRepository,
            ActivityBoundaryRepository activityBoundaryRepository,
            ActivityRepository activityRepository) {
        this.projectBoundaryRepository = projectBoundaryRepository;
        this.activityBoundaryRepository = activityBoundaryRepository;
        this.projectRepository = projectRepository;
        this.projectFiscalRepository = projectFiscalRepository;
        this.activityRepository = activityRepository;

    }


    public Map<String, Object> getAllFeatures(
            List<UUID> programAreaGuids,
            List<String> fiscalYears,
            List<String> forestRegionOrgUnitIds,
            List<String> forestDistrictOrgUnitIds,
            List<String> fireCentreOrgUnitIds,
            List<String> activityCategoryCodes,
            List<String> planFiscalStatusCodes,
            String searchText
    ) {
        List<ProjectEntity> projectEntities = projectRepository.findAll();
        List<ProjectFiscalEntity> projectFiscalEntities = projectFiscalRepository.findAll();
        List<ActivityEntity> activityEntities = activityRepository.findAll(); // Get all activities

        // Retrieve all project boundaries
        Map<UUID, List<ProjectBoundaryEntity>> boundariesByProjectGuid = new HashMap<>();
        List<ProjectBoundaryEntity> allBoundaries = projectBoundaryRepository.findAll();

        // Group boundaries by project GUID
        for (ProjectBoundaryEntity boundary : allBoundaries) {
            UUID projectGuid = boundary.getProjectGuid();
            boundariesByProjectGuid.computeIfAbsent(projectGuid, k -> new ArrayList<>()).add(boundary);
        }

        // Get all activity boundaries and group them by activity GUID
        Map<UUID, List<ActivityBoundaryEntity>> boundariesByActivityGuid = new HashMap<>();
        List<ActivityBoundaryEntity> allActivityBoundaries = activityBoundaryRepository.findAll();

        for (ActivityBoundaryEntity boundary : allActivityBoundaries) {
            UUID activityGuid = boundary.getActivityGuid();
            boundariesByActivityGuid.computeIfAbsent(activityGuid, k -> new ArrayList<>()).add(boundary);
        }

        // Map activities by project fiscal GUID
        Map<UUID, List<ActivityEntity>> activitiesByProjectFiscalGuid = new HashMap<>();
        for (ActivityEntity activity : activityEntities) {
            UUID projectFiscalGuid = activity.getProjectPlanFiscalGuid();
            activitiesByProjectFiscalGuid.computeIfAbsent(projectFiscalGuid, k -> new ArrayList<>()).add(activity);
        }

        // Group fiscals by projectGuid with filters applied
        Map<UUID, List<ProjectFiscalEntity>> fiscalsByProjectGuid = new HashMap<>();
        for (ProjectFiscalEntity fiscal : projectFiscalEntities) {
            UUID projectGuid = fiscal.getProject().getProjectGuid();

            boolean fiscalMatches = (fiscalYears == null || fiscalYears.isEmpty() ||
                    fiscalYears.contains(fiscal.getFiscalYear().stripTrailingZeros().toPlainString())) &&
                    (activityCategoryCodes == null || activityCategoryCodes.isEmpty() ||
                            activityCategoryCodes.contains(fiscal.getActivityCategoryCode())) &&
                    (planFiscalStatusCodes == null || planFiscalStatusCodes.isEmpty() ||
                            planFiscalStatusCodes.contains(fiscal.getPlanFiscalStatusCode()));

            if (fiscalMatches) {
                fiscalsByProjectGuid.computeIfAbsent(projectGuid, k -> new ArrayList<>()).add(fiscal);
            }
        }

        List<Map<String, Object>> projects = new ArrayList<>();

        for (ProjectEntity proj : projectEntities) {
            boolean projectMatches =
                    (programAreaGuids == null || programAreaGuids.isEmpty() ||
                            programAreaGuids.contains(proj.getProgramAreaGuid())) &&
                            (forestRegionOrgUnitIds == null || forestRegionOrgUnitIds.isEmpty() ||
                                    forestRegionOrgUnitIds.contains(String.valueOf(proj.getForestRegionOrgUnitId()))) &&
                            (forestDistrictOrgUnitIds == null || forestDistrictOrgUnitIds.isEmpty() ||
                                    forestDistrictOrgUnitIds.contains(String.valueOf(proj.getForestDistrictOrgUnitId()))) &&
                            (fireCentreOrgUnitIds == null || fireCentreOrgUnitIds.isEmpty() ||
                                    fireCentreOrgUnitIds.contains(String.valueOf(proj.getFireCentreOrgUnitId())));

            if (!projectMatches) continue;

            List<ProjectFiscalEntity> fiscals = fiscalsByProjectGuid.getOrDefault(proj.getProjectGuid(), Collections.emptyList());

            if (fiscals.isEmpty()) continue;

            // Search text filter
            if (searchText != null && !searchText.isBlank()) {
                String lowered = searchText.toLowerCase();
                boolean match = matchesSearchText(proj, fiscals, lowered);
                if (!match) continue;
            }

            Map<String, Object> projectMap = new HashMap<>();
            projectMap.put("project", createProjectProperties(proj));

            // Get the boundaries for this project
            List<ProjectBoundaryEntity> projectBoundaries = boundariesByProjectGuid.getOrDefault(proj.getProjectGuid(), Collections.emptyList());

            // Only add the latest boundary if it exists
            if (!projectBoundaries.isEmpty()) {
                ProjectBoundaryEntity latestBoundary = findLatestBoundary(projectBoundaries);
                if (latestBoundary != null
                        && latestBoundary.getBoundaryGeometry() != null
                        && !latestBoundary.getBoundaryGeometry().isEmpty()) {
                    List<Map<String, Object>> boundariesList = new ArrayList<>();

                    // Create boundary entry with geometry
                    Map<String, Object> boundaryMap = new HashMap<>();

                    // Use the createPolygonFeature to handle the geometry conversion
                    Map<String, Object> polygonFeature = createPolygonFeature(
                            latestBoundary.getBoundaryGeometry(),
                            Collections.emptyMap()
                    );

                    // Extract just the geometry part from the feature
                    if (polygonFeature != null && !polygonFeature.isEmpty()) {
                        boundaryMap.put("boundaryGeometry", polygonFeature.get("geometry"));
                        boundariesList.add(boundaryMap);
                    }

                    // If there's a point geometry, add it too
                    if (latestBoundary.getLocationGeometry() != null
                            && !latestBoundary.getLocationGeometry().isEmpty()) {
                        Map<String, Object> pointFeature = createPointFeature(
                                latestBoundary.getLocationGeometry(),
                                Collections.emptyMap()
                        );

                        if (pointFeature != null && !pointFeature.isEmpty()) {
                            Map<String, Object> pointBoundaryMap = new HashMap<>();
                            pointBoundaryMap.put("locationGeometry", pointFeature.get("geometry")); // Use just the geometry
                            boundariesList.add(pointBoundaryMap);
                        }
                    }

                    projectMap.put("projectBoundaries", boundariesList);
                }
            }

            // Add fiscal information
            List<Map<String, Object>> fiscalPropsList = new ArrayList<>();
            for (ProjectFiscalEntity fiscal : fiscals) {
                Map<String, Object> fiscalMap = createProjectFiscalProperties(fiscal);

                // Get activities for this fiscal
                List<ActivityEntity> activities = activitiesByProjectFiscalGuid.getOrDefault(
                        fiscal.getProjectPlanFiscalGuid(), Collections.emptyList());

                if (!activities.isEmpty()) {
                    List<Map<String, Object>> activityMapList = new ArrayList<>();

                    for (ActivityEntity activity : activities) {
                        Map<String, Object> activityMap = new HashMap<>();

                        // Get boundaries for this activity
                        List<ActivityBoundaryEntity> activityBoundaries = boundariesByActivityGuid.getOrDefault(
                                activity.getActivityGuid(), Collections.emptyList());

                        if (!activityBoundaries.isEmpty()) {
                            List<Map<String, Object>> activityBoundariesList = new ArrayList<>();

                            for (ActivityBoundaryEntity activityBoundary : activityBoundaries) {
                                Map<String, Object> boundaryMap = new HashMap<>();

                                // Add only the geometry
                                if (activityBoundary.getGeometry() != null &&
                                        !activityBoundary.getGeometry().isEmpty()) {
                                    Map<String, Object> polygonFeature = createPolygonFeature(
                                            activityBoundary.getGeometry(),
                                            Collections.emptyMap()
                                    );

                                    if (polygonFeature != null && !polygonFeature.isEmpty()) {
                                        boundaryMap.put("activityGeometry", polygonFeature.get("geometry"));
                                        activityBoundariesList.add(boundaryMap);
                                    }
                                }
                            }

                            activityMap.put("activityBoundaries", activityBoundariesList);
                        }

                        activityMapList.add(activityMap);
                    }

                    fiscalMap.put("activities", activityMapList);
                }

                fiscalPropsList.add(fiscalMap);
            }

            projectMap.put("projectFiscals", fiscalPropsList);
            projects.add(projectMap);
        }

        Map<String, Object> projectCollection = new HashMap<>();
        projectCollection.put("projects", projects);
        return projectCollection;
    }

    private ProjectBoundaryEntity findLatestBoundary(List<ProjectBoundaryEntity> boundaries) {
        if (boundaries == null || boundaries.isEmpty()) {
            return null;
        }

        // Sort boundaries by update date (descending) and return the first one
        return boundaries.stream()
                .sorted(Comparator.comparing(ProjectBoundaryEntity::getUpdateDate).reversed())
                .findFirst()
                .orElse(null);
    }

    private boolean matchesSearchText(ProjectEntity project, List<ProjectFiscalEntity> fiscals, String searchText) {
        return Stream.of(
                        project.getProjectName(),
                        project.getProjectLead(),
                        project.getProjectDescription(),
                        project.getClosestCommunityName(),
                        project.getSiteUnitName(),
                        String.valueOf(project.getProjectNumber())
                )
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .anyMatch(value -> value.contains(searchText))
                ||
                fiscals.stream().anyMatch(fiscal ->
                        Stream.of(
                                        fiscal.getProjectFiscalName(),
                                        fiscal.getFirstNationsPartner(),
                                        fiscal.getOtherPartner()
                                )
                                .filter(Objects::nonNull)
                                .map(String::toLowerCase)
                                .anyMatch(value -> value.contains(searchText))
                );
    }


    Map<String, Object> createPointFeature(@NotNull Point point, Map<String, Object> properties) {
        try {
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
        }catch (Exception e) {
            log.error("Exception while creating Point feature {}", e.getMessage());
        } return Collections.emptyMap();
    }

    public static Map<String, Object> createPolygonFeature(@NotNull MultiPolygon multiPolygon, Map<String, Object> properties) {
        try {
            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");

            Map<String, Object> geometry = new HashMap<>();
            geometry.put("type", "MultiPolygon");

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
        } catch (Exception e) {
            log.error("Exception while creating Multipolygon feature {}", e.getMessage());
        } return Collections.emptyMap();

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

    Map<String, Object> createProjectProperties(ProjectEntity project) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("projectName", project.getProjectName());
        properties.put("projectGuid", project.getProjectGuid());
        properties.put("projectNumber", project.getProjectNumber());
        properties.put("programAreaGuid", project.getProgramAreaGuid());
        properties.put("forestRegionOrgUnitId", project.getForestRegionOrgUnitId());
        properties.put("forestDistrictOrgUnitId", project.getForestDistrictOrgUnitId());
        properties.put("fireCentreOrgUnitId", project.getFireCentreOrgUnitId());
        properties.put("projectLead", project.getProjectLead());
        properties.put("projectDescription", project.getProjectDescription());
        properties.put("closestCommunityName", project.getClosestCommunityName());
        properties.put("siteUnitName", project.getSiteUnitName());

        return properties;
    }

    private Map<String, Object> createProjectFiscalProperties(ProjectFiscalEntity fiscal) {
        Map<String, Object> props = new HashMap<>();
        props.put("projectFiscalGuid", fiscal.getProjectPlanFiscalGuid());
        props.put("projectGuid", fiscal.getProject().getProjectGuid());
        props.put("projectFiscalName", fiscal.getProjectFiscalName());
        props.put("projectFiscalStatus", fiscal.getPlanFiscalStatusCode());
        props.put("fiscalYear", fiscal.getFiscalYear());
        props.put("activityCategoryCode", fiscal.getActivityCategoryCode());
        props.put("firstNationsPartner", fiscal.getFirstNationsPartner());
        props.put("otherPartner", fiscal.getOtherPartner());

        return props;
    }


}


