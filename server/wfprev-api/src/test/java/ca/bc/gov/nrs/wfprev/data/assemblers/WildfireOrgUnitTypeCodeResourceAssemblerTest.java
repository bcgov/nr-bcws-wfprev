package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.WildfireOrgUnitTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.WildfireOrgUnitTypeCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class WildfireOrgUnitTypeCodeResourceAssemblerTest {

    private WildfireOrgUnitTypeCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new WildfireOrgUnitTypeCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        WildfireOrgUnitTypeCodeEntity entity = createTestEntity();
        WildfireOrgUnitTypeCodeModel model = assembler.toModel(entity);
        assertNotNull(model);
        assertEquals(entity.getWildfireOrgUnitTypeCode(), model.getWildfireOrgUnitTypeCode());
        assertEquals(entity.getDescription(), model.getDescription());
    }

    @Test
    void testToEntity_Success() {
        WildfireOrgUnitTypeCodeModel model = createTestModel();
        WildfireOrgUnitTypeCodeEntity entity = assembler.toEntity(model);
        assertNotNull(entity);
        assertEquals(model.getWildfireOrgUnitTypeCode(), entity.getWildfireOrgUnitTypeCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToEntity_NullModel() {
        WildfireOrgUnitTypeCodeEntity entity = assembler.toEntity(null);
        assertNull(entity);
    }

    private WildfireOrgUnitTypeCodeEntity createTestEntity() {
        return WildfireOrgUnitTypeCodeEntity.builder()
                .wildfireOrgUnitTypeCode("ZONE")
                .description("Zone")
                .displayOrder(1)
                .effectiveDate(new Date())
                .expiryDate(new Date())
                .revisionCount(1)
                .createUser("creator")
                .createDate(new Date())
                .updateUser("updater")
                .updateDate(new Date())
                .build();
    }

    private WildfireOrgUnitTypeCodeModel createTestModel() {
        WildfireOrgUnitTypeCodeModel model = new WildfireOrgUnitTypeCodeModel();
        model.setWildfireOrgUnitTypeCode("ZONE");
        model.setDescription("Zone");
        model.setDisplayOrder(1);
        model.setEffectiveDate(new Date());
        model.setExpiryDate(new Date());
        model.setRevisionCount(1);
        model.setCreateUser("creator");
        model.setCreateDate(new Date());
        model.setUpdateUser("updater");
        model.setUpdateDate(new Date());
        return model;
    }
}
