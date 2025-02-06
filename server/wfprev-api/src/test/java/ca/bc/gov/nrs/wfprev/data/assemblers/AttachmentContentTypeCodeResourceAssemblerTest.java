package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.AttachmentContentTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.AttachmentContentTypeCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttachmentContentTypeCodeResourceAssemblerTest {

    private AttachmentContentTypeCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new AttachmentContentTypeCodeResourceAssembler();
    }

    @Test
    void testToModel_Success() {
        // Arrange
        AttachmentContentTypeCodeEntity entity = createTestEntity();

        // Act
        AttachmentContentTypeCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getAttachmentContentTypeCode(), model.getAttachmentContentTypeCode());
        assertEquals(entity.getDescription(), model.getDescription());
        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().hasLink("self"));
    }

    @Test
    void testToEntity_Success() {
        // Arrange
        AttachmentContentTypeCodeModel model = createTestModel();

        // Act
        AttachmentContentTypeCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getAttachmentContentTypeCode(), entity.getAttachmentContentTypeCode());
        assertEquals(model.getDescription(), entity.getDescription());
    }

    @Test
    void testToCollectionModel_Success() {
        // Arrange
        AttachmentContentTypeCodeEntity entity1 = createTestEntity();
        AttachmentContentTypeCodeEntity entity2 = createTestEntity();
        entity2.setAttachmentContentTypeCode("Code2");
        List<AttachmentContentTypeCodeEntity> entities = Arrays.asList(entity1, entity2);

        // Act
        CollectionModel<AttachmentContentTypeCodeModel> collectionModel = assembler.toCollectionModel(entities);

        // Assert
        assertNotNull(collectionModel);
        assertEquals(2, collectionModel.getContent().size());
        assertNotNull(collectionModel.getLinks());
        assertTrue(collectionModel.getLinks().hasLink("self"));
    }

    @Test
    void testToNullEntity_Success() {
        // Arrange
        AttachmentContentTypeCodeModel model = null;

        // Act
        AttachmentContentTypeCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNull(entity);
    }

    private AttachmentContentTypeCodeEntity createTestEntity() {
        AttachmentContentTypeCodeEntity entity = new AttachmentContentTypeCodeEntity();
        entity.setAttachmentContentTypeCode("Code1");
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

    private AttachmentContentTypeCodeModel createTestModel() {
        AttachmentContentTypeCodeModel model = new AttachmentContentTypeCodeModel();
        model.setAttachmentContentTypeCode("Code1");
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
