package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectActivityResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectActivityEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectActivityModel;
import ca.bc.gov.nrs.wfprev.data.models.SpatialExportModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectActivityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.jackson.datatype.jts.JtsModule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpatialExportServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query nativeQuery;

    @Mock
    private ProjectActivityRepository projectActivityRepository;

    private ProjectActivityResourceAssembler projectActivityResourceAssembler;
    private ObjectMapper objectMapper;
    private SpatialExportService spatialExportService;

    private final Instant fixedDbClock = Instant.parse("2026-10-30T15:00:00Z");

    @BeforeEach
    void setUp() {
        projectActivityResourceAssembler = new ProjectActivityResourceAssembler();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JtsModule());
        spatialExportService = new SpatialExportService(
                entityManager,
                projectActivityRepository,
                projectActivityResourceAssembler,
                objectMapper
        );
    }

    private void mockDatabaseClock(Instant clock) {
        when(entityManager.createNativeQuery("SELECT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')")).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn(Timestamp.from(clock));
    }

    @Test
    void testExport_DeletedProjects() {
        mockDatabaseClock(fixedDbClock);

        Date since = Date.from(Instant.parse("2026-10-30T00:00:00Z"));
        UUID deletedProjectGuid = UUID.randomUUID();
        Date deletedLastUpdated = Date.from(Instant.parse("2026-10-30T10:00:00Z"));

        // Deleted project from view has activityGuid == null and rowId == projectGuid
        ProjectActivityEntity deletedEntity = ProjectActivityEntity.builder()
                .rowId(deletedProjectGuid)
                .projectGuid(deletedProjectGuid)
                .activityGuid(null)
                .projectNumber(null)
                .projectTypeCode(null)
                .geometry(null)
                .lastUpdated(deletedLastUpdated)
                .build();

        when(projectActivityRepository.findByLastUpdatedGreaterThanEqualOrderByLastUpdatedAsc(since))
                .thenReturn(List.of(deletedEntity));

        SpatialExportModel result = spatialExportService.getSpatialExport(since);

        assertThat(result).isNotNull();
        assertThat(result.getProjectActivities()).hasSize(1);

        ProjectActivityModel model = result.getProjectActivities().get(0);
        assertThat(model.getProjectGuid()).isEqualTo(deletedProjectGuid);
        assertThat(model.getLastUpdated()).isEqualTo(deletedLastUpdated);
        // All activity / spatial fields must be null for deleted project
        assertThat(model.getActivityGuid()).isNull();
        assertThat(model.getGeometry()).isNull();
        assertThat(model.getProjectNumber()).isNull();
        assertThat(model.getActivityName()).isNull();

        assertThat(result.getSince()).isEqualTo(Date.from(fixedDbClock));
    }

    @Test
    void testExport_EmptyResult() {
        mockDatabaseClock(fixedDbClock);

        Date since = Date.from(Instant.parse("2026-10-30T00:00:00Z"));

        when(projectActivityRepository.findByLastUpdatedGreaterThanEqualOrderByLastUpdatedAsc(since))
                .thenReturn(Collections.emptyList());

        SpatialExportModel result = spatialExportService.getSpatialExport(since);

        assertThat(result).isNotNull();
        assertThat(result.getProjectActivities()).isNotNull().isEmpty();
        assertThat(result.getSince()).isEqualTo(Date.from(fixedDbClock));
    }

    @Test
    void testExport_InvalidSince_NullThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> spatialExportService.getSpatialExport(null)
        );

        assertThat(exception.getMessage()).isEqualTo("'since' parameter is required");
    }

    @Test
    void testExport_ActiveAndCompletedProjects() {
        mockDatabaseClock(fixedDbClock);

        Date since = Date.from(Instant.parse("2026-10-30T00:00:00Z"));
        UUID projectGuid = UUID.randomUUID();
        UUID activityGuid = UUID.randomUUID();
        Date lastUpdated = Date.from(Instant.parse("2026-10-30T11:00:00Z"));

        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coords = new Coordinate[] {
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0),
                new Coordinate(0, 0)
        };
        Polygon poly = gf.createPolygon(coords);
        MultiPolygon multiPolygon = gf.createMultiPolygon(new Polygon[]{poly});

        ProjectActivityEntity activeEntity = ProjectActivityEntity.builder()
                .rowId(activityGuid)
                .projectGuid(projectGuid)
                .projectNumber(12345)
                .projectTypeCode("FUEL_MGMT")
                .projectTypeDesc("Fuel Management")
                .activityGuid(activityGuid)
                .activityName("Prescribed Fire")
                .activityStatusCode("COMPLETED")
                .activityStatusDesc("Completed")
                .completedAreaHa(new BigDecimal("12.5"))
                .geometry(multiPolygon)
                .lastUpdated(lastUpdated)
                .build();

        when(projectActivityRepository.findByLastUpdatedGreaterThanEqualOrderByLastUpdatedAsc(since))
                .thenReturn(List.of(activeEntity));

        SpatialExportModel result = spatialExportService.getSpatialExport(since);

        assertThat(result).isNotNull();
        assertThat(result.getProjectActivities()).hasSize(1);

        ProjectActivityModel model = result.getProjectActivities().get(0);
        assertThat(model.getProjectGuid()).isEqualTo(projectGuid);
        assertThat(model.getActivityGuid()).isEqualTo(activityGuid);
        assertThat(model.getProjectNumber()).isEqualTo(12345);
        assertThat(model.getProjectTypeCode()).isEqualTo("FUEL_MGMT");
        assertThat(model.getActivityName()).isEqualTo("Prescribed Fire");
        assertThat(model.getGeometry()).isNotNull();
        assertThat(model.getGeometry().getSRID()).isEqualTo(4326);
        assertThat(model.getLastUpdated()).isEqualTo(lastUpdated);

        assertThat(result.getSince()).isEqualTo(Date.from(fixedDbClock));
    }

    @Test
    void testExport_TruncationWhenExceedingMaxBytes() {
        mockDatabaseClock(fixedDbClock);

        Date since = Date.from(Instant.parse("2026-10-30T00:00:00Z"));
        Date update1 = Date.from(Instant.parse("2026-10-30T08:00:00Z"));
        Date update2 = Date.from(Instant.parse("2026-10-30T09:00:00Z"));

        ProjectActivityEntity entity1 = ProjectActivityEntity.builder()
                .rowId(UUID.randomUUID())
                .projectGuid(UUID.randomUUID())
                .activityGuid(null)
                .lastUpdated(update1)
                .build();

        ProjectActivityEntity entity2 = ProjectActivityEntity.builder()
                .rowId(UUID.randomUUID())
                .projectGuid(UUID.randomUUID())
                .activityGuid(null)
                .lastUpdated(update2)
                .build();

        when(projectActivityRepository.findByLastUpdatedGreaterThanEqualOrderByLastUpdatedAsc(since))
                .thenReturn(List.of(entity1, entity2));

        // Create a mock ObjectMapper that simulates large byte size for the second model
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        try {
            when(mockMapper.writeValueAsBytes(any(ProjectActivityModel.class)))
                    .thenReturn(new byte[1000]) // first item fits
                    .thenReturn(new byte[(int) SpatialExportService.MAX_RESPONSE_BYTES]); // second item exceeds
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        SpatialExportService serviceWithMockMapper = new SpatialExportService(
                entityManager,
                projectActivityRepository,
                projectActivityResourceAssembler,
                mockMapper
        );

        SpatialExportModel result = serviceWithMockMapper.getSpatialExport(since);

        // Only the first entity should be included
        assertThat(result.getProjectActivities()).hasSize(1);
        // Next cursor since should be the last included model's lastUpdated
        assertThat(result.getSince()).isEqualTo(update1);
    }

    @Test
    void testFetchDatabaseClock_DatabaseExceptionFallback() {
        when(entityManager.createNativeQuery(anyString())).thenThrow(new RuntimeException("DB down"));

        Instant clock = spatialExportService.fetchDatabaseClock();

        assertThat(clock).isNotNull();
        assertThat(clock).isBeforeOrEqualTo(Instant.now());
    }
}

