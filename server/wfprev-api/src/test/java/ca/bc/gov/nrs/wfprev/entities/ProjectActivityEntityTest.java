package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectActivityEntity;
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

class ProjectActivityEntityTest {

    @Test
    void testAllFields() {
        UUID projectGuid = UUID.randomUUID();
        UUID projectPlanFiscalGuid = UUID.randomUUID();
        UUID activityGuid = UUID.randomUUID();
        BigDecimal fiscalYear = new BigDecimal("2026");
        BigDecimal completedAreaHa = new BigDecimal("45.67");
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400000L);

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

        ProjectActivityEntity entity = ProjectActivityEntity.builder()
                .projectGuid(projectGuid)
                .projectNumber(12345)
                .projectTypeCode("FUEL_MGMT")
                .projectTypeDesc("Fuel Management")
                .primaryObjectiveTypeCode("OBJ1")
                .primaryObjectiveTypeDesc("Primary Objective Description")
                .secondaryObjectiveTypeCode("OBJ2")
                .secondaryObjectiveTypeDesc("Secondary Objective Description")
                .resultsProjectCode("RES_001")
                .projectPlanFiscalGuid(projectPlanFiscalGuid)
                .fiscalYear(fiscalYear)
                .projectFiscalName("Fiscal Plan 2026")
                .activityGuid(activityGuid)
                .activityName("Prescribed Burn Activity")
                .activityStatusCode("COMPLETED")
                .activityStatusDesc("Completed")
                .activityStartDate(startDate)
                .activityEndDate(endDate)
                .completedAreaHa(completedAreaHa)
                .outstandingObligationsInd(false)
                .geometry(multiPolygon)
                .build();

        assertThat(entity.getProjectGuid()).isEqualTo(projectGuid);
        assertThat(entity.getProjectNumber()).isEqualTo(12345);
        assertThat(entity.getProjectTypeCode()).isEqualTo("FUEL_MGMT");
        assertThat(entity.getProjectTypeDesc()).isEqualTo("Fuel Management");
        assertThat(entity.getPrimaryObjectiveTypeCode()).isEqualTo("OBJ1");
        assertThat(entity.getPrimaryObjectiveTypeDesc()).isEqualTo("Primary Objective Description");
        assertThat(entity.getSecondaryObjectiveTypeCode()).isEqualTo("OBJ2");
        assertThat(entity.getSecondaryObjectiveTypeDesc()).isEqualTo("Secondary Objective Description");
        assertThat(entity.getResultsProjectCode()).isEqualTo("RES_001");
        assertThat(entity.getProjectPlanFiscalGuid()).isEqualTo(projectPlanFiscalGuid);
        assertThat(entity.getFiscalYear()).isEqualTo(fiscalYear);
        assertThat(entity.getProjectFiscalName()).isEqualTo("Fiscal Plan 2026");
        assertThat(entity.getActivityGuid()).isEqualTo(activityGuid);
        assertThat(entity.getActivityName()).isEqualTo("Prescribed Burn Activity");
        assertThat(entity.getActivityStatusCode()).isEqualTo("COMPLETED");
        assertThat(entity.getActivityStatusDesc()).isEqualTo("Completed");
        assertThat(entity.getActivityStartDate()).isEqualTo(startDate);
        assertThat(entity.getActivityEndDate()).isEqualTo(endDate);
        assertThat(entity.getCompletedAreaHa()).isEqualTo(completedAreaHa);
        assertThat(entity.getOutstandingObligationsInd()).isFalse();
        assertThat(entity.getGeometry()).isEqualTo(multiPolygon);
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        ProjectActivityEntity entity = new ProjectActivityEntity();
        UUID activityGuid = UUID.randomUUID();
        entity.setActivityGuid(activityGuid);
        entity.setActivityName("Test Activity");

        assertThat(entity.getActivityGuid()).isEqualTo(activityGuid);
        assertThat(entity.getActivityName()).isEqualTo("Test Activity");
    }

    @Test
    void testEqualsAndHashCode() {
        UUID activityGuid = UUID.randomUUID();

        ProjectActivityEntity entity1 = ProjectActivityEntity.builder()
                .activityGuid(activityGuid)
                .activityName("Activity A")
                .build();

        ProjectActivityEntity entity2 = ProjectActivityEntity.builder()
                .activityGuid(activityGuid)
                .activityName("Activity A")
                .build();

        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }
}
