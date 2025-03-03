package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.SilvicultureMethodCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.SilvicultureMethodCodeModel;
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

class SilvicultureMethodCodeResourceAssemblerTest {

    private SilvicultureMethodCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new SilvicultureMethodCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        SilvicultureMethodCodeEntity entity = createTestEntity();

        // Act
        SilvicultureMethodCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getSilvicultureMethodCode(), model.getSilvicultureMethodCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        SilvicultureMethodCodeModel model = createTestModel();

        // Act
        SilvicultureMethodCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getSilvicultureMethodCode(), entity.getSilvicultureMethodCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        SilvicultureMethodCodeEntity entity1 = createTestEntity();
        SilvicultureMethodCodeEntity entity2 = createTestEntity();
        entity2.setSilvicultureMethodCode("Code2");
        List<SilvicultureMethodCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<SilvicultureMethodCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        SilvicultureMethodCodeModel model = null;

        // Act
        SilvicultureMethodCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private SilvicultureMethodCodeEntity createTestEntity() {
        SilvicultureMethodCodeEntity entity = new SilvicultureMethodCodeEntity();
        entity.setSilvicultureMethodCode("Code1");
        entity.setSilvicultureMethodGuid(UUID.fromString("aa0a073b-f729-4b3b-9128-810010662772"));
        entity.setSilvicultureTechniqueGuid(UUID.fromString("970a3b25-03ba-42fd-b68e-c9272a707d0a"));
        entity.setDescription("Test Description");
        entity.setRevisionCount(1);
        entity.setCreateUser("TestUser");
        entity.setCreateDate(new Date());
        entity.setUpdateUser("Updater");
        entity.setUpdateDate(new Date());
        return entity;
    }

    private SilvicultureMethodCodeModel createTestModel() {
        SilvicultureMethodCodeModel model = new SilvicultureMethodCodeModel();
        model.setSilvicultureMethodCode("Code1");
        model.setSilvicultureMethodGuid("aa0a073b-f729-4b3b-9128-810010662772");
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
