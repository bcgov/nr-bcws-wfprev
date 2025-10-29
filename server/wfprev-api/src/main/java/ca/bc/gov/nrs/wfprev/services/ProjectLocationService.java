package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectLocationModel;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class ProjectLocationService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String PROJECT = "project";
    private static final String PROJECT_GUID = "projectGuid";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";

    public CollectionModel<ProjectLocationModel> getAllProjectLocations(FeatureQueryParams params) throws ServiceException {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Tuple> cq = cb.createTupleQuery();
            Root<ProjectEntity> project = cq.from(ProjectEntity.class);

            List<Predicate> predicates = new ArrayList<>();

            // Only return projects that have coordinates
            predicates.add(cb.isNotNull(project.get(LATITUDE)));
            predicates.add(cb.isNotNull(project.get(LONGITUDE)));

            // Apply project-level and fiscal-level filters
            addProjectLevelFilters(project, predicates, params);
            addFiscalAttributeFilters(cb, project, predicates, params);
            addSearchTextFilters(cb, project, predicates, params);

            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }

            // Only return the attributes needed for ProjectLocationEntity
            cq.multiselect(
                    project.get(PROJECT_GUID).alias(PROJECT_GUID),
                    project.get(LATITUDE).alias(LATITUDE),
                    project.get(LONGITUDE).alias(LONGITUDE)
            );

            List<Tuple> rows = entityManager.createQuery(cq).getResultList();

            List<ProjectLocationModel> models = new ArrayList<>();
            for (Tuple tuple : rows) {
                UUID projectGuid = tuple.get(PROJECT_GUID, UUID.class);
                BigDecimal lat = tuple.get(LATITUDE, BigDecimal.class);
                BigDecimal lon = tuple.get(LONGITUDE, BigDecimal.class);

                ProjectLocationModel model = new ProjectLocationModel();
                model.setProjectGuid(String.valueOf(projectGuid));
                model.setLatitude(lat);
                model.setLongitude(lon);
                models.add(model);
            }

            return CollectionModel.of(models);
        } catch (Exception e) {
            log.error("Error while fetching Project Locations", e);
            throw new ServiceException("Error while fetching Project Locations", e);
        }
    }

    void addProjectLevelFilters(Root<ProjectEntity> project, List<Predicate> predicates, FeatureQueryParams params) {
        // Collapse the repetitive "in" filters into a loop to avoid duplication flags.
        Map<String, Collection<?>> inFilters = new LinkedHashMap<>();
        inFilters.put("programAreaGuid", params.getProgramAreaGuids());
        inFilters.put("forestRegionOrgUnitId", params.getForestRegionOrgUnitIds());
        inFilters.put("forestDistrictOrgUnitId", params.getForestDistrictOrgUnitIds());
        inFilters.put("fireCentreOrgUnitId", params.getFireCentreOrgUnitIds());

        for (Map.Entry<String, Collection<?>> entry : inFilters.entrySet()) {
            if (notEmpty(entry.getValue())) {
                predicates.add(project.get(entry.getKey()).in(entry.getValue()));
            }
        }

        if (notEmpty(params.getProjectTypeCodes())) {
            predicates.add(
                    project.get("projectTypeCode").get("projectTypeCode").in(params.getProjectTypeCodes())
            );
        }
    }

    void addFiscalAttributeFilters(CriteriaBuilder cb, Root<ProjectEntity> project,
                                   List<Predicate> predicates, FeatureQueryParams params) {
        if (!hasAnyFiscalFilters(params)) return;

        // subquery that matches the fiscal filters
        Subquery<UUID> sqMatch = cb.createQuery().subquery(UUID.class);
        Root<ProjectFiscalEntity> fMatch = sqMatch.from(ProjectFiscalEntity.class);
        List<Predicate> match = new ArrayList<>();
        match.add(cb.equal(fMatch.get(PROJECT).get(PROJECT_GUID), project.get(PROJECT_GUID)));

        // fiscalYear
        if (notEmpty(params.getFiscalYears())) {
            Path<Object> fyPath = fMatch.get("fiscalYear");
            List<Predicate> years = new ArrayList<>();
            for (String year : params.getFiscalYears()) {
                if (year == null || "null".equals(year)) {
                    years.add(cb.isNull(fyPath));
                } else {
                    years.add(cb.like(fyPath.as(String.class), year + "%"));
                }
            }
            match.add(cb.or(years.toArray(new Predicate[0])));
        }

        // activityCategoryCode
        if (notEmpty(params.getActivityCategoryCodes())) {
            match.add(fMatch.get("activityCategoryCode").in(params.getActivityCategoryCodes()));
        }

        // planFiscalStatusCode
        if (notEmpty(params.getPlanFiscalStatusCodes())) {
            Path<String> statusCode = fMatch.get("planFiscalStatusCode").get("planFiscalStatusCode");
            match.add(statusCode.in(params.getPlanFiscalStatusCodes()));
        }

        sqMatch.select(fMatch.get(PROJECT).get(PROJECT_GUID))
                .where(cb.and(match.toArray(new Predicate[0])));

        // if "null" fiscal year is requested, allow projects with no fiscal rows
        // they can still have coordinates at the project level without a project fiscal
        boolean noFiscal = notEmpty(params.getFiscalYears()) &&
                params.getFiscalYears().stream().anyMatch(y -> y == null || "null".equals(y));

        if (noFiscal) {
            Subquery<UUID> sqAny = cb.createQuery().subquery(UUID.class);
            Root<ProjectFiscalEntity> fiscalAny = sqAny.from(ProjectFiscalEntity.class);
            sqAny.select(fiscalAny.get(PROJECT).get(PROJECT_GUID))
                    .where(cb.equal(fiscalAny.get(PROJECT).get(PROJECT_GUID), project.get(PROJECT_GUID)));

            // include if matches filters OR has zero fiscals
            predicates.add(cb.or(cb.exists(sqMatch), cb.not(cb.exists(sqAny))));
        } else {
            // regular path - require a fiscal row that matches the filters
            predicates.add(cb.exists(sqMatch));
        }
    }

    private boolean hasAnyFiscalFilters(FeatureQueryParams params) {
        return notEmpty(params.getFiscalYears())
                || notEmpty(params.getActivityCategoryCodes())
                || notEmpty(params.getPlanFiscalStatusCodes());
    }

    private static boolean notEmpty(Collection<?> c) {
        return c != null && !c.isEmpty();
    }

    void addSearchTextFilters(CriteriaBuilder cb, Root<ProjectEntity> project, List<Predicate> predicates, FeatureQueryParams params) {
        if (params.getSearchText() == null || params.getSearchText().isBlank()) return;

        String likeParam = "%" + params.getSearchText().toLowerCase() + "%";
        List<Predicate> search = new ArrayList<>();

        // Collapse the repeated like() calls into a loop
        for (String field : List.of(
                "projectName",
                "projectLead",
                "projectDescription",
                "closestCommunityName",
                "siteUnitName",
                "resultsProjectCode"
        )) {
            search.add(cb.like(cb.lower(project.get(field)), likeParam));
        }

        // projectNumber is numeric → cast to String
        search.add(cb.like(cb.lower(project.get("projectNumber").as(String.class)), likeParam));

        // Fiscal-level text search via EXISTS
        Subquery<UUID> s = cb.createQuery().subquery(UUID.class);
        Root<ProjectFiscalEntity> f = s.from(ProjectFiscalEntity.class);
        s.select(f.get(PROJECT).get(PROJECT_GUID))
                .where(
                        cb.equal(f.get(PROJECT).get(PROJECT_GUID), project.get(PROJECT_GUID)),
                        cb.or(
                                cb.like(cb.lower(f.get("projectFiscalName")), likeParam),
                                cb.like(cb.lower(f.get("firstNationsPartner")), likeParam),
                                cb.like(cb.lower(f.get("otherPartner")), likeParam)
                        )
                );

        predicates.add(cb.or(cb.or(search.toArray(new Predicate[0])), cb.exists(s)));
    }
}