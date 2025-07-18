package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.WildfireOrgUnitTypeCodeEntity;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class WildfireOrgUnitTypeCodeEntityTest {

    @Test
    public void test_create_entity_with_all_fields() {
        String code = "FIRE_CENTRE";
        String description = "Fire Centre";
        Integer displayOrder = 1;
        Date effectiveDate = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + 86400000);
        Integer revisionCount = 1;
        String createUser = "creator";
        Date createDate = new Date();
        String updateUser = "updater";
        Date updateDate = new Date();

        WildfireOrgUnitTypeCodeEntity entity = WildfireOrgUnitTypeCodeEntity.builder()
                .wildfireOrgUnitTypeCode(code)
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

        assertNotNull(entity);
        assertEquals(code, entity.getWildfireOrgUnitTypeCode());
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

    @Test
    public void test_date_fields_not_null() {
        WildfireOrgUnitTypeCodeEntity entity = WildfireOrgUnitTypeCodeEntity.builder()
                .wildfireOrgUnitTypeCode("ZONE")
                .description("Fire Zone")
                .effectiveDate(new Date())
                .expiryDate(new Date())
                .revisionCount(0)
                .createUser("admin")
                .createDate(new Date())
                .updateUser("admin")
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

        WildfireOrgUnitTypeCodeEntity entity1 = WildfireOrgUnitTypeCodeEntity.builder()
                .wildfireOrgUnitTypeCode("FIRE_CENTRE")
                .description("Fire Centre")
                .displayOrder(1)
                .effectiveDate(now)
                .expiryDate(now)
                .revisionCount(1)
                .createUser("user")
                .createDate(now)
                .updateUser("user")
                .updateDate(now)
                .build();

        WildfireOrgUnitTypeCodeEntity entity2 = WildfireOrgUnitTypeCodeEntity.builder()
                .wildfireOrgUnitTypeCode("FIRE_CENTRE")
                .description("Fire Centre")
                .displayOrder(1)
                .effectiveDate(now)
                .expiryDate(now)
                .revisionCount(1)
                .createUser("user")
                .createDate(now)
                .updateUser("user")
                .updateDate(now)
                .build();

        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    public void test_audit_fields_population() {
        WildfireOrgUnitTypeCodeEntity entity = new WildfireOrgUnitTypeCodeEntity();
        String createUser = "auditor";
        Date createDate = new Date();
        String updateUser = "auditor";
        Date updateDate = new Date();

        entity.setCreateUser(createUser);
        entity.setCreateDate(createDate);
        entity.setUpdateUser(updateUser);
        entity.setUpdateDate(updateDate);

        assertEquals(createUser, entity.getCreateUser());
        assertEquals(createDate, entity.getCreateDate());
        assertEquals(updateUser, entity.getUpdateUser());
        assertEquals(updateDate, entity.getUpdateDate());
    }
}
