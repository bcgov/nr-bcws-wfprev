package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.common.exceptions.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class FeaturesService implements CommonService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String GEOMETRY = "geometry";
    private static final String PROJECT_FISCALS = "projectFiscals";
    private static final String FISCAL_YEAR = "fiscalYear";
    private static final String ACTIVITY_CATEGORY_CODE = "activityCategoryCode";
    private static final String PLAN_FISCAL_STATUS_CODE = "planFiscalStatusCode";
    private static final String PROJECT_GUID = "projectGuid";

    public Map<String, Object> getAllFeatures(FeatureQueryParams params) throws ServiceException {
        try {
            List<ProjectEntity> filteredProjects = findFilteredProjects(params);
            List<Map<String, Object>> projects = new ArrayList<>();

            for (ProjectEntity project : filteredProjects) {
                Map<String, Object> projectMap = createProjectProperties(project);
                addProjectBoundaries(project, projectMap);
                addProjectFiscals(project, params, projectMap);
                projects.add(projectMap);
            }

            return Map.of("projects", projects);
        }catch (Exception e){
            log.error("Error encountered while fetching features:", e);
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    void addProjectBoundaries(ProjectEntity project, Map<String, Object> projectMap) {
        ProjectBoundaryEntity latestBoundary = findLatestProjectBoundary(project.getProjectGuid());
        if (latestBoundary != null) {
            List<Map<String, Object>> boundariesList = processProjectBoundary(latestBoundary);
            if (!boundariesList.isEmpty()) {
                projectMap.put("projectBoundaries", boundariesList);
            }
        }
    }

    void addProjectFiscals(ProjectEntity project, FeatureQueryParams params, Map<String, Object> projectMap) {
        List<ProjectFiscalEntity> projectFiscals = findFilteredProjectFiscals(
                project.getProjectGuid(),
                params.getFiscalYears(),
                params.getActivityCategoryCodes(),
                params.getPlanFiscalStatusCodes()
        );

        if (!projectFiscals.isEmpty()) {
            List<Map<String, Object>> fiscalPropsList = new ArrayList<>();
            for (ProjectFiscalEntity fiscal : projectFiscals) {
                Map<String, Object> fiscalMap = createProjectFiscalProperties(fiscal);
                addActivitiesToFiscal(fiscal, fiscalMap);
                fiscalPropsList.add(fiscalMap);
            }
            projectMap.put(PROJECT_FISCALS, fiscalPropsList);
        }
    }

    void addActivitiesToFiscal(ProjectFiscalEntity fiscal, Map<String, Object> fiscalMap) {
        List<ActivityEntity> activities = findActivitiesByProjectFiscal(fiscal.getProjectPlanFiscalGuid());
        if (!activities.isEmpty()) {
            List<Map<String, Object>> activityMapList = new ArrayList<>();
            for (ActivityEntity activity : activities) {
                Map<String, Object> activityMap = new HashMap<>();
                addActivityBoundaries(activity, activityMap);
                activityMapList.add(activityMap);
            }
            fiscalMap.put("activities", activityMapList);
        }
    }

    void addActivityBoundaries(ActivityEntity activity, Map<String, Object> activityMap) {
        List<ActivityBoundaryEntity> activityBoundaries = findActivityBoundaries(activity.getActivityGuid());
        if (!activityBoundaries.isEmpty()) {
            List<Map<String, Object>> activityBoundariesList = new ArrayList<>();
            for (ActivityBoundaryEntity activityBoundary : activityBoundaries) {
                addActivityBoundaryGeometry(activityBoundary, activityBoundariesList);
            }
            activityMap.put("activityBoundaries", activityBoundariesList);
        }
    }

    void addActivityBoundaryGeometry(ActivityBoundaryEntity activityBoundary, List<Map<String, Object>> activityBoundariesList) {
        if (activityBoundary.getGeometry() != null && !activityBoundary.getGeometry().isEmpty()) {
            Map<String, Object> boundaryMap = new HashMap<>();
            Map<String, Object> polygonFeature = createPolygonFeature(activityBoundary.getGeometry(), Collections.emptyMap());
            if (!polygonFeature.isEmpty()) {
                boundaryMap.put("activityGeometry", polygonFeature.get(GEOMETRY));
                activityBoundariesList.add(boundaryMap);
            }
        }
    }

    private List<Map<String, Object>> processProjectBoundary(ProjectBoundaryEntity latestBoundary) {
        List<Map<String, Object>> boundariesList = new ArrayList<>();
        processBoundaryGeometry(latestBoundary, boundariesList);
        processLocationGeometry(latestBoundary, boundariesList);
        return boundariesList;
    }

    private void processBoundaryGeometry(ProjectBoundaryEntity latestBoundary, List<Map<String, Object>> boundariesList) {
        if (latestBoundary.getBoundaryGeometry() != null && !latestBoundary.getBoundaryGeometry().isEmpty()) {
            Map<String, Object> boundaryMap = new HashMap<>();
            Map<String, Object> polygonFeature = createPolygonFeature(latestBoundary.getBoundaryGeometry(), Collections.emptyMap());
            if (!polygonFeature.isEmpty()) {
                boundaryMap.put("boundaryGeometry", polygonFeature.get(GEOMETRY));
                boundariesList.add(boundaryMap);
            }
        }
    }

    private void processLocationGeometry(ProjectBoundaryEntity latestBoundary, List<Map<String, Object>> boundariesList) {
        if (latestBoundary.getLocationGeometry() != null && !latestBoundary.getLocationGeometry().isEmpty()) {
            Map<String, Object> pointFeature = createPointFeature(latestBoundary.getLocationGeometry(), Collections.emptyMap());
            if (!pointFeature.isEmpty()) {
                Map<String, Object> pointBoundaryMap = new HashMap<>();
                pointBoundaryMap.put("locationGeometry", pointFeature.get(GEOMETRY));
                boundariesList.add(pointBoundaryMap);
            }
        }
    }


    List<ProjectEntity> findFilteredProjects(FeatureQueryParams params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProjectEntity> query = cb.createQuery(ProjectEntity.class);
        Root<ProjectEntity> project = query.from(ProjectEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        addProjectLevelFilters(project, predicates, params);
        addFiscalAttributeFilters(cb, project, predicates, params);
        addSearchTextFilters(cb, project, predicates, params);

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        query.distinct(true);

        return entityManager.createQuery(query).getResultList();
    }

    void addProjectLevelFilters(Root<ProjectEntity> project, List<Predicate> predicates, FeatureQueryParams params) {
        if (params.getProgramAreaGuids() != null && !params.getProgramAreaGuids().isEmpty()) {
            predicates.add(project.get("programAreaGuid").in(params.getProgramAreaGuids()));
        }

        if (params.getForestRegionOrgUnitIds() != null && !params.getForestRegionOrgUnitIds().isEmpty()) {
            predicates.add(project.get("forestRegionOrgUnitId").in(params.getForestRegionOrgUnitIds()));
        }

        if (params.getForestDistrictOrgUnitIds() != null && !params.getForestDistrictOrgUnitIds().isEmpty()) {
            predicates.add(project.get("forestDistrictOrgUnitId").in(params.getForestDistrictOrgUnitIds()));
        }

        if (params.getFireCentreOrgUnitIds() != null && !params.getFireCentreOrgUnitIds().isEmpty()) {
            predicates.add(project.get("fireCentreOrgUnitId").in(params.getFireCentreOrgUnitIds()));
        }
    }

    void addFiscalAttributeFilters(CriteriaBuilder cb, Root<ProjectEntity> project, List<Predicate> predicates, FeatureQueryParams params) {
        if ((params.getFiscalYears() != null && !params.getFiscalYears().isEmpty()) ||
                (params.getActivityCategoryCodes() != null && !params.getActivityCategoryCodes().isEmpty()) ||
                (params.getPlanFiscalStatusCodes() != null && !params.getPlanFiscalStatusCodes().isEmpty())) {

            Join<ProjectEntity, ProjectFiscalEntity> fiscal = project.join(PROJECT_FISCALS, JoinType.INNER);

            addFiscalYearFilters(cb, fiscal, predicates, params.getFiscalYears());
            addActivityCategoryCodeFilters(fiscal, predicates, params.getActivityCategoryCodes());
            addPlanFiscalStatusCodeFilters(fiscal, predicates, params.getPlanFiscalStatusCodes());
        }
    }

    void addFiscalYearFilters(CriteriaBuilder cb, Join<ProjectEntity, ProjectFiscalEntity> fiscal, List<Predicate> predicates, List<String> fiscalYears) {
        if (fiscalYears != null && !fiscalYears.isEmpty()) {
            List<Predicate> fiscalYearPredicates = new ArrayList<>();
            for (String year : fiscalYears) {
                fiscalYearPredicates.add(cb.like(fiscal.get(FISCAL_YEAR).as(String.class), year + "%"));
            }
            predicates.add(cb.or(fiscalYearPredicates.toArray(new Predicate[0])));
        }
    }

    void addActivityCategoryCodeFilters(Join<ProjectEntity, ProjectFiscalEntity> fiscal, List<Predicate> predicates, List<String> activityCategoryCodes) {
        if (activityCategoryCodes != null && !activityCategoryCodes.isEmpty()) {
            predicates.add(fiscal.get(ACTIVITY_CATEGORY_CODE).in(activityCategoryCodes));
        }
    }

    void addPlanFiscalStatusCodeFilters(Join<ProjectEntity, ProjectFiscalEntity> fiscal, List<Predicate> predicates, List<String> planFiscalStatusCodes) {
        if (planFiscalStatusCodes != null && !planFiscalStatusCodes.isEmpty()) {
            predicates.add(fiscal.get(PLAN_FISCAL_STATUS_CODE).in(planFiscalStatusCodes));
        }
    }

    void addSearchTextFilters(CriteriaBuilder cb, Root<ProjectEntity> project, List<Predicate> predicates, FeatureQueryParams params) {
        if (params.getSearchText() != null && !params.getSearchText().isBlank()) {
            String likeParam = "%" + params.getSearchText().toLowerCase() + "%";

            List<Predicate> searchPredicates = new ArrayList<>();
            searchPredicates.add(cb.like(cb.lower(project.get("projectName")), likeParam));
            searchPredicates.add(cb.like(cb.lower(project.get("projectLead")), likeParam));
            searchPredicates.add(cb.like(cb.lower(project.get("projectDescription")), likeParam));
            searchPredicates.add(cb.like(cb.lower(project.get("closestCommunityName")), likeParam));
            searchPredicates.add(cb.like(cb.lower(project.get("siteUnitName")), likeParam));
            searchPredicates.add(cb.like(cb.lower(project.get("projectNumber").as(String.class)), likeParam));

            // Join with fiscal for additional search filters
            Join<ProjectEntity, ProjectFiscalEntity> fiscal = project.join(PROJECT_FISCALS, JoinType.LEFT);
            searchPredicates.add(cb.like(cb.lower(fiscal.get("projectFiscalName")), likeParam));
            searchPredicates.add(cb.like(cb.lower(fiscal.get("firstNationsPartner")), likeParam));
            searchPredicates.add(cb.like(cb.lower(fiscal.get("otherPartner")), likeParam));

            predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
        }
    }

    List<ProjectFiscalEntity> findFilteredProjectFiscals(
            UUID projectGuid,
            List<String> fiscalYears,
            List<String> activityCategoryCodes,
            List<String> planFiscalStatusCodes
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProjectFiscalEntity> query = cb.createQuery(ProjectFiscalEntity.class);
        Root<ProjectFiscalEntity> fiscal = query.from(ProjectFiscalEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // Filter by project GUID
        predicates.add(cb.equal(fiscal.get("project").get(PROJECT_GUID), projectGuid));

        // Fiscal year filter
        if (fiscalYears != null && !fiscalYears.isEmpty()) {
            List<Predicate> fiscalYearPredicates = new ArrayList<>();
            for (String year : fiscalYears) {
                fiscalYearPredicates.add(cb.like(fiscal.get(FISCAL_YEAR).as(String.class), year + "%"));
            }
            predicates.add(cb.or(fiscalYearPredicates.toArray(new Predicate[0])));
        }

        // Activity category filter
        if (activityCategoryCodes != null && !activityCategoryCodes.isEmpty()) {
            predicates.add(fiscal.get(ACTIVITY_CATEGORY_CODE).in(activityCategoryCodes));
        }

        // Plan fiscal status filter
        if (planFiscalStatusCodes != null && !planFiscalStatusCodes.isEmpty()) {
            predicates.add(fiscal.get(PLAN_FISCAL_STATUS_CODE).in(planFiscalStatusCodes));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }

    List<ActivityEntity> findActivitiesByProjectFiscal(UUID projectFiscalGuid) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ActivityEntity> query = cb.createQuery(ActivityEntity.class);
        Root<ActivityEntity> activity = query.from(ActivityEntity.class);

        query.where(cb.equal(activity.get("projectPlanFiscalGuid"), projectFiscalGuid));

        return entityManager.createQuery(query).getResultList();
    }

    List<ActivityBoundaryEntity> findActivityBoundaries(UUID activityGuid) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ActivityBoundaryEntity> query = cb.createQuery(ActivityBoundaryEntity.class);
        Root<ActivityBoundaryEntity> boundary = query.from(ActivityBoundaryEntity.class);

        query.where(cb.equal(boundary.get("activityGuid"), activityGuid));

        return entityManager.createQuery(query).getResultList();
    }

    ProjectBoundaryEntity findLatestProjectBoundary(UUID projectGuid) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProjectBoundaryEntity> query = cb.createQuery(ProjectBoundaryEntity.class);
        Root<ProjectBoundaryEntity> boundary = query.from(ProjectBoundaryEntity.class);

        query.where(cb.equal(boundary.get(PROJECT_GUID), projectGuid));
        query.orderBy(cb.desc(boundary.get("updateDate")));

        List<ProjectBoundaryEntity> results = entityManager.createQuery(query)
                .setMaxResults(1)  // Only get the first result (most recent)
                .getResultList();

        return results.isEmpty() ? null : results.getFirst();
    }

    private Map<String, Object> createProjectProperties(ProjectEntity project) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("projectName", project.getProjectName());
        properties.put(PROJECT_GUID, project.getProjectGuid());
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
        props.put(PROJECT_GUID, fiscal.getProject().getProjectGuid());
        props.put("projectFiscalName", fiscal.getProjectFiscalName());
        props.put(PLAN_FISCAL_STATUS_CODE, fiscal.getPlanFiscalStatusCode());
        props.put(FISCAL_YEAR, fiscal.getFiscalYear());
        props.put(ACTIVITY_CATEGORY_CODE, fiscal.getActivityCategoryCode());
        props.put("firstNationsPartner", fiscal.getFirstNationsPartner());
        props.put("otherPartner", fiscal.getOtherPartner());

        return props;
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

            feature.put(GEOMETRY, geometry);
            feature.put("properties", properties);

            return feature;
        } catch (Exception e) {
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
            feature.put(GEOMETRY, geometry);
            feature.put("properties", properties);

            return feature;
        } catch (Exception e) {
            log.error("Exception while creating Multipolygon feature {}", e.getMessage());
        } return Collections.emptyMap();
    }

    static List<double[]> extractCoordinates(LinearRing linearRing) {
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
}