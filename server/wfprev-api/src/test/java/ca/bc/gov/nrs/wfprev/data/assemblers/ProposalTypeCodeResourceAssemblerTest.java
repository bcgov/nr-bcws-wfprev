package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ProposalTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProposalTypeCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProposalTypeCodeResourceAssemblerTest {

    private ProposalTypeCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ProposalTypeCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        ProposalTypeCodeEntity entity = createTestEntity();

        // Act
        ProposalTypeCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getProposalTypeCode(), model.getProposalTypeCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        ProposalTypeCodeModel model = createTestModel();

        // Act
        ProposalTypeCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getProposalTypeCode(), entity.getProposalTypeCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        ProposalTypeCodeEntity entity1 = createTestEntity();
        ProposalTypeCodeEntity entity2 = createTestEntity();
        entity2.setProposalTypeCode("Code2");
        List<ProposalTypeCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<ProposalTypeCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        ProposalTypeCodeModel model = null;

        // Act
        ProposalTypeCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private ProposalTypeCodeEntity createTestEntity() {
        ProposalTypeCodeEntity entity = new ProposalTypeCodeEntity();
        entity.setProposalTypeCode("Code1");
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

    private ProposalTypeCodeModel createTestModel() {
        ProposalTypeCodeModel model = new ProposalTypeCodeModel();
        model.setProposalTypeCode("Code1");
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
