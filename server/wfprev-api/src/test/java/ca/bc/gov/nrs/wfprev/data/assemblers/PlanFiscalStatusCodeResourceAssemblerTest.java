package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.PlanFiscalStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.PlanFiscalStatusCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlanFiscalStatusCodeResourceAssemblerTest {

    private PlanFiscalStatusCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new PlanFiscalStatusCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        PlanFiscalStatusCodeEntity entity = createTestEntity();

        // Act
        PlanFiscalStatusCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getPlanFiscalStatusCode(), model.getPlanFiscalStatusCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        PlanFiscalStatusCodeModel model = createTestModel();

        // Act
        PlanFiscalStatusCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getPlanFiscalStatusCode(), entity.getPlanFiscalStatusCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        PlanFiscalStatusCodeEntity entity1 = createTestEntity();
        PlanFiscalStatusCodeEntity entity2 = createTestEntity();
        entity2.setPlanFiscalStatusCode("PFS2");
        List<PlanFiscalStatusCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<PlanFiscalStatusCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        PlanFiscalStatusCodeModel model = null;

        // Act
        PlanFiscalStatusCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private PlanFiscalStatusCodeEntity createTestEntity() {
        PlanFiscalStatusCodeEntity entity = new PlanFiscalStatusCodeEntity();
        entity.setPlanFiscalStatusCode("PFS1");
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

    private PlanFiscalStatusCodeModel createTestModel() {
        PlanFiscalStatusCodeModel model = new PlanFiscalStatusCodeModel();
        model.setPlanFiscalStatusCode("PFS1");
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