package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ObjectiveTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ObjectiveTypeCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObjectiveTypeCodeResourceAssemblerTest {

    private ObjectiveTypeCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ObjectiveTypeCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        ObjectiveTypeCodeEntity entity = createTestEntity();

        // Act
        ObjectiveTypeCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getObjectiveTypeCode(), model.getObjectiveTypeCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        ObjectiveTypeCodeModel model = createTestModel();

        // Act
        ObjectiveTypeCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getObjectiveTypeCode(), entity.getObjectiveTypeCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        ObjectiveTypeCodeEntity entity1 = createTestEntity();
        ObjectiveTypeCodeEntity entity2 = createTestEntity();
        entity2.setObjectiveTypeCode("Code2");
        List<ObjectiveTypeCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<ObjectiveTypeCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        ObjectiveTypeCodeModel model = null;

        // Act
        ObjectiveTypeCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private ObjectiveTypeCodeEntity createTestEntity() {
        ObjectiveTypeCodeEntity entity = new ObjectiveTypeCodeEntity();
        entity.setObjectiveTypeCode("Code1");
        entity.setDescription("Test Description");
        entity.setDisplayOrder(1);
        entity.setEffectiveDate(new Date());
        entity.setExpiryDate(new Date());
        entity.setRevisionCount(1);
        entity.setCreateUser("TestUser");
        entity.setCreateDate(new Date());
        entity.setUpdateUser("Updater");
        entity.setUpdateDate(new Date());
        return entity;
    }

    private ObjectiveTypeCodeModel createTestModel() {
        ObjectiveTypeCodeModel model = new ObjectiveTypeCodeModel();
        model.setObjectiveTypeCode("Code1");
        model.setDescription("Test Description");
        model.setDisplayOrder(1);
        model.setEffectiveDate(new Date());
        model.setExpiryDate(new Date());
        model.setRevisionCount(1);
        model.setCreateUser("TestUser");
        model.setCreateDate(new Date());
        model.setUpdateUser("Updater");
        model.setUpdateDate(new Date());
        return model;
    }
}
