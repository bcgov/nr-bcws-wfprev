package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.FileAttachmentEntity;
import ca.bc.gov.nrs.wfprev.data.models.FileAttachmentModel;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FileAttachmentResourceAssemblerTest {

    FileAttachmentResourceAssembler assembler = new FileAttachmentResourceAssembler();

    @Test
    void testToModel_MapsEntityToModel() {
        // Arrange
        FileAttachmentEntity entity = new FileAttachmentEntity();
        entity.setFileAttachmentGuid(UUID.randomUUID());
        entity.setSourceObjectUniqueId("12345");
        entity.setDocumentPath("/documents/test.pdf");
        entity.setFileIdentifier("file123");
        entity.setWildfireYear(2025);
        entity.setAttachmentDescription("Test Description");
        entity.setAttachmentReadOnlyInd(true);
        entity.setUploadedByUserType("USR");
        entity.setUploadedByUserId("test_user");
        entity.setUploadedByTimestamp(new Date());
        entity.setRevisionCount(1);
        entity.setCreateUser("creator");
        entity.setCreateDate(new Date());
        entity.setUpdateUser("updater");
        entity.setUpdateDate(new Date());

        // Act
        FileAttachmentModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getFileAttachmentGuid().toString(), model.getFileAttachmentGuid());
        assertEquals(entity.getSourceObjectUniqueId(), model.getSourceObjectUniqueId());
        assertEquals(entity.getDocumentPath(), model.getDocumentPath());
        assertEquals(entity.getFileIdentifier(), model.getFileIdentifier());
        assertEquals(entity.getWildfireYear(), model.getWildfireYear());
        assertEquals(entity.getAttachmentDescription(), model.getAttachmentDescription());
        assertEquals(entity.getAttachmentReadOnlyInd(), model.getAttachmentReadOnlyInd());
        assertEquals(entity.getUploadedByUserType(), model.getUploadedByUserType());
        assertEquals(entity.getUploadedByUserId(), model.getUploadedByUserId());
        assertEquals(entity.getUploadedByTimestamp(), model.getUploadedByTimestamp());
        assertEquals(entity.getRevisionCount(), model.getRevisionCount());
        assertEquals(entity.getCreateUser(), model.getCreateUser());
        assertEquals(entity.getCreateDate(), model.getCreateDate());
        assertEquals(entity.getUpdateUser(), model.getUpdateUser());
        assertEquals(entity.getUpdateDate(), model.getUpdateDate());
    }

    @Test
    void testToEntity_MapsModelToEntity() {
        // Arrange
        FileAttachmentModel model = new FileAttachmentModel();
        model.setFileAttachmentGuid(UUID.randomUUID().toString());
        model.setSourceObjectUniqueId("12345");
        model.setDocumentPath("/documents/test.pdf");
        model.setFileIdentifier("file123");
        model.setWildfireYear(2025);
        model.setAttachmentDescription("Test Description");
        model.setAttachmentReadOnlyInd(true);
        model.setUploadedByUserType("USR");
        model.setUploadedByUserId("test_user");
        model.setUploadedByTimestamp(new Date());
        model.setRevisionCount(1);
        model.setCreateUser("creator");
        model.setCreateDate(new Date());
        model.setUpdateUser("updater");
        model.setUpdateDate(new Date());

        // Act
        FileAttachmentEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getFileAttachmentGuid(), entity.getFileAttachmentGuid().toString());
        assertEquals(model.getSourceObjectUniqueId(), entity.getSourceObjectUniqueId());
        assertEquals(model.getDocumentPath(), entity.getDocumentPath());
        assertEquals(model.getFileIdentifier(), entity.getFileIdentifier());
        assertEquals(model.getWildfireYear(), entity.getWildfireYear());
        assertEquals(model.getAttachmentDescription(), entity.getAttachmentDescription());
        assertEquals(model.getAttachmentReadOnlyInd(), entity.getAttachmentReadOnlyInd());
        assertEquals(model.getUploadedByUserType(), entity.getUploadedByUserType());
        assertEquals(model.getUploadedByUserId(), entity.getUploadedByUserId());
        assertEquals(model.getUploadedByTimestamp(), entity.getUploadedByTimestamp());
        assertEquals(model.getRevisionCount(), entity.getRevisionCount());
        assertEquals(model.getCreateUser(), entity.getCreateUser());
        assertEquals(model.getCreateDate(), entity.getCreateDate());
        assertEquals(model.getUpdateUser(), entity.getUpdateUser());
        assertEquals(model.getUpdateDate(), entity.getUpdateDate());
    }
}
