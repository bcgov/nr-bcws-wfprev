package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectLocationModel;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.hateoas.CollectionModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ProjectLocationServiceTest {

    @Mock private EntityManager entityManager;
    @InjectMocks private ProjectLocationService service;

    @Mock private CriteriaBuilder cb;
    @Mock private CriteriaQuery<Tuple> tupleQuery;
    @Mock private CriteriaQuery<Object> genericQuery;
    @Mock private Root<ProjectEntity> projectRoot;
    @Mock private Subquery<UUID> subquery;
    @Mock private Root<ProjectFiscalEntity> fiscalRoot;

    @Mock private TypedQuery<Tuple> typedQuery;

    @Mock private Path<Object> latPath;
    @Mock private Path<Object> lonPath;

    @Mock private Path<Object> projectTypeOuterPath;
    @Mock private Path<Object> projectTypeCodePath;
    @Mock private Path<Object> programAreaPath;
    @Mock private Path<Object> regionPath;
    @Mock private Path<Object> districtPath;
    @Mock private Path<Object> centrePath;

    @Mock private Predicate p1, p2, p3, pOr, pAnd, pExists;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllProjectLocations_mapsTuples() throws ServiceException {
        // Arrange
        FeatureQueryParams params = new FeatureQueryParams();
        params.setProgramAreaGuids(Collections.singletonList(UUID.randomUUID()));

        // Criteria setup actually used by the code path
        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createTupleQuery()).thenReturn(tupleQuery);
        when(tupleQuery.from(ProjectEntity.class)).thenReturn(projectRoot);

        // paths the service touches
        when(projectRoot.get("latitude")).thenReturn(latPath);
        when(projectRoot.get("longitude")).thenReturn(lonPath);
        Path<Object> guidPath = (Path<Object>) mock(Path.class);
        when(projectRoot.get("projectGuid")).thenReturn(guidPath);

        // Only coordinates predicate + programArea filter are needed
        when(cb.isNotNull(latPath)).thenReturn(p1);
        when(cb.isNotNull(lonPath)).thenReturn(p2);

        when(projectRoot.get("programAreaGuid")).thenReturn(programAreaPath);
        when(programAreaPath.in(anyCollection())).thenReturn(p3);

        // AND predicate passed to where(...)
        when(cb.and(any(Predicate[].class))).thenReturn(pAnd);

        // multiselect chaining (the service ignores the return of distinct(...), so no stub needed)
        when(tupleQuery.multiselect(any(), any(), any())).thenReturn(tupleQuery);

        when(entityManager.createQuery(tupleQuery)).thenReturn(typedQuery);

        // result tuples
        Tuple t1 = mock(Tuple.class);
        Tuple t2 = mock(Tuple.class);
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(t1.get("projectGuid", UUID.class)).thenReturn(id1);
        when(t1.get("latitude", BigDecimal.class)).thenReturn(new BigDecimal("49.100000"));
        when(t1.get("longitude", BigDecimal.class)).thenReturn(new BigDecimal("-123.100000"));

        when(t2.get("projectGuid", UUID.class)).thenReturn(id2);
        when(t2.get("latitude", BigDecimal.class)).thenReturn(new BigDecimal("50.200000"));
        when(t2.get("longitude", BigDecimal.class)).thenReturn(new BigDecimal("-122.200000"));

        when(typedQuery.getResultList()).thenReturn(List.of(t1, t2));

        // Act
        CollectionModel<ProjectLocationModel> result = service.getAllProjectLocations(params);

        // Assert
        assertNotNull(result);
        List<ProjectLocationModel> content = new ArrayList<>(result.getContent());
        assertEquals(2, content.size());
        assertEquals(id1.toString(), content.get(0).getProjectGuid());
        assertEquals(new BigDecimal("49.100000"), content.get(0).getLatitude());
        assertEquals(new BigDecimal("-123.100000"), content.get(0).getLongitude());
    }


    @Test
    void addProjectLevelFilters_addsPredicates_forAllProjectScopes() {
        FeatureQueryParams params = new FeatureQueryParams();
        params.setProgramAreaGuids(List.of(UUID.randomUUID()));
        params.setForestRegionOrgUnitIds(List.of("R1"));
        params.setForestDistrictOrgUnitIds(List.of("D1"));
        params.setFireCentreOrgUnitIds(List.of("C1"));
        params.setProjectTypeCodes(List.of("TYPE1"));

        List<Predicate> preds = new ArrayList<>();

        when(projectRoot.get("programAreaGuid")).thenReturn(programAreaPath);
        when(projectRoot.get("forestRegionOrgUnitId")).thenReturn(regionPath);
        when(projectRoot.get("forestDistrictOrgUnitId")).thenReturn(districtPath);
        when(projectRoot.get("fireCentreOrgUnitId")).thenReturn(centrePath);
        when(projectRoot.get("projectTypeCode")).thenReturn(projectTypeOuterPath);
        when(projectTypeOuterPath.get("projectTypeCode")).thenReturn(projectTypeCodePath);

        when(programAreaPath.in(anyCollection())).thenReturn(p1);
        when(regionPath.in(anyCollection())).thenReturn(p2);
        when(districtPath.in(anyCollection())).thenReturn(p3);
        when(centrePath.in(anyCollection())).thenReturn(mock(Predicate.class));
        when(projectTypeCodePath.in(anyCollection())).thenReturn(mock(Predicate.class));

        service.addProjectLevelFilters(projectRoot, preds, params);

        assertEquals(5, preds.size(), "Expected 5 project-level predicates appended");
        verify(projectRoot).get("programAreaGuid");
        verify(projectRoot).get("forestRegionOrgUnitId");
        verify(projectRoot).get("forestDistrictOrgUnitId");
        verify(projectRoot).get("fireCentreOrgUnitId");
        verify(projectRoot).get("projectTypeCode");
        verify(projectTypeOuterPath).get("projectTypeCode");
    }

    @Test
    @SuppressWarnings("unchecked")
    void addFiscalAttributeFilters_addsExistsPredicate_whenAnyFiscalFilterPresent() {
        FeatureQueryParams params = new FeatureQueryParams();
        // Ensure cb.or(...) is actually used
        params.setFiscalYears(List.of("2024", "2025"));
        List<Predicate> preds = new ArrayList<>();

        when(cb.createQuery()).thenReturn(genericQuery);
        when(genericQuery.subquery(UUID.class)).thenReturn(subquery);
        when(subquery.from(ProjectFiscalEntity.class)).thenReturn(fiscalRoot);

        // Strongly-typed UUID paths returned as raw Path in stubs
        Path<?> projectPath = mock(Path.class);
        Path<UUID> projectGuidPath = (Path<UUID>) mock(Path.class);
        Path<UUID> guidPathRoot = (Path<UUID>) mock(Path.class);

        when(fiscalRoot.get("project")).thenReturn((Path) projectPath);
        when(projectPath.get("projectGuid")).thenReturn((Path) projectGuidPath);
        when(projectRoot.get("projectGuid")).thenReturn((Path) guidPathRoot);

        Predicate eqProjectGuid = mock(Predicate.class);
        when(cb.equal(projectGuidPath, guidPathRoot)).thenReturn(eqProjectGuid);

        Path<Object> fyPath = (Path<Object>) mock(Path.class);
        Expression<String> fyAsString = (Expression<String>) mock(Expression.class);
        when(fiscalRoot.get("fiscalYear")).thenReturn(fyPath);
        when(fyPath.as(String.class)).thenReturn(fyAsString);

        Predicate likeFy1 = mock(Predicate.class);
        Predicate likeFy2 = mock(Predicate.class);
        when(cb.like(fyAsString, "2024%")).thenReturn(likeFy1);
        when(cb.like(fyAsString, "2025%")).thenReturn(likeFy2);

        when(cb.or(any(Predicate[].class))).thenReturn(pOr);
        when(cb.and(any(Predicate[].class))).thenReturn(pAnd);
        when(subquery.select(projectGuidPath)).thenReturn(subquery);
        when(subquery.where(pAnd)).thenReturn(subquery);
        when(cb.exists(subquery)).thenReturn(pExists);

        // Act
        service.addFiscalAttributeFilters(cb, projectRoot, preds, params);

        // Assert
        assertEquals(1, preds.size(), "EXISTS predicate should be appended");
        verify(cb).exists(subquery);
        verify(cb).or(any(Predicate[].class));
        verify(cb, atLeast(2)).like(eq(fyAsString), anyString());
    }
    @Test
    void addFiscalAttributeFilters_returnsImmediately_whenNoFilters() {
        FeatureQueryParams params = new FeatureQueryParams();
        List<Predicate> preds = new ArrayList<>();

        service.addFiscalAttributeFilters(cb, projectRoot, preds, params);

        assertTrue(preds.isEmpty());
        verifyNoInteractions(cb);
    }

    @Test @SuppressWarnings({"unchecked", "rawtypes"})
    void addFiscalAttributeFilters_addsExists_whenFiscalYearProvided() {
        FeatureQueryParams params = new FeatureQueryParams();
        params.setFiscalYears(List.of("2024"));

        List<Predicate> preds = new ArrayList<>();

        when(cb.createQuery()).thenReturn(genericQuery);
        Subquery<UUID> sqMatch = (Subquery<UUID>) mock(Subquery.class);
        when(genericQuery.subquery(UUID.class)).thenReturn(sqMatch);
        Root<ProjectFiscalEntity> fMatch = (Root<ProjectFiscalEntity>) mock(Root.class);
        when(sqMatch.from(ProjectFiscalEntity.class)).thenReturn(fMatch);

        Path<?> projPathMatch = mock(Path.class);
        Path<UUID> projectGuidPathMatch = (Path<UUID>) mock(Path.class);
        Path<UUID> rootGuidPath = (Path<UUID>) mock(Path.class);
        when(fMatch.get("project")).thenReturn((Path) projPathMatch);
        when(projPathMatch.get("projectGuid")).thenReturn((Path) projectGuidPathMatch);
        when(projectRoot.get("projectGuid")).thenReturn((Path) rootGuidPath);

        Predicate eqProjectGuid = mock(Predicate.class);
        when(cb.equal(projectGuidPathMatch, rootGuidPath)).thenReturn(eqProjectGuid);

        Path<Object> fyPath = (Path<Object>) mock(Path.class);
        when(fMatch.get("fiscalYear")).thenReturn(fyPath);
        Path<String> fyAsString = (Path<String>) mock(Path.class);
        when(fyPath.as(String.class)).thenReturn(fyAsString);
        Predicate like2024 = mock(Predicate.class);
        when(cb.like(fyAsString, "2024%")).thenReturn(like2024);

        Predicate pAnd = mock(Predicate.class);
        when(cb.and(any(Predicate[].class))).thenReturn(pAnd);
        when(sqMatch.select(projectGuidPathMatch)).thenReturn(sqMatch);
        when(sqMatch.where(pAnd)).thenReturn(sqMatch);

        Predicate pExists = mock(Predicate.class);
        when(cb.exists(sqMatch)).thenReturn(pExists);

        service.addFiscalAttributeFilters(cb, projectRoot, preds, params);

        assertEquals(1, preds.size());
        assertSame(pExists, preds.get(0));

        verify(cb).like(fyAsString, "2024%");
        verify(cb).exists(sqMatch);
    }

    @Test @SuppressWarnings({"unchecked", "rawtypes"})
    void addFiscalAttributeFilters_treatsStringNullAsIsNull_andAllowsProjectsWithoutFiscal() {
        FeatureQueryParams params = new FeatureQueryParams();
        params.setFiscalYears(List.of("null"));

        List<Predicate> preds = new ArrayList<>();

        when(cb.createQuery()).thenReturn(genericQuery);

        Subquery<UUID> sqMatch = (Subquery<UUID>) mock(Subquery.class);
        Subquery<UUID> sqAny   = (Subquery<UUID>) mock(Subquery.class);
        when(genericQuery.subquery(UUID.class)).thenReturn(sqMatch, sqAny);

        Root<ProjectFiscalEntity> fMatch = (Root<ProjectFiscalEntity>) mock(Root.class);
        Root<ProjectFiscalEntity> fAny   = (Root<ProjectFiscalEntity>) mock(Root.class);
        when(sqMatch.from(ProjectFiscalEntity.class)).thenReturn(fMatch);
        when(sqAny.from(ProjectFiscalEntity.class)).thenReturn(fAny);

        Path<?> projPathMatch = mock(Path.class);
        Path<?> projPathAny   = mock(Path.class);
        Path<UUID> guidMatch  = (Path<UUID>) mock(Path.class);
        Path<UUID> guidAny    = (Path<UUID>) mock(Path.class);
        Path<UUID> guidRoot   = (Path<UUID>) mock(Path.class);

        when(fMatch.get("project")).thenReturn((Path) projPathMatch);
        when(fAny.get("project")).thenReturn((Path) projPathAny);
        when(projPathMatch.get("projectGuid")).thenReturn((Path) guidMatch);
        when(projPathAny.get("projectGuid")).thenReturn((Path) guidAny);
        when(projectRoot.get("projectGuid")).thenReturn((Path) guidRoot);

        Predicate eqMatch = mock(Predicate.class);
        Predicate eqAny   = mock(Predicate.class);
        when(cb.equal(guidMatch, guidRoot)).thenReturn(eqMatch);
        when(cb.equal(guidAny,   guidRoot)).thenReturn(eqAny);

        // fiscalYear IS NULL in sqMatch
        Path<Object> fyPath = (Path<Object>) mock(Path.class);
        when(fMatch.get("fiscalYear")).thenReturn(fyPath);
        Predicate isNullFy = mock(Predicate.class);
        when(cb.isNull(fyPath)).thenReturn(isNullFy);

        // AND(...) for both subqueries
        Predicate pAndMatch = mock(Predicate.class);
        Predicate pAndAny   = mock(Predicate.class);
        when(cb.and(any(Predicate[].class))).thenReturn(pAndMatch, pAndAny);
        when(sqMatch.select(guidMatch)).thenReturn(sqMatch);
        when(sqAny.select(guidAny)).thenReturn(sqAny);
        when(sqMatch.where(pAndMatch)).thenReturn(sqMatch);
        when(sqAny.where(pAndAny)).thenReturn(sqAny);

        // EXISTS/NOT EXISTS
        Predicate pExistsMatch  = mock(Predicate.class);
        Predicate pExistsAny    = mock(Predicate.class);
        Predicate pNotExistsAny = mock(Predicate.class);
        when(cb.exists(sqMatch)).thenReturn(pExistsMatch);
        when(cb.exists(sqAny)).thenReturn(pExistsAny);
        when(cb.not(pExistsAny)).thenReturn(pNotExistsAny);

        // OR(...)
        Predicate pOrCombined = mock(Predicate.class);
        doReturn(pOrCombined).when(cb).or(any(Predicate.class), any(Predicate.class));
        // call
        service.addFiscalAttributeFilters(cb, projectRoot, preds, params);

        // assert + verify
        assertEquals(1, preds.size());
        assertSame(pOrCombined, preds.get(0));

        verify(cb).isNull(fyPath);
        verify(cb).exists(sqMatch);
        verify(cb).exists(sqAny);
        verify(cb).or(pExistsMatch, pNotExistsAny);
    }

    @Test
    @SuppressWarnings("unchecked")
    void addSearchTextFilters_buildsProjectAndFiscalExists() {
        FeatureQueryParams params = new FeatureQueryParams();
        params.setSearchText("firE"); // case-insensitive

        List<Predicate> preds = new ArrayList<>();

        // Project fields used in LIKEs
        Path<String> pName = (Path<String>) mock(Path.class);
        Path<String> pLead = (Path<String>) mock(Path.class);
        Path<String> pDesc = (Path<String>) mock(Path.class);
        Path<String> pComm = (Path<String>) mock(Path.class);
        Path<String> pSite = (Path<String>) mock(Path.class);
        Path<Object> pNum  = (Path<Object>)  mock(Path.class);
        Path<String> pRes  = (Path<String>)  mock(Path.class);

        when(projectRoot.get("projectName")).thenReturn((Path) pName);
        when(projectRoot.get("projectLead")).thenReturn((Path) pLead);
        when(projectRoot.get("projectDescription")).thenReturn((Path) pDesc);
        when(projectRoot.get("closestCommunityName")).thenReturn((Path) pComm);
        when(projectRoot.get("siteUnitName")).thenReturn((Path) pSite);
        when(projectRoot.get("projectNumber")).thenReturn((Path) pNum);
        when(projectRoot.get("resultsProjectCode")).thenReturn((Path) pRes);

        // projectNumber.as(String.class) is used in service
        Expression<String> projNumAsString = (Expression<String>) mock(Expression.class);
        when(pNum.as(String.class)).thenReturn(projNumAsString);

        // lower(...) returns its arg to keep stubbing simple
        when(cb.lower((Expression<String>) any())).thenAnswer(inv -> inv.getArgument(0));

        // like(...) returns predicate
        Predicate likePred = mock(Predicate.class);
        when(cb.like(any(Expression.class), eq("%fire%"))).thenReturn(likePred);

        // EXISTS subquery on fiscal fields
        when(cb.createQuery()).thenReturn(genericQuery);
        when(genericQuery.subquery(UUID.class)).thenReturn(subquery);
        when(subquery.from(ProjectFiscalEntity.class)).thenReturn(fiscalRoot);

        Path<?>   projectPath     = mock(Path.class);
        Path<UUID> guidFromFiscal = (Path<UUID>) mock(Path.class);
        Path<UUID> guidFromProj   = (Path<UUID>) mock(Path.class);

        when(fiscalRoot.get("project")).thenReturn((Path) projectPath);
        when(projectPath.get("projectGuid")).thenReturn((Path) guidFromFiscal);
        when(projectRoot.get("projectGuid")).thenReturn((Path) guidFromProj);

        Predicate eq = mock(Predicate.class);
        when(cb.equal(guidFromFiscal, guidFromProj)).thenReturn(eq);

        Path<String> pfName = (Path<String>) mock(Path.class);
        Path<String> pfFN   = (Path<String>) mock(Path.class);
        Path<String> pfOP   = (Path<String>) mock(Path.class);
        when(fiscalRoot.get("projectFiscalName")).thenReturn((Path) pfName);
        when(fiscalRoot.get("firstNationsPartner")).thenReturn((Path) pfFN);
        when(fiscalRoot.get("otherPartner")).thenReturn((Path) pfOP);

        // OR of fiscal LIKEs
        when(cb.or(any(Predicate[].class))).thenReturn(pOr);

        when(subquery.select(guidFromFiscal)).thenReturn(subquery);
        when(subquery.where(any(Predicate.class), any(Predicate.class))).thenReturn(subquery);

        when(cb.exists(subquery)).thenReturn(pExists);

        // top-level OR(project-like..., EXISTS(fiscal-like...))
        when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(pOr);

        // Act
        service.addSearchTextFilters(cb, projectRoot, preds, params);

        // Assert
        assertEquals(1, preds.size(), "One top-level OR predicate expected");
        verify(cb, atLeastOnce()).like(any(), eq("%fire%"));
        verify(cb).exists(subquery);

        // Optional: prove we called where with two preds
        verify(subquery).where(any(Predicate.class), any(Predicate.class));
    }


    @Test
    void getAllProjectLocations_wrapsUnexpectedException() {
        FeatureQueryParams params = new FeatureQueryParams();
        when(entityManager.getCriteriaBuilder()).thenThrow(new RuntimeException("boom"));

        assertThrows(ServiceException.class, () -> service.getAllProjectLocations(params));
    }
}
