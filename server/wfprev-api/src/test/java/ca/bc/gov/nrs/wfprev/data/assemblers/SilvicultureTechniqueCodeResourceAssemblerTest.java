package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.SilvicultureTechniqueCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.SilvicultureTechniqueCodeModel;
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

class SilvicultureTechniqueCodeResourceAssemblerTest {

    private SilvicultureTechniqueCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new SilvicultureTechniqueCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        SilvicultureTechniqueCodeEntity entity = createTestEntity();

        // Act
        SilvicultureTechniqueCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getSilvicultureTechniqueCode(), model.getSilvicultureTechniqueCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        SilvicultureTechniqueCodeModel model = createTestModel();

        // Act
        SilvicultureTechniqueCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getSilvicultureTechniqueCode(), entity.getSilvicultureTechniqueCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        SilvicultureTechniqueCodeEntity entity1 = createTestEntity();
        SilvicultureTechniqueCodeEntity entity2 = createTestEntity();
        entity2.setSilvicultureTechniqueCode("Code2");
        List<SilvicultureTechniqueCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<SilvicultureTechniqueCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        SilvicultureTechniqueCodeModel model = null;

        // Act
        SilvicultureTechniqueCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private SilvicultureTechniqueCodeEntity createTestEntity() {
        SilvicultureTechniqueCodeEntity entity = new SilvicultureTechniqueCodeEntity();
        entity.setSilvicultureTechniqueCode("Code1");
        entity.setSilvicultureBaseGuid(UUID.fromString("aa0a073b-f729-4b3b-9128-810010662772"));
        entity.setSilvicultureTechniqueGuid(UUID.fromString("970a3b25-03ba-42fd-b68e-c9272a707d0a"));
        entity.setDescription("Test Description");
        entity.setRevisionCount(1);
        entity.setCreateUser("TestUser");
        entity.setCreateDate(new Date());
        entity.setUpdateUser("Updater");
        entity.setUpdateDate(new Date());
        return entity;
    }

    private SilvicultureTechniqueCodeModel createTestModel() {
        SilvicultureTechniqueCodeModel model = new SilvicultureTechniqueCodeModel();
        model.setSilvicultureTechniqueCode("Code1");
        model.setSilvicultureBaseGuid("aa0a073b-f729-4b3b-9128-810010662772");
        model.setSilvicultureTechniqueGuid("970a3b25-03ba-42fd-b68e-c9272a707d0a");
        model.setDescription("Test Description");
        model.setRevisionCount(1);
        model.setCreateUser("TestUser");
        model.setCreateDate(new Date());
        model.setUpdateUser("Updater");
        model.setUpdateDate(new Date());
        return model;
    }
}
