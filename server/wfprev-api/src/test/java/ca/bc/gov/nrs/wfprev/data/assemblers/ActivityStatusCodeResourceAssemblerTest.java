package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ActivityStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityStatusCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivityStatusCodeResourceAssemblerTest {

    private ActivityStatusCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ActivityStatusCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        ActivityStatusCodeEntity entity = createTestEntity();

        // Act
        ActivityStatusCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getActivityStatusCode(), model.getActivityStatusCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        ActivityStatusCodeModel model = createTestModel();

        // Act
        ActivityStatusCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getActivityStatusCode(), entity.getActivityStatusCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        ActivityStatusCodeEntity entity1 = createTestEntity();
        ActivityStatusCodeEntity entity2 = createTestEntity();
        entity2.setActivityStatusCode("Code2");
        List<ActivityStatusCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<ActivityStatusCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        ActivityStatusCodeModel model = null;

        // Act
        ActivityStatusCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private ActivityStatusCodeEntity createTestEntity() {
        ActivityStatusCodeEntity entity = new ActivityStatusCodeEntity();
        entity.setActivityStatusCode("Code1");
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

    private ActivityStatusCodeModel createTestModel() {
        ActivityStatusCodeModel model = new ActivityStatusCodeModel();
        model.setActivityStatusCode("Code1");
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
