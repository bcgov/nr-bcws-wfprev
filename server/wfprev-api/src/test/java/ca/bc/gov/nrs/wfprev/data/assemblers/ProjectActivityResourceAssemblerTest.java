package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectActivityEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectActivityModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectActivityResourceAssemblerTest {

    private ProjectActivityResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ProjectActivityResourceAssembler();
    }

    @Test
    void testToModel_DeletedProject() {
        UUID projectGuid = UUID.randomUUID();
        Instant lastUpdated = Instant.now();

        // In view, deleted projects have activityGuid == null
        ProjectActivityEntity entity = ProjectActivityEntity.builder()
                .rowId(projectGuid)
                .projectGuid(projectGuid)
                .activityGuid(null)
                .projectNumber(12345) // even if other fields were somehow present
                .lastUpdated(lastUpdated)
                .build();

        List<ProjectActivityModel> models = assembler.toModels(List.of(entity));

        assertThat(models).hasSize(1);
        ProjectActivityModel model = models.get(0);
        assertThat(model.getProjectGuid()).isEqualTo(projectGuid);
        assertThat(model.getLastUpdated()).isEqualTo(lastUpdated);
        assertThat(model.getActivities()).isNotNull().isEmpty();
        assertThat(model.getProjectNumber()).isEqualTo(12345);
    }

    @Test
    void testToModel_ActiveProject() {
        UUID projectGuid = UUID.randomUUID();
        UUID activityGuid = UUID.randomUUID();
        UUID projectPlanFiscalGuid = UUID.randomUUID();
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400000L);
        Instant lastUpdated = Instant.now();

        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coords = new Coordinate[] {
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0),
                new Coordinate(0, 0)
        };
        LinearRing shell = gf.createLinearRing(coords);
        Polygon polygon = gf.createPolygon(shell);
        MultiPolygon multiPolygon = gf.createMultiPolygon(new Polygon[] { polygon });
        // SRID starts as 0
        multiPolygon.setSRID(0);

        ProjectActivityEntity entity = ProjectActivityEntity.builder()
                .rowId(activityGuid)
                .projectGuid(projectGuid)
                .projectNumber(12345)
                .projectTypeCode("FUEL_MGMT")
                .projectTypeDesc("Fuel Management")
                .primaryObjectiveTypeCode("OBJ1")
                .primaryObjectiveTypeDesc("Primary Objective")
                .secondaryObjectiveTypeCode("OBJ2")
                .secondaryObjectiveTypeDesc("Secondary Objective")
                .resultsProjectCode("RES01")
                .projectPlanFiscalGuid(projectPlanFiscalGuid)
                .fiscalYear(new BigDecimal("2026"))
                .projectFiscalName("Fiscal Plan 2026")
                .activityGuid(activityGuid)
                .activityName("Prescribed Fire")
                .activityStatusCode("COMPLETED")
                .activityStatusDesc("Completed")
                .activityStartDate(startDate)
                .activityEndDate(endDate)
                .completedAreaHa(new BigDecimal("50.5"))
                .outstandingObligationsInd(false)
                .geometry(multiPolygon)
                .lastUpdated(lastUpdated)
                .build();

        List<ProjectActivityModel> models = assembler.toModels(List.of(entity));

        assertThat(models).hasSize(1);
        ProjectActivityModel model = models.get(0);
        assertThat(model.getProjectGuid()).isEqualTo(projectGuid);
        assertThat(model.getProjectNumber()).isEqualTo(12345);
        assertThat(model.getProjectTypeCode()).isEqualTo("FUEL_MGMT");
        assertThat(model.getProjectTypeDesc()).isEqualTo("Fuel Management");
        assertThat(model.getPrimaryObjectiveTypeCode()).isEqualTo("OBJ1");
        assertThat(model.getPrimaryObjectiveTypeDesc()).isEqualTo("Primary Objective");
        assertThat(model.getSecondaryObjectiveTypeCode()).isEqualTo("OBJ2");
        assertThat(model.getSecondaryObjectiveTypeDesc()).isEqualTo("Secondary Objective");
        assertThat(model.getResultsProjectCode()).isEqualTo("RES01");
        assertThat(model.getLastUpdated()).isEqualTo(lastUpdated);

        assertThat(model.getActivities()).isNotNull().hasSize(1);
        ProjectActivityModel.Activity activity = model.getActivities().get(0);
        assertThat(activity.getActivityGuid()).isEqualTo(activityGuid);
        assertThat(activity.getProjectPlanFiscalGuid()).isEqualTo(projectPlanFiscalGuid);
        assertThat(activity.getFiscalYear()).isEqualTo(new BigDecimal("2026"));
        assertThat(activity.getProjectFiscalName()).isEqualTo("Fiscal Plan 2026");
        assertThat(activity.getActivityName()).isEqualTo("Prescribed Fire");
        assertThat(activity.getActivityStatusCode()).isEqualTo("COMPLETED");
        assertThat(activity.getActivityStatusDesc()).isEqualTo("Completed");
        assertThat(activity.getActivityStartDate()).isEqualTo(startDate);
        assertThat(activity.getActivityEndDate()).isEqualTo(endDate);
        assertThat(activity.getCompletedAreaHa()).isEqualTo(new BigDecimal("50.5"));
        assertThat(activity.getOutstandingObligationsInd()).isFalse();
        // Verify SRID has been set to 4326
        assertThat(activity.getGeometry()).isNotNull();
        assertThat(activity.getGeometry().getSRID()).isEqualTo(4326);
    }

    @Test
    void testToModels_NullList() {
        List<ProjectActivityModel> models = assembler.toModels(null);
        assertThat(models).isNotNull().isEmpty();
    }

    @Test
    void testToModels_AggregatesActivitiesForSameProject() {
        UUID projectGuid = UUID.randomUUID();
        UUID activityGuid1 = UUID.randomUUID();
        UUID activityGuid2 = UUID.randomUUID();
        Instant lastUpdated = Instant.now();

        ProjectActivityEntity entity1 = ProjectActivityEntity.builder()
                .projectGuid(projectGuid)
                .projectNumber(100)
                .activityGuid(activityGuid1)
                .activityName("Activity 1")
                .lastUpdated(lastUpdated)
                .build();

        ProjectActivityEntity entity2 = ProjectActivityEntity.builder()
                .projectGuid(projectGuid)
                .projectNumber(100)
                .activityGuid(activityGuid2)
                .activityName("Activity 2")
                .lastUpdated(lastUpdated)
                .build();

        java.util.List<ProjectActivityModel> models = assembler.toModels(java.util.List.of(entity1, entity2));

        assertThat(models).hasSize(1);
        ProjectActivityModel model = models.get(0);
        assertThat(model.getProjectGuid()).isEqualTo(projectGuid);
        assertThat(model.getActivities()).hasSize(2);
        assertThat(model.getActivities().get(0).getActivityGuid()).isEqualTo(activityGuid1);
        assertThat(model.getActivities().get(0).getActivityName()).isEqualTo("Activity 1");
        assertThat(model.getActivities().get(1).getActivityGuid()).isEqualTo(activityGuid2);
        assertThat(model.getActivities().get(1).getActivityName()).isEqualTo("Activity 2");
    }
}

