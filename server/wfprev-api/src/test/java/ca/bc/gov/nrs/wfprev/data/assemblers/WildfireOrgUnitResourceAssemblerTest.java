package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.WildfireOrgUnitEntity;
import ca.bc.gov.nrs.wfprev.data.entities.WildfireOrgUnitTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.WildfireOrgUnitModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WildfireOrgUnitResourceAssemblerTest {

    private WildfireOrgUnitResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new WildfireOrgUnitResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        WildfireOrgUnitEntity entity = createTestEntity();
        WildfireOrgUnitModel model = assembler.toModel(entity);
        assertNotNull(model);
        assertEquals(entity.getOrgUnitIdentifier(), model.getOrgUnitIdentifier());
        assertEquals(entity.getOrgUnitName(), model.getOrgUnitName());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        WildfireOrgUnitModel model = createTestModel();
        WildfireOrgUnitEntity entity = assembler.toEntity(model);
        assertNotNull(entity);
        assertEquals(model.getOrgUnitIdentifier(), entity.getOrgUnitIdentifier());
        assertEquals(model.getOrgUnitName(), entity.getOrgUnitName());
    }

    @Test
    void testToEntity_NullModel() {
        WildfireOrgUnitModel model = null;
        WildfireOrgUnitEntity entity = assembler.toEntity(model);
        assertNull(entity);
    }

    @Test
    void testToCollectionModel_Success() {
        WildfireOrgUnitEntity e1 = createTestEntity();
        WildfireOrgUnitEntity e2 = createTestEntity();
        e2.setOrgUnitIdentifier(999);
        e2.setOrgUnitName("Other Centre");
        List<WildfireOrgUnitEntity> entities = Arrays.asList(e1, e2);
        CollectionModel<WildfireOrgUnitModel> collectionModel = assembler.toCollectionModel(entities);
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    private WildfireOrgUnitEntity createTestEntity() {
        WildfireOrgUnitTypeCodeEntity typeCode = WildfireOrgUnitTypeCodeEntity.builder()
                .wildfireOrgUnitTypeCode("ZONE")
                .description("Fire Zone")
                .displayOrder(1)
                .effectiveDate(new Date())
                .expiryDate(new Date())
                .revisionCount(1)
                .createUser("creator")
                .createDate(new Date())
                .updateUser("updater")
                .updateDate(new Date())
                .build();

        return WildfireOrgUnitEntity.builder()
                .orgUnitIdentifier(50)
                .orgUnitName("Kamloops Fire Centre")
                .parentOrgUnitIdentifier(null)
                .characterAlias("KFC")
                .integerAlias(500)
                .effectiveDate(new Date())
                .expiryDate(new Date())
                .revisionCount(1)
                .createUser("creator")
                .createDate(new Date())
                .updateUser("updater")
                .updateDate(new Date())
                .wildfireOrgUnitTypeCode(typeCode)
                .build();
    }

    private WildfireOrgUnitModel createTestModel() {
        WildfireOrgUnitTypeCodeEntity typeCode = WildfireOrgUnitTypeCodeEntity.builder()
                .wildfireOrgUnitTypeCode("ZONE")
                .description("Fire Zone")
                .displayOrder(1)
                .effectiveDate(new Date())
                .expiryDate(new Date())
                .revisionCount(1)
                .createUser("creator")
                .createDate(new Date())
                .updateUser("updater")
                .updateDate(new Date())
                .build();

        WildfireOrgUnitTypeCodeResourceAssembler typeAssembler = new WildfireOrgUnitTypeCodeResourceAssembler();

        WildfireOrgUnitModel model = new WildfireOrgUnitModel();
        model.setOrgUnitIdentifier(50);
        model.setOrgUnitName("Kamloops Fire Centre");
        model.setParentOrgUnitIdentifier(null);
        model.setCharacterAlias("KFC");
        model.setIntegerAlias(500);
        model.setEffectiveDate(new Date());
        model.setExpiryDate(new Date());
        model.setRevisionCount(1);
        model.setCreateUser("creator");
        model.setCreateDate(new Date());
        model.setUpdateUser("updater");
        model.setUpdateDate(new Date());
        model.setWildfireOrgUnitTypeCode(typeAssembler.toModel(typeCode));

        return model;
    }
}
