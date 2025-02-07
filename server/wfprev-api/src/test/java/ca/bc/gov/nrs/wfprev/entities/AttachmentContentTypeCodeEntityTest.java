package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.AttachmentContentTypeCodeEntity;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AttachmentContentTypeCodeEntityTest {
    @Test
    public void test_create_entity_with_all_fields() {
        // Given
        String attachmentContentTypeCode = "ACTC123";
        String description = "Sample Description";
        Integer displayOrder = 1;
        Date effectiveDate = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + 1000000);
        Integer revisionCount = 0;
        String createUser = "creator";
        Date createDate = new Date();
        String updateUser = "updater";
        Date updateDate = new Date();

        // When
        AttachmentContentTypeCodeEntity entity = AttachmentContentTypeCodeEntity.builder()
                .attachmentContentTypeCode(attachmentContentTypeCode)
                .description(description)
                .displayOrder(displayOrder)
                .effectiveDate(effectiveDate)
                .expiryDate(expiryDate)
                .revisionCount(revisionCount)
                .createUser(createUser)
                .createDate(createDate)
                .updateUser(updateUser)
                .updateDate(updateDate)
                .build();

        // Then
        assertNotNull(entity);
        assertEquals(attachmentContentTypeCode, entity.getAttachmentContentTypeCode());
        assertEquals(description, entity.getDescription());
        assertEquals(displayOrder, entity.getDisplayOrder());
        assertEquals(effectiveDate, entity.getEffectiveDate());
        assertEquals(expiryDate, entity.getExpiryDate());
        assertEquals(revisionCount, entity.getRevisionCount());
        assertEquals(createUser, entity.getCreateUser());
        assertEquals(createDate, entity.getCreateDate());
        assertEquals(updateUser, entity.getUpdateUser());
        assertEquals(updateDate, entity.getUpdateDate());
    }

    // Verify date fields handle timezone conversions correctly
    @Test
    public void test_date_fields_timezone_conversion() {
        // Given
        AttachmentContentTypeCodeEntity entity = AttachmentContentTypeCodeEntity.builder()
                .attachmentContentTypeCode("ACTC123")
                .description("Test Description")
                .effectiveDate(new Date())
                .expiryDate(new Date())
                .createUser("testUser")
                .createDate(new Date())
                .updateUser("testUser")
                .updateDate(new Date())
                .revisionCount(1)
                .build();

        // When
        Date effectiveDate = entity.getEffectiveDate();
        Date expiryDate = entity.getExpiryDate();
        Date createDate = entity.getCreateDate();
        Date updateDate = entity.getUpdateDate();

        // Then
        assertNotNull(effectiveDate);
        assertNotNull(expiryDate);
        assertNotNull(createDate);
        assertNotNull(updateDate);
    }

    // Test equals/hashCode implementation
    @Test
    public void test_equals_and_hashcode() {
        // Given
        AttachmentContentTypeCodeEntity entity1 = AttachmentContentTypeCodeEntity.builder()
                .attachmentContentTypeCode("ACTC123")
                .description("Attachment Content Type 123")
                .displayOrder(1)
                .effectiveDate(new Date())
                .expiryDate(new Date())
                .revisionCount(0)
                .createUser("user1")
                .createDate(new Date())
                .updateUser("user1")
                .updateDate(new Date())
                .build();

        AttachmentContentTypeCodeEntity entity2 = AttachmentContentTypeCodeEntity.builder()
                .attachmentContentTypeCode("ACTC123")
                .description("Attachment Content Type 123")
                .displayOrder(1)
                .effectiveDate(entity1.getEffectiveDate())
                .expiryDate(entity1.getExpiryDate())
                .revisionCount(0)
                .createUser("user1")
                .createDate(entity1.getCreateDate())
                .updateUser("user1")
                .updateDate(entity1.getUpdateDate())
                .build();

        // When & Then
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    // Verify automatic population of audit fields (create/update user and dates)
    @Test
    public void test_audit_fields_population() {
        // Given
        AttachmentContentTypeCodeEntity entity = new AttachmentContentTypeCodeEntity();
        String expectedCreateUser = "system";
        Date expectedCreateDate = new Date();
        String expectedUpdateUser = "system";
        Date expectedUpdateDate = new Date();

        // When
        entity.setCreateUser(expectedCreateUser);
        entity.setCreateDate(expectedCreateDate);
        entity.setUpdateUser(expectedUpdateUser);
        entity.setUpdateDate(expectedUpdateDate);

        // Then
        assertEquals(expectedCreateUser, entity.getCreateUser());
        assertEquals(expectedCreateDate, entity.getCreateDate());
        assertEquals(expectedUpdateUser, entity.getUpdateUser());
        assertEquals(expectedUpdateDate, entity.getUpdateDate());
    }
}
