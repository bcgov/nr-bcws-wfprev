package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionCodeEntity;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EvaluationCriteriaSectionCodeEntityTest {

    @Test
    public void test_create_entity_with_all_fields() {
        String evaluationCriteriaSectionCode = "ECS123";
        String description = "Sample Description";
        Integer displayOrder = 1;
        Date effectiveDate = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + 1000000);
        Integer revisionCount = 0;
        String createUser = "creator";
        Date createDate = new Date();
        String updateUser = "updater";
        Date updateDate = new Date();

        EvaluationCriteriaSectionCodeEntity entity = new EvaluationCriteriaSectionCodeEntity();
        entity.setEvaluationCriteriaSectionCode(evaluationCriteriaSectionCode);
        entity.setDescription(description);
        entity.setDisplayOrder(displayOrder);
        entity.setEffectiveDate(effectiveDate);
        entity.setExpiryDate(expiryDate);
        entity.setRevisionCount(revisionCount);
        entity.setCreateUser(createUser);
        entity.setCreateDate(createDate);
        entity.setUpdateUser(updateUser);
        entity.setUpdateDate(updateDate);

        assertNotNull(entity);
        assertEquals(evaluationCriteriaSectionCode, entity.getEvaluationCriteriaSectionCode());
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
    public void test_date_fields_timezone_conversion() {
        EvaluationCriteriaSectionCodeEntity entity = new EvaluationCriteriaSectionCodeEntity();
        entity.setEvaluationCriteriaSectionCode("ECS123");
        entity.setDescription("Test Description");
        entity.setEffectiveDate(new Date());
        entity.setExpiryDate(new Date());
        entity.setCreateUser("testUser");
        entity.setCreateDate(new Date());
        entity.setUpdateUser("testUser");
        entity.setUpdateDate(new Date());
        entity.setRevisionCount(1);

        entity.setEvaluationCriteriaSectionCode("ECS123");
        entity.setDescription("Test Description");
        entity.setEffectiveDate(new Date());
        entity.setExpiryDate(new Date());
        entity.setCreateUser("testUser");

        Date effectiveDate = entity.getEffectiveDate();
        Date expiryDate = entity.getExpiryDate();
        Date createDate = entity.getCreateDate();
        Date updateDate = entity.getUpdateDate();

        assertNotNull(effectiveDate);
        assertNotNull(expiryDate);
        assertNotNull(createDate);
        assertNotNull(updateDate);
    }

    @Test
    public void test_equals_and_hashcode() {
        EvaluationCriteriaSectionCodeEntity entity1 = new EvaluationCriteriaSectionCodeEntity();
        entity1.setEvaluationCriteriaSectionCode("ECS123");
        entity1.setDescription("Evaluation Criteria Section 123");
        entity1.setDisplayOrder(1);
        entity1.setEffectiveDate(new Date());
        entity1.setExpiryDate(new Date());
        entity1.setRevisionCount(0);
        entity1.setCreateUser("user1");
        entity1.setCreateDate(new Date());
        entity1.setUpdateUser("user1");
        entity1.setUpdateDate(new Date());

        EvaluationCriteriaSectionCodeEntity entity2 = new EvaluationCriteriaSectionCodeEntity();
        entity2.setEvaluationCriteriaSectionCode("ECS123");
        entity2.setDescription("Evaluation Criteria Section 123");
        entity2.setDisplayOrder(1);
        entity2.setEffectiveDate(entity1.getEffectiveDate());
        entity2.setExpiryDate(entity1.getExpiryDate());
        entity2.setRevisionCount(0);
        entity2.setCreateUser("user1");
        entity2.setCreateDate(entity1.getCreateDate());
        entity2.setUpdateUser("user1");
        entity2.setUpdateDate(entity1.getUpdateDate());

        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    public void test_audit_fields_population() {
        EvaluationCriteriaSectionCodeEntity entity = new EvaluationCriteriaSectionCodeEntity();
        String expectedCreateUser = "system";
        Date expectedCreateDate = new Date();
        String expectedUpdateUser = "system";
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
