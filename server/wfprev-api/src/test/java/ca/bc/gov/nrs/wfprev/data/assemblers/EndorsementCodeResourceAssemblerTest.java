package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.EndorsementCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.EndorsementCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EndorsementCodeResourceAssemblerTest {

    private EndorsementCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new EndorsementCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        EndorsementCodeEntity entity = createTestEntity();

        // Act
        EndorsementCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getEndorsementCode(), model.getEndorsementCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        EndorsementCodeModel model = createTestModel();

        // Act
        EndorsementCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getEndorsementCode(), entity.getEndorsementCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        EndorsementCodeEntity entity1 = createTestEntity();
        EndorsementCodeEntity entity2 = createTestEntity();
        entity2.setEndorsementCode("Code2");
        List<EndorsementCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<EndorsementCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        EndorsementCodeModel model = null;

        // Act
        EndorsementCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private EndorsementCodeEntity createTestEntity() {
        EndorsementCodeEntity entity = new EndorsementCodeEntity();
        entity.setEndorsementCode("Code1");
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

    private EndorsementCodeModel createTestModel() {
        EndorsementCodeModel model = new EndorsementCodeModel();
        model.setEndorsementCode("Code1");
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
