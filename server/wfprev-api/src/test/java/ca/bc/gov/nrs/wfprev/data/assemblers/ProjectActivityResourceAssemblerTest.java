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
import java.util.Date;
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
        Date lastUpdated = new Date();

        // In view, deleted projects have activityGuid == null
        ProjectActivityEntity entity = ProjectActivityEntity.builder()
                .rowId(projectGuid)
                .projectGuid(projectGuid)
                .activityGuid(null)
                .projectNumber(12345) // even if other fields were somehow present
                .lastUpdated(lastUpdated)
                .build();

        ProjectActivityModel model = assembler.toModel(entity);

        assertThat(model).isNotNull();
        assertThat(model.getProjectGuid()).isEqualTo(projectGuid);
        assertThat(model.getLastUpdated()).isEqualTo(lastUpdated);
        assertThat(model.getActivityGuid()).isNull();
        assertThat(model.getProjectNumber()).isNull();
        assertThat(model.getGeometry()).isNull();
        assertThat(model.getActivityName()).isNull();
    }

    @Test
    void testToModel_ActiveProject() {
        UUID projectGuid = UUID.randomUUID();
        UUID activityGuid = UUID.randomUUID();
        UUID projectPlanFiscalGuid = UUID.randomUUID();
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400000L);
        Date lastUpdated = new Date();

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

        ProjectActivityModel model = assembler.toModel(entity);

        assertThat(model).isNotNull();
        assertThat(model.getProjectGuid()).isEqualTo(projectGuid);
        assertThat(model.getActivityGuid()).isEqualTo(activityGuid);
        assertThat(model.getProjectNumber()).isEqualTo(12345);
        assertThat(model.getProjectTypeCode()).isEqualTo("FUEL_MGMT");
        assertThat(model.getProjectTypeDesc()).isEqualTo("Fuel Management");
        assertThat(model.getPrimaryObjectiveTypeCode()).isEqualTo("OBJ1");
        assertThat(model.getPrimaryObjectiveTypeDesc()).isEqualTo("Primary Objective");
        assertThat(model.getSecondaryObjectiveTypeCode()).isEqualTo("OBJ2");
        assertThat(model.getSecondaryObjectiveTypeDesc()).isEqualTo("Secondary Objective");
        assertThat(model.getResultsProjectCode()).isEqualTo("RES01");
        assertThat(model.getProjectPlanFiscalGuid()).isEqualTo(projectPlanFiscalGuid);
        assertThat(model.getFiscalYear()).isEqualTo(new BigDecimal("2026"));
        assertThat(model.getProjectFiscalName()).isEqualTo("Fiscal Plan 2026");
        assertThat(model.getActivityName()).isEqualTo("Prescribed Fire");
        assertThat(model.getActivityStatusCode()).isEqualTo("COMPLETED");
        assertThat(model.getActivityStatusDesc()).isEqualTo("Completed");
        assertThat(model.getActivityStartDate()).isEqualTo(startDate);
        assertThat(model.getActivityEndDate()).isEqualTo(endDate);
        assertThat(model.getCompletedAreaHa()).isEqualTo(new BigDecimal("50.5"));
        assertThat(model.getOutstandingObligationsInd()).isFalse();
        assertThat(model.getLastUpdated()).isEqualTo(lastUpdated);
        // Verify SRID has been set to 4326
        assertThat(model.getGeometry()).isNotNull();
        assertThat(model.getGeometry().getSRID()).isEqualTo(4326);
    }

    @Test
    void testToModel_NullEntity() {
        ProjectActivityModel model = assembler.toModel((ProjectActivityEntity) null);
        assertThat(model).isNull();
    }

    @Test
    void testToEntity_DeletedProject() {
        UUID projectGuid = UUID.randomUUID();
        Date lastUpdated = new Date();

        ProjectActivityModel model = ProjectActivityModel.builder()
                .projectGuid(projectGuid)
                .activityGuid(null)
                .lastUpdated(lastUpdated)
                .build();

        ProjectActivityEntity entity = assembler.toEntity(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getRowId()).isEqualTo(projectGuid);
        assertThat(entity.getProjectGuid()).isEqualTo(projectGuid);
        assertThat(entity.getActivityGuid()).isNull();
        assertThat(entity.getLastUpdated()).isEqualTo(lastUpdated);
    }

    @Test
    void testToEntity_ActiveProject() {
        UUID projectGuid = UUID.randomUUID();
        UUID activityGuid = UUID.randomUUID();
        Date lastUpdated = new Date();

        ProjectActivityModel model = ProjectActivityModel.builder()
                .projectGuid(projectGuid)
                .activityGuid(activityGuid)
                .activityName("Prescribed Fire")
                .lastUpdated(lastUpdated)
                .build();

        ProjectActivityEntity entity = assembler.toEntity(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getRowId()).isEqualTo(activityGuid);
        assertThat(entity.getProjectGuid()).isEqualTo(projectGuid);
        assertThat(entity.getActivityGuid()).isEqualTo(activityGuid);
        assertThat(entity.getActivityName()).isEqualTo("Prescribed Fire");
        assertThat(entity.getLastUpdated()).isEqualTo(lastUpdated);
    }

    @Test
    void testToEntity_NullModel() {
        ProjectActivityEntity entity = assembler.toEntity(null);
        assertThat(entity).isNull();
    }
}

