package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.RiskRatingCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.RiskRatingCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RiskRatingCodeResourceAssemblerTest {

    private RiskRatingCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new RiskRatingCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        RiskRatingCodeEntity entity = createTestEntity();

        // Act
        RiskRatingCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getRiskRatingCode(), model.getRiskRatingCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        RiskRatingCodeModel model = createTestModel();

        // Act
        RiskRatingCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getRiskRatingCode(), entity.getRiskRatingCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        RiskRatingCodeEntity entity1 = createTestEntity();
        RiskRatingCodeEntity entity2 = createTestEntity();
        entity2.setRiskRatingCode("Code2");
        List<RiskRatingCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<RiskRatingCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        RiskRatingCodeModel model = null;

        // Act
        RiskRatingCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private RiskRatingCodeEntity createTestEntity() {
        RiskRatingCodeEntity entity = new RiskRatingCodeEntity();
        entity.setRiskRatingCode("Code1");
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

    private RiskRatingCodeModel createTestModel() {
        RiskRatingCodeModel model = new RiskRatingCodeModel();
        model.setRiskRatingCode("Code1");
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
