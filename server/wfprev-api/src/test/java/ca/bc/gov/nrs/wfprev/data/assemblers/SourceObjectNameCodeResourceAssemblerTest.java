package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.SourceObjectNameCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.SourceObjectNameCodeModel;
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

class SourceObjectNameCodeResourceAssemblerTest {

    private SourceObjectNameCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new SourceObjectNameCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        SourceObjectNameCodeEntity entity = createTestEntity();

        // Act
        SourceObjectNameCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getSourceObjectNameCode(), model.getSourceObjectNameCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        SourceObjectNameCodeModel model = createTestModel();

        // Act
        SourceObjectNameCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getSourceObjectNameCode(), entity.getSourceObjectNameCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        SourceObjectNameCodeEntity entity1 = createTestEntity();
        SourceObjectNameCodeEntity entity2 = createTestEntity();
        entity2.setSourceObjectNameCode("Code2");
        List<SourceObjectNameCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<SourceObjectNameCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        SourceObjectNameCodeModel model = null;

        // Act
        SourceObjectNameCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private SourceObjectNameCodeEntity createTestEntity() {
        SourceObjectNameCodeEntity entity = new SourceObjectNameCodeEntity();
        entity.setSourceObjectNameCode("Code1");
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

    private SourceObjectNameCodeModel createTestModel() {
        SourceObjectNameCodeModel model = new SourceObjectNameCodeModel();
        model.setSourceObjectNameCode("Code1");
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
