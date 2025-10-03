package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectLocationEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectLocationModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectLocationResourceAssemblerTest {

    private ProjectLocationResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ProjectLocationResourceAssembler();
    }

    @Test
    void toEntity_withGuidAndCoords_mapsAllFields() {
        String guid = UUID.randomUUID().toString();
        ProjectLocationModel model = new ProjectLocationModel();
        model.setProjectGuid(guid);
        model.setLatitude(BigDecimal.valueOf(49.2827));
        model.setLongitude(BigDecimal.valueOf(-123.1207));

        ProjectLocationEntity entity = assembler.toEntity(model);

        assertNotNull(entity);
        assertEquals(UUID.fromString(guid), entity.getProjectGuid());
        assertEquals(BigDecimal.valueOf(49.2827), entity.getLatitude());
        assertEquals(BigDecimal.valueOf(-123.1207), entity.getLongitude());
    }

    @Test
    void toEntity_nullGuid_leavesGuidNull() {
        ProjectLocationModel model = new ProjectLocationModel();
        model.setProjectGuid(null);
        model.setLatitude(BigDecimal.valueOf(50.0));
        model.setLongitude(BigDecimal.valueOf(-120.0));

        ProjectLocationEntity entity = assembler.toEntity(model);

        assertNotNull(entity);
        assertNull(entity.getProjectGuid());
        assertEquals(BigDecimal.valueOf(50.0), entity.getLatitude());
        assertEquals(BigDecimal.valueOf(-120.0), entity.getLongitude());
    }

    @Test
    void toEntity_invalidGuid_throwsIllegalArgumentException() {
        ProjectLocationModel model = new ProjectLocationModel();
        model.setProjectGuid("not-a-uuid");
        model.setLatitude(BigDecimal.valueOf(1.0));
        model.setLongitude(BigDecimal.valueOf(2.0));

        assertThrows(IllegalArgumentException.class, () -> assembler.toEntity(model));
    }

    @Test
    void toModel_withGuidAndCoords_mapsAllFields() {
        UUID guid = UUID.randomUUID();
        ProjectLocationEntity entity = new ProjectLocationEntity();
        entity.setProjectGuid(guid);
        entity.setLatitude(BigDecimal.valueOf(48.4284));
        entity.setLongitude(BigDecimal.valueOf(-123.3656));

        ProjectLocationModel model = assembler.toModel(entity);

        assertNotNull(model);
        assertEquals(guid.toString(), model.getProjectGuid());
        assertEquals(BigDecimal.valueOf(48.4284), model.getLatitude());
        assertEquals(BigDecimal.valueOf(-123.3656), model.getLongitude());
    }

    @Test
    void toModel_nullGuid_leavesGuidNull() {
        ProjectLocationEntity entity = new ProjectLocationEntity();
        entity.setProjectGuid(null);
        entity.setLatitude(BigDecimal.valueOf(55.0));
        entity.setLongitude(BigDecimal.valueOf(-128.0));

        ProjectLocationModel model = assembler.toModel(entity);

        assertNotNull(model);
        assertNull(model.getProjectGuid());
        assertEquals(BigDecimal.valueOf(55.0), model.getLatitude());
        assertEquals(BigDecimal.valueOf(-128.00), model.getLongitude());
    }
}
