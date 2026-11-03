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

        Instant since = Instant.parse("2026-10-30T00:00:00Z");
        UUID deletedProjectGuid = UUID.randomUUID();
        Instant deletedLastUpdated = Instant.parse("2026-10-30T10:00:00.123456Z");

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

        when(projectActivityRepository.findByLastUpdatedAndProjectGuidAfter(since, null))
                .thenReturn(List.of(deletedEntity));

        SpatialExportModel result = spatialExportService.getSpatialExport(since, null);

        assertThat(result).isNotNull();
        assertThat(result.getProjectActivities()).hasSize(1);

        ProjectActivityModel model = result.getProjectActivities().get(0);
        assertThat(model.getProjectGuid()).isEqualTo(deletedProjectGuid);
        assertThat(model.getLastUpdated()).isEqualTo(deletedLastUpdated);
        assertThat(model.getActivities()).isNotNull().isEmpty();
        assertThat(model.getProjectNumber()).isNull();

        assertThat(result.getSince()).isEqualTo(fixedDbClock);
    }

    @Test
    void testExport_EmptyResult() {
        mockDatabaseClock(fixedDbClock);

        Instant since = Instant.parse("2026-10-30T00:00:00Z");

        when(projectActivityRepository.findByLastUpdatedAndProjectGuidAfter(since, null))
                .thenReturn(Collections.emptyList());

        SpatialExportModel result = spatialExportService.getSpatialExport(since, null);

        assertThat(result).isNotNull();
        assertThat(result.getProjectActivities()).isNotNull().isEmpty();
        assertThat(result.getSince()).isEqualTo(fixedDbClock);
    }

    @Test
    void testExport_InvalidSince_NullThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> spatialExportService.getSpatialExport(null, null)
        );

        assertThat(exception.getMessage()).isEqualTo("'since' parameter is required");
    }

    @Test
    void testExport_ActiveAndCompletedProjects() {
        mockDatabaseClock(fixedDbClock);

        Instant since = Instant.parse("2026-10-30T00:00:00Z");
        UUID projectGuid = UUID.randomUUID();
        UUID activityGuid = UUID.randomUUID();
        Instant lastUpdated = Instant.parse("2026-10-30T11:00:00.654321Z");

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

        when(projectActivityRepository.findByLastUpdatedAndProjectGuidAfter(since, null))
                .thenReturn(List.of(activeEntity));

        SpatialExportModel result = spatialExportService.getSpatialExport(since, null);

        assertThat(result).isNotNull();
        assertThat(result.getProjectActivities()).hasSize(1);

        ProjectActivityModel model = result.getProjectActivities().get(0);
        assertThat(model.getProjectGuid()).isEqualTo(projectGuid);
        assertThat(model.getProjectNumber()).isEqualTo(12345);
        assertThat(model.getProjectTypeCode()).isEqualTo("FUEL_MGMT");
        assertThat(model.getLastUpdated()).isEqualTo(lastUpdated);

        assertThat(model.getActivities()).isNotNull().hasSize(1);
        ProjectActivityModel.Activity activity = model.getActivities().get(0);
        assertThat(activity.getActivityGuid()).isEqualTo(activityGuid);
        assertThat(activity.getActivityName()).isEqualTo("Prescribed Fire");
        assertThat(activity.getGeometry()).isNotNull();
        assertThat(activity.getGeometry().getSRID()).isEqualTo(4326);

        assertThat(result.getSince()).isEqualTo(fixedDbClock);
    }

    @Test
    void testExport_AggregatesMultipleActivitiesForSameProject() {
        mockDatabaseClock(fixedDbClock);

        Instant since = Instant.parse("2026-10-30T00:00:00Z");
        UUID projectGuid = UUID.randomUUID();
        UUID activityGuid1 = UUID.randomUUID();
        UUID activityGuid2 = UUID.randomUUID();
        Instant lastUpdated = Instant.parse("2026-10-30T11:00:00.123456Z");

        ProjectActivityEntity entity1 = ProjectActivityEntity.builder()
                .rowId(activityGuid1)
                .projectGuid(projectGuid)
                .projectNumber(12345)
                .projectTypeCode("FUEL_MGMT")
                .activityGuid(activityGuid1)
                .activityName("Activity 1")
                .lastUpdated(lastUpdated)
                .build();

        ProjectActivityEntity entity2 = ProjectActivityEntity.builder()
                .rowId(activityGuid2)
                .projectGuid(projectGuid)
                .projectNumber(12345)
                .projectTypeCode("FUEL_MGMT")
                .activityGuid(activityGuid2)
                .activityName("Activity 2")
                .lastUpdated(lastUpdated)
                .build();

        when(projectActivityRepository.findByLastUpdatedAndProjectGuidAfter(since, null))
                .thenReturn(List.of(entity1, entity2));

        SpatialExportModel result = spatialExportService.getSpatialExport(since, null);

        assertThat(result).isNotNull();
        // Aggregated into 1 project with 2 activities
        assertThat(result.getProjectActivities()).hasSize(1);
        ProjectActivityModel model = result.getProjectActivities().get(0);
        assertThat(model.getProjectGuid()).isEqualTo(projectGuid);
        assertThat(model.getActivities()).hasSize(2);
        assertThat(model.getActivities().get(0).getActivityGuid()).isEqualTo(activityGuid1);
        assertThat(model.getActivities().get(1).getActivityGuid()).isEqualTo(activityGuid2);
    }

    @Test
    void testExport_WithAfterProjectGuid() {
        mockDatabaseClock(fixedDbClock);

        Instant since = Instant.parse("2026-10-30T00:00:00Z");
        UUID afterProjectGuid = UUID.randomUUID();

        when(projectActivityRepository.findByLastUpdatedAndProjectGuidAfter(since, afterProjectGuid))
                .thenReturn(Collections.emptyList());

        SpatialExportModel result = spatialExportService.getSpatialExport(since, afterProjectGuid);

        assertThat(result).isNotNull();
        assertThat(result.getProjectActivities()).isEmpty();
    }

    @Test
    void testExport_TruncationWhenExceedingMaxBytes() {
        mockDatabaseClock(fixedDbClock);

        Instant since = Instant.parse("2026-10-30T00:00:00Z");
        Instant update1 = Instant.parse("2026-10-30T08:00:00.111111Z");
        Instant update2 = Instant.parse("2026-10-30T09:00:00.222222Z");

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

        when(projectActivityRepository.findByLastUpdatedAndProjectGuidAfter(since, null))
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

        SpatialExportModel result = serviceWithMockMapper.getSpatialExport(since, null);

        // Only the first entity should be included
        assertThat(result.getProjectActivities()).hasSize(1);
        // Next cursor since should be the last included model's lastUpdated
        assertThat(result.getSince()).isEqualTo(update1);
        assertThat(result.getAfterProjectGuid()).isEqualTo(entity1.getProjectGuid());
    }

    @Test
    void testFetchDatabaseClock_DatabaseExceptionThrows() {
        when(entityManager.createNativeQuery(anyString())).thenThrow(new RuntimeException("DB down"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> spatialExportService.fetchDatabaseClock());
        assertThat(ex.getMessage()).contains("Could not fetch database clock");
    }
}

