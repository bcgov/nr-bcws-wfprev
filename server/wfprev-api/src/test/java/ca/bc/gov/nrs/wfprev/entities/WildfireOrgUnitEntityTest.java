package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.WildfireOrgUnitEntity;
import ca.bc.gov.nrs.wfprev.data.entities.WildfireOrgUnitTypeCodeEntity;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WildfireOrgUnitEntityTest {

    @Test
    public void test_create_entity_with_all_fields() {
        Integer orgUnitIdentifier = 100;
        Date effectiveDate = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + 86400000);
        WildfireOrgUnitTypeCodeEntity typeCodeEntity = new WildfireOrgUnitTypeCodeEntity();
        Integer parentOrgUnitIdentifier = 99;
        String orgUnitName = "Kamloops Fire Centre";
        Integer integerAlias = 5;
        String characterAlias = "KFC";
        Integer revisionCount = 1;
        String createUser = "creator";
        Date createDate = new Date();
        String updateUser = "updater";
        Date updateDate = new Date();

        WildfireOrgUnitEntity entity = WildfireOrgUnitEntity.builder()
                .orgUnitIdentifier(orgUnitIdentifier)
                .effectiveDate(effectiveDate)
                .expiryDate(expiryDate)
                .wildfireOrgUnitTypeCode(typeCodeEntity)
                .parentOrgUnitIdentifier(parentOrgUnitIdentifier)
                .orgUnitName(orgUnitName)
                .integerAlias(integerAlias)
                .characterAlias(characterAlias)
                .revisionCount(revisionCount)
                .createUser(createUser)
                .createDate(createDate)
                .updateUser(updateUser)
                .updateDate(updateDate)
                .build();

        assertNotNull(entity);
        assertEquals(orgUnitIdentifier, entity.getOrgUnitIdentifier());
        assertEquals(effectiveDate, entity.getEffectiveDate());
        assertEquals(expiryDate, entity.getExpiryDate());
        assertEquals(typeCodeEntity, entity.getWildfireOrgUnitTypeCode());
        assertEquals(parentOrgUnitIdentifier, entity.getParentOrgUnitIdentifier());
        assertEquals(orgUnitName, entity.getOrgUnitName());
        assertEquals(integerAlias, entity.getIntegerAlias());
        assertEquals(characterAlias, entity.getCharacterAlias());
        assertEquals(revisionCount, entity.getRevisionCount());
        assertEquals(createUser, entity.getCreateUser());
        assertEquals(createDate, entity.getCreateDate());
        assertEquals(updateUser, entity.getUpdateUser());
        assertEquals(updateDate, entity.getUpdateDate());
    }

    @Test
    public void test_date_fields_not_null() {
        WildfireOrgUnitEntity entity = WildfireOrgUnitEntity.builder()
                .orgUnitIdentifier(1)
                .effectiveDate(new Date())
                .expiryDate(new Date())
                .revisionCount(0)
                .createUser("user")
                .createDate(new Date())
                .updateUser("user")
                .updateDate(new Date())
                .build();

        assertNotNull(entity.getEffectiveDate());
        assertNotNull(entity.getExpiryDate());
        assertNotNull(entity.getCreateDate());
        assertNotNull(entity.getUpdateDate());
    }

    @Test
    public void test_equals_and_hashcode() {
        Date now = new Date();

        WildfireOrgUnitEntity entity1 = WildfireOrgUnitEntity.builder()
                .orgUnitIdentifier(1)
                .effectiveDate(now)
                .expiryDate(now)
                .revisionCount(1)
                .createUser("creator")
                .createDate(now)
                .updateUser("updater")
                .updateDate(now)
                .build();

        WildfireOrgUnitEntity entity2 = WildfireOrgUnitEntity.builder()
                .orgUnitIdentifier(1)
                .effectiveDate(now)
                .expiryDate(now)
                .revisionCount(1)
                .createUser("creator")
                .createDate(now)
                .updateUser("updater")
                .updateDate(now)
                .build();

        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    public void test_audit_fields_population() {
        WildfireOrgUnitEntity entity = new WildfireOrgUnitEntity();
        String expectedCreateUser = "auditor";
        Date expectedCreateDate = new Date();
        String expectedUpdateUser = "auditor";
        Date expectedUpdateDate = new Date();

        entity.setCreateUser(expectedCreateUser);
        entity.setCreateDate(expectedCreateDate);
        entity.setUpdateUser(expectedUpdateUser);
        entity.setUpdateDate(expectedUpdateDate);

        assertEquals(expectedCreateUser, entity.getCreateUser());
        assertEquals(expectedCreateDate, entity.getCreateDate());
        assertEquals(expectedUpdateUser, entity.getUpdateUser());
        assertEquals(expectedUpdateDate, entity.getUpdateDate());
    }
}
