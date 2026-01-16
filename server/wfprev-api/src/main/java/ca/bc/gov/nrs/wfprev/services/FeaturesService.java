package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
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
    private static final String PROJECT_PLAN_FISCAL_GUID = "projectPlanFiscalGuid";
    private static final String CREATE_USER = "createUser";
    private static final String UPDATE_USER = "updateUser";
    private static final String CREATE_DATE = "createDate";
    private static final String UPDATE_DATE = "updateDate";
    private static final String REVISION_COUNT = "revisionCount";


    public Map<String, Object> getAllFeatures(FeatureQueryParams params, int pageNumber, int pageRowCount) throws ServiceException {
        try {
            if (params.getProjectGuid() != null) {
                ProjectEntity project = entityManager.find(ProjectEntity.class, params.getProjectGuid());
                if (project == null) {
                    return Collections.emptyMap();
                }

                Map<String, Object> projectMap = createProjectProperties(project);
                addProjectBoundaries(project, projectMap);
                addProjectFiscals(project, params, projectMap);

                Map<String, Object> response = new HashMap<>();
                response.put("project", projectMap);
                return response;
            }
            long totalItems = countFilteredProjects(params);
            List<ProjectEntity> filteredProjects = findFilteredProjects(params, pageNumber, pageRowCount, params.getSortBy(), params.getSortDirection());
            List<Map<String, Object>> projects = new ArrayList<>();

            for (ProjectEntity project : filteredProjects) {
                Map<String, Object> projectMap = createProjectProperties(project);
                addProjectBoundaries(project, projectMap);
                addProjectFiscals(project, params, projectMap);
                projects.add(projectMap);
            }

            int totalPages = (int) Math.ceil((double) totalItems / pageRowCount);
            Map<String, Object> response = new HashMap<>();
            response.put("projects", projects);
            response.put("currentPage", pageNumber);
            response.put("pageSize", pageRowCount);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            return response;
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
                Map<String, Object> activityMap = createActivityProperties(activity);
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


    public List<ProjectEntity> findFilteredProjects(FeatureQueryParams params, int pageNumber, int pageRowCount, String sortBy, String sortDirection) {
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

        if ("projectName".equalsIgnoreCase(sortBy)) {
            if ("desc".equalsIgnoreCase(sortDirection)) {
                query.orderBy(cb.desc(project.get("projectName")));
            } else {
                query.orderBy(cb.asc(project.get("projectName")));
            }
        } else {
            // default
            query.orderBy(cb.asc(project.get("projectName")));
        }


        return entityManager.createQuery(query)
            .setFirstResult((pageNumber - 1) * pageRowCount)
            .setMaxResults(pageRowCount)
            .setHint("hibernate.query.passDistinctThrough", false)
            .getResultList();
    }

    void addProjectLevelFilters(Root<ProjectEntity> project, List<Predicate> predicates, FeatureQueryParams params) {
        log.info("Filtering by projectTypeCodes: {}", params.getProjectTypeCodes());
        if (params.getProjectGuid() != null) {
            predicates.add(project.get("projectGuid").in(params.getProjectGuid()));
            return;
        }
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

        if (params.getProjectTypeCodes() != null && !params.getProjectTypeCodes().isEmpty()) {
            predicates.add(
                project.get("projectTypeCode").get("projectTypeCode").in(params.getProjectTypeCodes())
            );
        }
    }

    void addFiscalAttributeFilters(CriteriaBuilder cb, Root<ProjectEntity> project, List<Predicate> predicates, FeatureQueryParams params) {
        if ((params.getFiscalYears() != null && !params.getFiscalYears().isEmpty()) ||
                (params.getActivityCategoryCodes() != null && !params.getActivityCategoryCodes().isEmpty()) ||
                (params.getPlanFiscalStatusCodes() != null && !params.getPlanFiscalStatusCodes().isEmpty())) {

            Join<ProjectEntity, ProjectFiscalEntity> fiscal = project.join(PROJECT_FISCALS, JoinType.LEFT);

            addFiscalYearFilters(cb, fiscal, predicates, params.getFiscalYears());
            addActivityCategoryCodeFilters(fiscal, predicates, params.getActivityCategoryCodes());
            addPlanFiscalStatusCodeFilters(fiscal, predicates, params.getPlanFiscalStatusCodes());
        }
    }

    void addFiscalYearFilters(CriteriaBuilder cb, Join<ProjectEntity, ProjectFiscalEntity> fiscal, List<Predicate> predicates, List<String> fiscalYears) {
        if (fiscalYears != null && !fiscalYears.isEmpty()) {
            List<Predicate> fiscalYearPredicates = new ArrayList<>();
            for (String year : fiscalYears) {
                if (year == null|| year.equals("null")) {
                    fiscalYearPredicates.add(cb.isNull(fiscal.get(FISCAL_YEAR)));
                } else {
                    fiscalYearPredicates.add(cb.like(fiscal.get(FISCAL_YEAR).as(String.class), year + "%"));
                }
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
            predicates.add(
                fiscal.get(PLAN_FISCAL_STATUS_CODE)
                    .get(PLAN_FISCAL_STATUS_CODE)
                    .in(planFiscalStatusCodes)
            );
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
            searchPredicates.add(cb.like(cb.lower(project.get("resultsProjectCode")), likeParam));
            // Join with fiscal for additional search filters
            Join<ProjectEntity, ProjectFiscalEntity> fiscal = project.join(PROJECT_FISCALS, JoinType.LEFT);
            searchPredicates.add(cb.like(cb.lower(fiscal.get("projectFiscalName")), likeParam));
            searchPredicates.add(cb.like(cb.lower(fiscal.get("firstNationsPartner")), likeParam));
            searchPredicates.add(cb.like(cb.lower(fiscal.get("otherPartner")), likeParam));

            predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
        }
    }

    public List<ProjectFiscalEntity> findFilteredProjectFiscals(
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
                if (year == null || year.equals("null")) {
                    fiscalYearPredicates.add(cb.isNull(fiscal.get(FISCAL_YEAR)));
                } else {
                    fiscalYearPredicates.add(cb.like(fiscal.get(FISCAL_YEAR).as(String.class), year + "%"));
                }
            }
            predicates.add(cb.or(fiscalYearPredicates.toArray(new Predicate[0])));
        }

        // Activity category filter
        if (activityCategoryCodes != null && !activityCategoryCodes.isEmpty()) {
            predicates.add(fiscal.get(ACTIVITY_CATEGORY_CODE).in(activityCategoryCodes));
        }

        // Plan fiscal status filter
        if (planFiscalStatusCodes != null && !planFiscalStatusCodes.isEmpty()) {
            predicates.add(
                fiscal.get(PLAN_FISCAL_STATUS_CODE)
                    .get(PLAN_FISCAL_STATUS_CODE)
                    .in(planFiscalStatusCodes)
            );
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }

    List<ActivityEntity> findActivitiesByProjectFiscal(UUID projectFiscalGuid) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ActivityEntity> query = cb.createQuery(ActivityEntity.class);
        Root<ActivityEntity> activity = query.from(ActivityEntity.class);

        query.where(cb.equal(activity.get(PROJECT_PLAN_FISCAL_GUID), projectFiscalGuid));

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
        query.orderBy(cb.desc(boundary.get(UPDATE_DATE)));

        List<ProjectBoundaryEntity> results = entityManager.createQuery(query)
                .setMaxResults(1)  // Only get the first result (most recent)
                .getResultList();

        return results.isEmpty() ? null : results.get(0);
    }

    private Map<String, Object> createProjectProperties(ProjectEntity project) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROJECT_GUID, project.getProjectGuid());
        properties.put("projectTypeCode", project.getProjectTypeCode() != null ? project.getProjectTypeCode().getProjectTypeCode() : null);
        properties.put("projectNumber", project.getProjectNumber());
        properties.put("siteUnitName", project.getSiteUnitName());
        properties.put("forestAreaCode", project.getForestAreaCode() != null ? project.getForestAreaCode().getForestAreaCode() : null);
        properties.put("generalScopeCode", project.getGeneralScopeCode() != null ? project.getGeneralScopeCode().getGeneralScopeCode() : null);
        properties.put("programAreaGuid", project.getProgramAreaGuid());
        properties.put("forestRegionOrgUnitId", project.getForestRegionOrgUnitId());
        properties.put("forestDistrictOrgUnitId", project.getForestDistrictOrgUnitId());
        properties.put("fireCentreOrgUnitId", project.getFireCentreOrgUnitId());
        properties.put("bcParksRegionOrgUnitId", project.getBcParksRegionOrgUnitId());
        properties.put("bcParksSectionOrgUnitId", project.getBcParksSectionOrgUnitId());
        properties.put("projectName", project.getProjectName());
        properties.put("projectLead", project.getProjectLead());
        properties.put("projectLeadEmailAddress", project.getProjectLeadEmailAddress());
        properties.put("projectDescription", project.getProjectDescription());
        properties.put("closestCommunityName", project.getClosestCommunityName());
        properties.put("totalEstimatedCostAmount", project.getTotalEstimatedCostAmount());
        properties.put("totalForecastAmount", project.getTotalForecastAmount());
        properties.put("totalPlannedProjectSizeHa", project.getTotalPlannedProjectSizeHa());
        properties.put("totalPlannedCostPerHectare", project.getTotalPlannedCostPerHectare());
        properties.put("totalActualAmount", project.getTotalActualAmount());
        properties.put("totalActualProjectSizeHa", project.getTotalActualProjectSizeHa());
        properties.put("totalActualCostPerHectareAmount", project.getTotalActualCostPerHectareAmount());
        properties.put("isMultiFiscalYearProj", project.getIsMultiFiscalYearProj());
        properties.put("latitude", project.getLatitude());
        properties.put("longitude", project.getLongitude());
        properties.put("lastProgressUpdateTimestamp", project.getLastProgressUpdateTimestamp());
        properties.put(REVISION_COUNT, project.getRevisionCount());
        properties.put("projectStatusCode", project.getProjectStatusCode() != null ? project.getProjectStatusCode().getProjectStatusCode() : null);
        properties.put(CREATE_USER, project.getCreateUser());
        properties.put(CREATE_DATE, project.getCreateDate());
        properties.put(UPDATE_USER, project.getUpdateUser());
        properties.put(UPDATE_DATE, project.getUpdateDate());
        properties.put("primaryObjectiveTypeCode", project.getPrimaryObjectiveTypeCode() != null ? project.getPrimaryObjectiveTypeCode().getObjectiveTypeCode() : null);
        properties.put("secondaryObjectiveTypeCode", project.getSecondaryObjectiveTypeCode() != null ? project.getSecondaryObjectiveTypeCode().getObjectiveTypeCode() : null);
        properties.put("tertiaryObjectiveTypeCode", project.getTertiaryObjectiveTypeCode() != null ? project.getTertiaryObjectiveTypeCode().getObjectiveTypeCode() : null);
        properties.put("secondaryObjectiveRationale", project.getSecondaryObjectiveRationale());
        properties.put("resultsProjectCode", project.getResultsProjectCode());
        return properties;
    }

    private Map<String, Object> createProjectFiscalProperties(ProjectFiscalEntity fiscal) {
        Map<String, Object> props = new HashMap<>();
        props.put(PROJECT_PLAN_FISCAL_GUID, fiscal.getProjectPlanFiscalGuid());
        props.put(PROJECT_GUID, fiscal.getProject().getProjectGuid());
        props.put(ACTIVITY_CATEGORY_CODE, fiscal.getActivityCategoryCode());
        props.put(FISCAL_YEAR, fiscal.getFiscalYear());
        props.put("ancillaryFundingProvider", fiscal.getAncillaryFundingProvider());
        props.put("projectPlanStatusCode", fiscal.getProjectPlanStatusCode());
        props.put(PLAN_FISCAL_STATUS_CODE, fiscal.getPlanFiscalStatusCode());
        props.put("endorsementCode", fiscal.getEndorsementCode());
        props.put("projectFiscalName", fiscal.getProjectFiscalName());
        props.put("projectFiscalDescription", fiscal.getProjectFiscalDescription());
        props.put("businessAreaComment", fiscal.getBusinessAreaComment());
        props.put("estimatedClwrrAllocAmount", fiscal.getEstimatedClwrrAllocAmount());
        props.put("totalCostEstimateAmount", fiscal.getTotalCostEstimateAmount());
        props.put("cfsProjectCode", fiscal.getCfsProjectCode());
        props.put("fiscalForecastAmount", fiscal.getFiscalForecastAmount());
        props.put("fiscalAncillaryFundAmount", fiscal.getFiscalAncillaryFundAmount());
        props.put("fiscalPlannedProjectSizeHa", fiscal.getFiscalPlannedProjectSizeHa());
        props.put("fiscalPlannedCostPerHaAmt", fiscal.getFiscalPlannedCostPerHaAmt());
        props.put("fiscalReportedSpendAmount", fiscal.getFiscalReportedSpendAmount());
        props.put("fiscalActualAmount", fiscal.getFiscalActualAmount());
        props.put("fiscalCompletedSizeHa", fiscal.getFiscalCompletedSizeHa());
        props.put("fiscalActualCostPerHaAmt", fiscal.getFiscalActualCostPerHaAmt());
        props.put("firstNationsDelivPartInd", fiscal.getFirstNationsDelivPartInd());
        props.put("firstNationsEngagementInd", fiscal.getFirstNationsEngagementInd());
        props.put("firstNationsPartner", fiscal.getFirstNationsPartner());
        props.put("otherPartner", fiscal.getOtherPartner());
        props.put("resultsNumber", fiscal.getResultsNumber());
        props.put("resultsOpeningId", fiscal.getResultsOpeningId());
        props.put("resultsContactEmail", fiscal.getResultsContactEmail());
        props.put("submittedByName", fiscal.getSubmittedByName());
        props.put("submittedByUserGuid", fiscal.getSubmittedByUserGuid());
        props.put("submittedByUserUserid", fiscal.getSubmittedByUserUserid());
        props.put("submissionTimestamp", fiscal.getSubmissionTimestamp());
        props.put("endorsementEvalTimestamp", fiscal.getEndorsementEvalTimestamp());
        props.put("endorserName", fiscal.getEndorserName());
        props.put("endorserUserGuid", fiscal.getEndorserUserGuid());
        props.put("endorserUserUserid", fiscal.getEndorserUserUserid());
        props.put("endorsementTimestamp", fiscal.getEndorsementTimestamp());
        props.put("endorsementComment", fiscal.getEndorsementComment());
        props.put("isApprovedInd", fiscal.getIsApprovedInd());
        props.put("approverName", fiscal.getApproverName());
        props.put("approverUserGuid", fiscal.getApproverUserGuid());
        props.put("approverUserUserid", fiscal.getApproverUserUserid());
        props.put("approvedTimestamp", fiscal.getApprovedTimestamp());
        props.put("accomplishmentsComment", fiscal.getAccomplishmentsComment());
        props.put("isDelayedInd", fiscal.getIsDelayedInd());
        props.put("delayRationale", fiscal.getDelayRationale());
        props.put("abandonedRationale", fiscal.getAbandonedRationale());
        props.put("lastProgressUpdateTimestamp", fiscal.getLastProgressUpdateTimestamp());
        props.put(REVISION_COUNT, fiscal.getRevisionCount());
        props.put(CREATE_USER, fiscal.getCreateUser());
        props.put(CREATE_DATE, fiscal.getCreateDate());
        props.put(UPDATE_USER, fiscal.getUpdateUser());
        props.put(UPDATE_DATE, fiscal.getUpdateDate());

        return props;
    }

    Map<String, Object> createActivityProperties(ActivityEntity activity) {
        Map<String, Object> map = new HashMap<>();
        map.put("activityGuid", activity.getActivityGuid());
        map.put(PROJECT_PLAN_FISCAL_GUID, activity.getProjectPlanFiscalGuid());
        map.put("activityStatusCode", activity.getActivityStatusCode() != null ? activity.getActivityStatusCode().getActivityStatusCode() : null);
        map.put("silvicultureBaseGuid", activity.getSilvicultureBaseGuid());
        map.put("silvicultureTechniqueGuid", activity.getSilvicultureTechniqueGuid());
        map.put("silvicultureMethodGuid", activity.getSilvicultureMethodGuid());
        map.put("riskRatingCode", activity.getRiskRatingCode() != null ? activity.getRiskRatingCode().getRiskRatingCode() : null);
        map.put("contractPhaseCode", activity.getContractPhaseCode() != null ? activity.getContractPhaseCode().getContractPhaseCode() : null);
        map.put("activityFundingSourceGuid", activity.getActivityFundingSourceGuid());
        map.put("activityName", activity.getActivityName());
        map.put("activityDescription", activity.getActivityDescription());
        map.put("activityStartDate", activity.getActivityStartDate());
        map.put("activityEndDate", activity.getActivityEndDate());
        map.put("plannedSpendAmount", activity.getPlannedSpendAmount());
        map.put("plannedTreatmentAreaHa", activity.getPlannedTreatmentAreaHa());
        map.put("reportedSpendAmount", activity.getReportedSpendAmount());
        map.put("completedAreaHa", activity.getCompletedAreaHa());
        map.put("isResultsReportableInd", activity.getIsResultsReportableInd());
        map.put("outstandingObligationsInd", activity.getOutstandingObligationsInd());
        map.put("activityComment", activity.getActivityComment());
        map.put("isSpatialAddedInd", activity.getIsSpatialAddedInd());
        map.put(REVISION_COUNT, activity.getRevisionCount());
        map.put(CREATE_USER, activity.getCreateUser());
        map.put(CREATE_DATE, activity.getCreateDate());
        map.put(UPDATE_USER, activity.getUpdateUser());
        map.put(UPDATE_DATE, activity.getUpdateDate());
        return map;
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

    long countFilteredProjects(FeatureQueryParams params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProjectEntity> project = countQuery.from(ProjectEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        addProjectLevelFilters(project, predicates, params);
        addFiscalAttributeFilters(cb, project, predicates, params);
        addSearchTextFilters(cb, project, predicates, params);

        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        countQuery.select(cb.countDistinct(project));
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
