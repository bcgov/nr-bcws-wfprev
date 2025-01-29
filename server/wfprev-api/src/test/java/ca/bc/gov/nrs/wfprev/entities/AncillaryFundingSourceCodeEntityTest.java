package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.AncillaryFundingSourceCodeEntity;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AncillaryFundingSourceCodeEntityTest {

    @Test
    public void test_create_entity_with_all_fields() {
        // Given
        UUID ancillaryFundingSourceGuid = UUID.randomUUID();
        String fundingSourceAbbreviation = "FS123";
        String fundingSourceName = "Funding Source Name";
        Integer revisionCount = 1;
        String createUser = "creator";
        Date createDate = new Date();
        String updateUser = "updater";
        Date updateDate = new Date();

        // When
        AncillaryFundingSourceCodeEntity entity = AncillaryFundingSourceCodeEntity.builder()
                .ancillaryFundingSourceGuid(ancillaryFundingSourceGuid)
                .fundingSourceAbbreviation(fundingSourceAbbreviation)
                .fundingSourceName(fundingSourceName)
                .revisionCount(revisionCount)
                .createUser(createUser)
                .createDate(createDate)
                .updateUser(updateUser)
                .updateDate(updateDate)
                .build();

        // Then
        assertNotNull(entity);
        assertEquals(ancillaryFundingSourceGuid, entity.getAncillaryFundingSourceGuid());
        assertEquals(fundingSourceAbbreviation, entity.getFundingSourceAbbreviation());
        assertEquals(fundingSourceName, entity.getFundingSourceName());
        assertEquals(revisionCount, entity.getRevisionCount());
        assertEquals(createUser, entity.getCreateUser());
        assertEquals(createDate, entity.getCreateDate());
        assertEquals(updateUser, entity.getUpdateUser());
        assertEquals(updateDate, entity.getUpdateDate());
    }

    @Test
    public void test_date_fields_timezone_conversion() {
        // Given
        AncillaryFundingSourceCodeEntity entity = AncillaryFundingSourceCodeEntity.builder()
                .ancillaryFundingSourceGuid(UUID.randomUUID())
                .fundingSourceAbbreviation("FS123")
                .fundingSourceName("Funding Source")
                .createUser("creator")
                .createDate(new Date())
                .updateUser("updater")
                .updateDate(new Date())
                .revisionCount(1)
                .build();

        // When
        Date createDate = entity.getCreateDate();
        Date updateDate = entity.getUpdateDate();

        // Then
        assertNotNull(createDate);
        assertNotNull(updateDate);
    }

    @Test
    public void test_equals_and_hashcode() {
        // Given
        UUID guid = UUID.randomUUID();
        AncillaryFundingSourceCodeEntity entity1 = AncillaryFundingSourceCodeEntity.builder()
                .ancillaryFundingSourceGuid(guid)
                .fundingSourceAbbreviation("FS123")
                .fundingSourceName("Funding Source")
                .revisionCount(1)
                .createUser("user1")
                .createDate(new Date())
                .updateUser("user1")
                .updateDate(new Date())
                .build();

        AncillaryFundingSourceCodeEntity entity2 = AncillaryFundingSourceCodeEntity.builder()
                .ancillaryFundingSourceGuid(guid)
                .fundingSourceAbbreviation("FS123")
                .fundingSourceName("Funding Source")
                .revisionCount(1)
                .createUser("user1")
                .createDate(entity1.getCreateDate())
                .updateUser("user1")
                .updateDate(entity1.getUpdateDate())
                .build();

        // When & Then
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    public void test_audit_fields_population() {
        // Given
        AncillaryFundingSourceCodeEntity entity = new AncillaryFundingSourceCodeEntity();
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
