package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.FundingSourceCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.FundingSourceCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FundingSourceCodeResourceAssemblerTest {

    private FundingSourceCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new FundingSourceCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        FundingSourceCodeEntity entity = createTestEntity();

        // Act
        FundingSourceCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getFundingSourceGuid().toString(), model.getFundingSourceGuid());
        assertEquals(entity.getFundingSourceAbbreviation(), model.getFundingSourceAbbreviation());
        assertEquals(entity.getFundingSourceName(), model.getFundingSourceName());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        FundingSourceCodeModel model = createTestModel();

        // Act
        FundingSourceCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getFundingSourceGuid(), entity.getFundingSourceGuid().toString());
        assertEquals(model.getFundingSourceAbbreviation(), entity.getFundingSourceAbbreviation());
        assertEquals(model.getFundingSourceName(), entity.getFundingSourceName());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        FundingSourceCodeEntity entity1 = createTestEntity();
        FundingSourceCodeEntity entity2 = createTestEntity();
        entity2.setFundingSourceGuid(UUID.randomUUID());
        List<FundingSourceCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<FundingSourceCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        FundingSourceCodeModel model = null;

        // Act
        FundingSourceCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private FundingSourceCodeEntity createTestEntity() {
        FundingSourceCodeEntity entity = new FundingSourceCodeEntity();
        entity.setFundingSourceGuid(UUID.randomUUID());
        entity.setFundingSourceAbbreviation("FS123");
        entity.setFundingSourceName("Funding Source Name");
        entity.setRevisionCount(1);
        entity.setCreateUser("TestUser");
        entity.setCreateDate(new Date());
        entity.setUpdateUser("Updater");
        entity.setUpdateDate(new Date());
        return entity;
    }

    private FundingSourceCodeModel createTestModel() {
        FundingSourceCodeModel model = new FundingSourceCodeModel();
        model.setFundingSourceGuid(UUID.randomUUID().toString());
        model.setFundingSourceAbbreviation("FS123");
        model.setFundingSourceName("Funding Source Name");
        model.setRevisionCount(1);
        model.setCreateUser("TestUser");
        model.setCreateDate(new Date());
        model.setUpdateUser("Updater");
        model.setUpdateDate(new Date());
        return model;
    }
}
