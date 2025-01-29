package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.AncillaryFundingSourceCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.AncillaryFundingSourceCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AncillaryFundingSourceCodeResourceAssemblerTest {

    private AncillaryFundingSourceCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new AncillaryFundingSourceCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        AncillaryFundingSourceCodeEntity entity = createTestEntity();

        // Act
        AncillaryFundingSourceCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getAncillaryFundingSourceGuid().toString(), model.getAncillaryFundingSourceGuid());
        assertEquals(entity.getFundingSourceAbbreviation(), model.getFundingSourceAbbreviation());
        assertEquals(entity.getFundingSourceName(), model.getFundingSourceName());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        AncillaryFundingSourceCodeModel model = createTestModel();

        // Act
        AncillaryFundingSourceCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getAncillaryFundingSourceGuid(), entity.getAncillaryFundingSourceGuid().toString());
        assertEquals(model.getFundingSourceAbbreviation(), entity.getFundingSourceAbbreviation());
        assertEquals(model.getFundingSourceName(), entity.getFundingSourceName());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        AncillaryFundingSourceCodeEntity entity1 = createTestEntity();
        AncillaryFundingSourceCodeEntity entity2 = createTestEntity();
        entity2.setAncillaryFundingSourceGuid(UUID.randomUUID());
        List<AncillaryFundingSourceCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<AncillaryFundingSourceCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        AncillaryFundingSourceCodeModel model = null;

        // Act
        AncillaryFundingSourceCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private AncillaryFundingSourceCodeEntity createTestEntity() {
        AncillaryFundingSourceCodeEntity entity = new AncillaryFundingSourceCodeEntity();
        entity.setAncillaryFundingSourceGuid(UUID.randomUUID());
        entity.setFundingSourceAbbreviation("FS123");
        entity.setFundingSourceName("Funding Source Name");
        entity.setRevisionCount(1);
        entity.setCreateUser("TestUser");
        entity.setCreateDate(new Date());
        entity.setUpdateUser("Updater");
        entity.setUpdateDate(new Date());
        return entity;
    }

    private AncillaryFundingSourceCodeModel createTestModel() {
        AncillaryFundingSourceCodeModel model = new AncillaryFundingSourceCodeModel();
        model.setAncillaryFundingSourceGuid(UUID.randomUUID().toString());
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
