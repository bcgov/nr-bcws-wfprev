package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.SilvicultureBaseCodeEntity;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SilvicultureBaseCodeEntityTest {
    @Test
    public void test_create_entity_with_all_fields() {
        // Given
        String silvicultureBaseCode = "SBC123";
        UUID silvicultureBaseGuid = UUID.fromString("aa0a073b-f729-4b3b-9128-810010662772");
        String description = "Sample Description";
        String projectTypeCode = "FUEL_MGMT";
        Integer revisionCount = 0;
        String createUser = "creator";
        Date createDate = new Date();
        String updateUser = "updater";
        Date updateDate = new Date();

        // When
        SilvicultureBaseCodeEntity entity = SilvicultureBaseCodeEntity.builder()
                .silvicultureBaseCode(silvicultureBaseCode)
                .silvicultureBaseGuid(silvicultureBaseGuid)
                .description(description)
                .projectTypeCode(projectTypeCode)
                .revisionCount(revisionCount)
                .createUser(createUser)
                .createDate(createDate)
                .updateUser(updateUser)
                .updateDate(updateDate)
                .build();

        // Then
        assertNotNull(entity);
        assertEquals(silvicultureBaseCode, entity.getSilvicultureBaseCode());
        assertEquals(silvicultureBaseGuid, entity.getSilvicultureBaseGuid());
        assertEquals(description, entity.getDescription());
        assertEquals(projectTypeCode, entity.getProjectTypeCode());
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
        SilvicultureBaseCodeEntity entity1 = SilvicultureBaseCodeEntity.builder()
                .silvicultureBaseCode("SBC123")
                .silvicultureBaseGuid(UUID.fromString("aa0a073b-f729-4b3b-9128-810010662772"))
                .description("Risk Rating 123")
                .projectTypeCode("FUEL_MGMT")
                .revisionCount(0)
                .createUser("user1")
                .createDate(new Date())
                .updateUser("user1")
                .updateDate(new Date())
                .build();

        SilvicultureBaseCodeEntity entity2 = SilvicultureBaseCodeEntity.builder()
                .silvicultureBaseCode("SBC123")
                .silvicultureBaseGuid(UUID.fromString("aa0a073b-f729-4b3b-9128-810010662772"))
                .description("Risk Rating 123")
                .projectTypeCode("FUEL_MGMT")
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
        SilvicultureBaseCodeEntity entity = new SilvicultureBaseCodeEntity();
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
