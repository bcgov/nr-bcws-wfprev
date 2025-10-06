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
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ProjectLocationService {

    @PersistenceContext
    private EntityManager entityManager;

    public CollectionModel<ProjectLocationModel> getAllProjectLocations(FeatureQueryParams params) throws ServiceException {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Tuple> cq = cb.createTupleQuery();
            Root<ProjectEntity> project = cq.from(ProjectEntity.class);

            List<Predicate> predicates = new ArrayList<>();

            // Only return projects that have coordinates
            predicates.add(cb.isNotNull(project.get("latitude")));
            predicates.add(cb.isNotNull(project.get("longitude")));

            // Apply project-level and fiscal-level filters
            addProjectLevelFilters(project, predicates, params);
            addFiscalAttributeFilters(cb, project, predicates, params);
            addSearchTextFilters(cb, project, predicates, params);

            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }

            // Only return the attributes needed for ProjectLocationEntity
            cq.multiselect(
                    project.get("projectGuid").alias("projectGuid"),
                    project.get("latitude").alias("latitude"),
                    project.get("longitude").alias("longitude")
            );

            List<Tuple> rows = entityManager.createQuery(cq).getResultList();

            List<ProjectLocationModel> models = new ArrayList<>();
            for (Tuple tuple : rows) {
                UUID projectGuid = tuple.get("projectGuid", UUID.class);
                BigDecimal lat = tuple.get("latitude", BigDecimal.class);
                BigDecimal lon = tuple.get("longitude", BigDecimal.class);

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
        boolean needsFiscal =
                (params.getFiscalYears() != null && !params.getFiscalYears().isEmpty()) ||
                        (params.getActivityCategoryCodes() != null && !params.getActivityCategoryCodes().isEmpty()) ||
                        (params.getPlanFiscalStatusCodes() != null && !params.getPlanFiscalStatusCodes().isEmpty());

        if (!needsFiscal) return;

        Subquery<UUID> sq = cb.createQuery().subquery(UUID.class);
        Root<ProjectFiscalEntity> fiscal = sq.from(ProjectFiscalEntity.class);

        List<Predicate> pf = new ArrayList<>();
        pf.add(cb.equal(fiscal.get("project").get("projectGuid"), project.get("projectGuid")));

        if (params.getFiscalYears() != null && !params.getFiscalYears().isEmpty()) {
            List<Predicate> fiscalYears = new ArrayList<>();
            for (String year : params.getFiscalYears()) {
                if (year == null || "null".equals(year)) {
                    fiscalYears.add(cb.isNull(fiscal.get("fiscalYear")));
                } else {
                    fiscalYears.add(cb.like(fiscal.get("fiscalYear").as(String.class), year + "%"));
                }
            }
            pf.add(cb.or(fiscalYears.toArray(new Predicate[0])));
        }

        if (params.getActivityCategoryCodes() != null && !params.getActivityCategoryCodes().isEmpty()) {
            pf.add(fiscal.get("activityCategoryCode").in(params.getActivityCategoryCodes()));
        }

        if (params.getPlanFiscalStatusCodes() != null && !params.getPlanFiscalStatusCodes().isEmpty()) {
            pf.add(
                    fiscal.get("planFiscalStatusCode").get("planFiscalStatusCode")
                            .in(params.getPlanFiscalStatusCodes())
            );
        }

        sq.select(fiscal.get("project").get("projectGuid")).where(cb.and(pf.toArray(new Predicate[0])));
        predicates.add(cb.exists(sq));
    }

    void addSearchTextFilters(CriteriaBuilder cb, Root<ProjectEntity> project, List<Predicate> predicates, FeatureQueryParams params) {
        if (params.getSearchText() == null || params.getSearchText().isBlank()) return;

        String likeParam = "%" + params.getSearchText().toLowerCase() + "%";
        List<Predicate> search = new ArrayList<>();

        // Project-level text search
        search.add(cb.like(cb.lower(project.get("projectName")), likeParam));
        search.add(cb.like(cb.lower(project.get("projectLead")), likeParam));
        search.add(cb.like(cb.lower(project.get("projectDescription")), likeParam));
        search.add(cb.like(cb.lower(project.get("closestCommunityName")), likeParam));
        search.add(cb.like(cb.lower(project.get("siteUnitName")), likeParam));
        search.add(cb.like(cb.lower(project.get("projectNumber").as(String.class)), likeParam));
        search.add(cb.like(cb.lower(project.get("resultsProjectCode")), likeParam));

        // Fiscal-level text search via EXISTS
        Subquery<UUID> s = cb.createQuery().subquery(UUID.class);
        Root<ProjectFiscalEntity> f = s.from(ProjectFiscalEntity.class);
        s.select(f.get("project").get("projectGuid"))
                .where(cb.equal(f.get("project").get("projectGuid"), project.get("projectGuid")),
                        cb.or(
                                cb.like(cb.lower(f.get("projectFiscalName")), likeParam),
                                cb.like(cb.lower(f.get("firstNationsPartner")), likeParam),
                                cb.like(cb.lower(f.get("otherPartner")), likeParam)
                        )
                );

        predicates.add(cb.or(
                cb.or(search.toArray(new Predicate[0])),
                cb.exists(s)
        ));
    }
}
