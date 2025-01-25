package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ActivityCategoryCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityCategoryCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivityCategoryCodeResourceAssemblerTest {

    private ActivityCategoryCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ActivityCategoryCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        ActivityCategoryCodeEntity entity = createTestEntity();

        // Act
        ActivityCategoryCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getActivityCategoryCode(), model.getActivityCategoryCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        ActivityCategoryCodeModel model = createTestModel();

        // Act
        ActivityCategoryCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getActivityCategoryCode(), entity.getActivityCategoryCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        ActivityCategoryCodeEntity entity1 = createTestEntity();
        ActivityCategoryCodeEntity entity2 = createTestEntity();
        entity2.setActivityCategoryCode("Code2");
        List<ActivityCategoryCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<ActivityCategoryCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        ActivityCategoryCodeModel model = null;

        // Act
        ActivityCategoryCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private ActivityCategoryCodeEntity createTestEntity() {
        ActivityCategoryCodeEntity entity = new ActivityCategoryCodeEntity();
        entity.setActivityCategoryCode("Code1");
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

    private ActivityCategoryCodeModel createTestModel() {
        ActivityCategoryCodeModel model = new ActivityCategoryCodeModel();
        model.setActivityCategoryCode("Code1");
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