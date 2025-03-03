package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.SilvicultureBaseCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.SilvicultureBaseCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SilvicultureBaseCodeResourceAssemblerTest {

    private SilvicultureBaseCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new SilvicultureBaseCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        SilvicultureBaseCodeEntity entity = createTestEntity();

        // Act
        SilvicultureBaseCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getSilvicultureBaseCode(), model.getSilvicultureBaseCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        SilvicultureBaseCodeModel model = createTestModel();

        // Act
        SilvicultureBaseCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getSilvicultureBaseCode(), entity.getSilvicultureBaseCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        SilvicultureBaseCodeEntity entity1 = createTestEntity();
        SilvicultureBaseCodeEntity entity2 = createTestEntity();
        entity2.setSilvicultureBaseCode("Code2");
        List<SilvicultureBaseCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<SilvicultureBaseCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        SilvicultureBaseCodeModel model = null;

        // Act
        SilvicultureBaseCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private SilvicultureBaseCodeEntity createTestEntity() {
        SilvicultureBaseCodeEntity entity = new SilvicultureBaseCodeEntity();
        entity.setSilvicultureBaseCode("Code1");
        entity.setSilvicultureBaseGuid(UUID.fromString("aa0a073b-f729-4b3b-9128-810010662772"));
        entity.setProjectTypeCode("FUEL_MGMT");
        entity.setDescription("Test Description");
        entity.setRevisionCount(1);
        entity.setCreateUser("TestUser");
        entity.setCreateDate(new Date());
        entity.setUpdateUser("Updater");
        entity.setUpdateDate(new Date());
        return entity;
    }

    private SilvicultureBaseCodeModel createTestModel() {
        SilvicultureBaseCodeModel model = new SilvicultureBaseCodeModel();
        model.setSilvicultureBaseCode("Code1");
        model.setSilvicultureBaseGuid("aa0a073b-f729-4b3b-9128-810010662772");
        model.setProjectTypeCode("FUEL_MGMT");
        model.setDescription("Test Description");
        model.setRevisionCount(1);
        model.setCreateUser("TestUser");
        model.setCreateDate(new Date());
        model.setUpdateUser("Updater");
        model.setUpdateDate(new Date());
        return model;
    }
}
