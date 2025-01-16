package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectPlanStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectPlanStatusCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectPlanStatusCodeResourceAssemblerTest {
    private ProjectPlanStatusCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ProjectPlanStatusCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        ProjectPlanStatusCodeEntity entity = createTestEntity();

        // Act
        ProjectPlanStatusCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getProjectPlanStatusCode(), model.getProjectPlanStatusCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        ProjectPlanStatusCodeModel model = createTestModel();

        // Act
        ProjectPlanStatusCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getProjectPlanStatusCode(), entity.getProjectPlanStatusCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        ProjectPlanStatusCodeEntity entity1 = createTestEntity();
        ProjectPlanStatusCodeEntity entity2 = createTestEntity();
        entity2.setProjectPlanStatusCode("Code2");
        List<ProjectPlanStatusCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<ProjectPlanStatusCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    private ProjectPlanStatusCodeEntity createTestEntity() {
        ProjectPlanStatusCodeEntity entity = new ProjectPlanStatusCodeEntity();
        entity.setProjectPlanStatusCode("Code1");
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

    private ProjectPlanStatusCodeModel createTestModel() {
        ProjectPlanStatusCodeModel model = new ProjectPlanStatusCodeModel();
        model.setProjectPlanStatusCode("Code1");
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
