package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ContractPhaseCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ContractPhaseCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContractPhaseCodeResourceAssemblerTest {

    private ContractPhaseCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ContractPhaseCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        ContractPhaseCodeEntity entity = createTestEntity();

        // Act
        ContractPhaseCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getContractPhaseCode(), model.getContractPhaseCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        ContractPhaseCodeModel model = createTestModel();

        // Act
        ContractPhaseCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getContractPhaseCode(), entity.getContractPhaseCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        ContractPhaseCodeEntity entity1 = createTestEntity();
        ContractPhaseCodeEntity entity2 = createTestEntity();
        entity2.setContractPhaseCode("Code2");
        List<ContractPhaseCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<ContractPhaseCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        ContractPhaseCodeModel model = null;

        // Act
        ContractPhaseCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private ContractPhaseCodeEntity createTestEntity() {
        ContractPhaseCodeEntity entity = new ContractPhaseCodeEntity();
        entity.setContractPhaseCode("Code1");
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

    private ContractPhaseCodeModel createTestModel() {
        ContractPhaseCodeModel model = new ContractPhaseCodeModel();
        model.setContractPhaseCode("Code1");
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
