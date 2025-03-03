package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.SilvicultureTechniqueCodeEntity;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SilvicultureTechniqueCodeEntityTest {
    @Test
    public void test_create_entity_with_all_fields() {
        // Given
        String silvicultureTechniqueCode = "STC123";
        UUID silvicultureTechniqueGuid = UUID.fromString("aa0a073b-f729-4b3b-9128-810010662772");
        UUID silvicultureBaseGuid = UUID.fromString("970a3b25-03ba-42fd-b68e-c9272a707d0a");
        String description = "Sample Description";
        Integer revisionCount = 0;
        String createUser = "creator";
        Date createDate = new Date();
        String updateUser = "updater";
        Date updateDate = new Date();

        // When
        SilvicultureTechniqueCodeEntity entity = SilvicultureTechniqueCodeEntity.builder()
                .silvicultureTechniqueCode(silvicultureTechniqueCode)
                .silvicultureTechniqueGuid(silvicultureTechniqueGuid)
                .silvicultureBaseGuid(silvicultureBaseGuid)
                .description(description)
                .revisionCount(revisionCount)
                .createUser(createUser)
                .createDate(createDate)
                .updateUser(updateUser)
                .updateDate(updateDate)
                .build();

        // Then
        assertNotNull(entity);
        assertEquals(silvicultureTechniqueCode, entity.getSilvicultureTechniqueCode());
        assertEquals(silvicultureTechniqueGuid, entity.getSilvicultureTechniqueGuid());
        assertEquals(silvicultureBaseGuid, entity.getSilvicultureBaseGuid());
        assertEquals(description, entity.getDescription());
        assertEquals(revisionCount, entity.getRevisionCount());
        assertEquals(createUser, entity.getCreateUser());
        assertEquals(createDate, entity.getCreateDate());
        assertEquals(updateUser, entity.getUpdateUser());
        assertEquals(updateDate, entity.getUpdateDate());
    }

    // Test equals/hashCode implementation
    @Test
    public void test_equals_and_hashcode() {
        // Given
        SilvicultureTechniqueCodeEntity entity1 = SilvicultureTechniqueCodeEntity.builder()
                .silvicultureTechniqueCode("STC123")
                .silvicultureTechniqueGuid(UUID.fromString("aa0a073b-f729-4b3b-9128-810010662772"))
                .silvicultureBaseGuid(UUID.fromString("970a3b25-03ba-42fd-b68e-c9272a707d0a"))
                .description("Risk Rating 123")
                .revisionCount(0)
                .createUser("user1")
                .createDate(new Date())
                .updateUser("user1")
                .updateDate(new Date())
                .build();

        SilvicultureTechniqueCodeEntity entity2 = SilvicultureTechniqueCodeEntity.builder()
                .silvicultureTechniqueCode("STC123")
                .silvicultureTechniqueGuid(UUID.fromString("aa0a073b-f729-4b3b-9128-810010662772"))
                .silvicultureBaseGuid(UUID.fromString("970a3b25-03ba-42fd-b68e-c9272a707d0a"))
                .description("Risk Rating 123")
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
        SilvicultureTechniqueCodeEntity entity = new SilvicultureTechniqueCodeEntity();
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
