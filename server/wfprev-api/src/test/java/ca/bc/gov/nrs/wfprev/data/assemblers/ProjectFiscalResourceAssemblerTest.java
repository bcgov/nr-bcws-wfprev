package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.PlanFiscalStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.models.PlanFiscalStatusCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ProjectFiscalResourceAssemblerTest {

    ProjectFiscalResourceAssembler assembler = new ProjectFiscalResourceAssembler();

    ProjectEntity projectEntity = mock(ProjectEntity.class);

    @Test
    void testToModel_MapsEntityToModel() {
        // Arrange
        ProjectFiscalEntity entity = ProjectFiscalEntity.builder()
                .projectPlanFiscalGuid(UUID.randomUUID())
                .activityCategoryCode("ACT001")
                .fiscalYear(new BigDecimal("2023"))
                .projectPlanStatusCode("STATUS1")
                .planFiscalStatusCode(new PlanFiscalStatusCodeEntity())
                .proposalTypeCode("NEW")
                .projectFiscalName("Test Project Fiscal")
                .isApprovedInd(true)
                .isDelayedInd(false)
                .revisionCount(1)
                .createUser("tester")
                .createDate(new Date())
                .updateUser("tester2")
                .updateDate(new Date())
                .endorseApprUpdateName("endorser name")
                .endorseApprUpdateUserGuid("user-guid-123")
                .endorseApprUpdateUserid("idir123")
                .endorseApprUpdatedTimestamp(new Date())
                .isBcwsHQApprovedInd(true)
                .bcwsHQApproverName("Joe Bloggs")
                .bcwsHQApproverUserGuid(UUID.randomUUID().toString())
                .bcwsHQApproverUserUserid(UUID.randomUUID().toString())
                .bcwsHQApprovedTimestamp(new Date())
                .bcwsHQApprovedComment("Approved")
                .build();

        // Act
        ProjectFiscalModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(model.getProjectPlanFiscalGuid(), entity.getProjectPlanFiscalGuid().toString());
        assertEquals(model.getActivityCategoryCode(), entity.getActivityCategoryCode());
        assertEquals(model.getFiscalYear(), entity.getFiscalYear().longValue());
        assertEquals(model.getProjectPlanStatusCode(), entity.getProjectPlanStatusCode());
        assertEquals(model.getPlanFiscalStatusCode().getPlanFiscalStatusCode(), entity.getPlanFiscalStatusCode().getPlanFiscalStatusCode());
        assertEquals(model.getProposalTypeCode(), entity.getProposalTypeCode());
        assertEquals(model.getProjectFiscalName(), entity.getProjectFiscalName());
        assertEquals(model.getIsApprovedInd(), entity.getIsApprovedInd());
        assertEquals(model.getIsDelayedInd(), entity.getIsDelayedInd());
        assertEquals(model.getEndorseApprUpdateName(), entity.getEndorseApprUpdateName());
        assertEquals(model.getEndorseApprUpdateUserGuid(), entity.getEndorseApprUpdateUserGuid());
        assertEquals(model.getEndorseApprUpdateUserid(), entity.getEndorseApprUpdateUserid());
        assertEquals(model.getEndorseApprUpdatedTimestamp(), entity.getEndorseApprUpdatedTimestamp());
    }

    @Test
    void testToEntity_MapsModelToEntity() {
        // GIVEN a ProjectFiscalModel with populated fields
        ProjectFiscalModel model = new ProjectFiscalModel();
        model.setProjectPlanFiscalGuid(UUID.randomUUID().toString());
        model.setActivityCategoryCode("ACT001");
        model.setFiscalYear(2023L);
        model.setProjectPlanStatusCode("STATUS1");
        model.setPlanFiscalStatusCode(new PlanFiscalStatusCodeModel());
        model.setProposalTypeCode("NEW");
        model.setProjectFiscalName("Test Project Fiscal");
        model.setIsApprovedInd(true);
        model.setIsDelayedInd(false);
        model.setIsBcwsHQApprovedInd(true);
        model.setBcwsHQApproverName("Joe Bloggs");
        model.setBcwsHQApproverUserGuid(UUID.randomUUID().toString());
        model.setBcwsHQApproverUserUserid(UUID.randomUUID().toString());
        model.setBcwsHQApprovedTimestamp(new Date());
        model.setBcwsHQApprovedComment("Approved");
        model.setRevisionCount(1);
        model.setCreateUser("tester");
        model.setCreateDate(new Date());
        model.setUpdateUser("tester2");
        model.setUpdateDate(new Date());

        // WHEN the model is converted to an entity using the assembler
        ProjectFiscalEntity entity = assembler.toEntity(model, projectEntity);

        // THEN the resulting entity fields should match the corresponding model fields
        assertNotNull(entity, "The resulting entity should not be null");
        assertEquals(entity.getProjectPlanFiscalGuid().toString(), model.getProjectPlanFiscalGuid(), "ProjectPlanFiscalGuid should match");
        assertEquals(entity.getActivityCategoryCode(), model.getActivityCategoryCode(), "ActivityCategoryCode should match");
        assertEquals(entity.getFiscalYear().longValue(), model.getFiscalYear(), "FiscalYear should match");
        assertEquals(entity.getProjectPlanStatusCode(), model.getProjectPlanStatusCode(), "ProjectPlanStatusCode should match");
        assertEquals(entity.getPlanFiscalStatusCode().getPlanFiscalStatusCode(), model.getPlanFiscalStatusCode().getPlanFiscalStatusCode(), "PlanFiscalStatusCode should match");
        assertEquals(entity.getProposalTypeCode(), model.getProposalTypeCode(), "ProposalTypeCode should match");
        assertEquals(entity.getProjectFiscalName(), model.getProjectFiscalName(), "ProjectFiscalName should match");
        assertEquals(entity.getIsApprovedInd(), model.getIsApprovedInd(), "IsApprovedInd should match");
        assertEquals(entity.getIsDelayedInd(), model.getIsDelayedInd(), "IsDelayedInd should match");
        assertEquals(entity.getEndorseApprUpdateName(), model.getEndorseApprUpdateName());
        assertEquals(entity.getEndorseApprUpdateUserGuid(), model.getEndorseApprUpdateUserGuid());
        assertEquals(entity.getEndorseApprUpdateUserid(), model.getEndorseApprUpdateUserid());
        assertEquals(entity.getEndorseApprUpdatedTimestamp(), model.getEndorseApprUpdatedTimestamp());
        assertEquals(entity.getIsBcwsHQApprovedInd(), model.getIsBcwsHQApprovedInd());
        assertEquals(entity.getBcwsHQApproverName(), model.getBcwsHQApproverName());
        assertEquals(entity.getBcwsHQApproverUserGuid(), model.getBcwsHQApproverUserGuid());
        assertEquals(entity.getBcwsHQApproverUserUserid(), model.getBcwsHQApproverUserUserid());
        assertEquals(entity.getBcwsHQApprovedTimestamp(), model.getBcwsHQApprovedTimestamp());
        assertEquals(entity.getBcwsHQApprovedComment(), model.getBcwsHQApprovedComment());
    }

    @Test
    void testUpdateEntity() {
        ProjectFiscalModel model = new ProjectFiscalModel();
        model.setActivityCategoryCode("UPDATED_CODE");
        model.setFiscalYear(2022L);

        ProjectFiscalEntity existingEntity = new ProjectFiscalEntity();
        existingEntity.setActivityCategoryCode("OLD_CODE");
        existingEntity.setFiscalYear(BigDecimal.valueOf(2021));

        ProjectFiscalEntity updatedEntity = assembler.updateEntity(model, existingEntity);

        assertEquals("UPDATED_CODE", updatedEntity.getActivityCategoryCode());
        assertEquals(BigDecimal.valueOf(2022), updatedEntity.getFiscalYear());
    }

    @Test
    void testUpdateEntity_ClearsBcwsHQApprovalFieldsWhenUnapproved() {
        ProjectFiscalEntity existingEntity = new ProjectFiscalEntity();
        existingEntity.setIsBcwsHQApprovedInd(true);
        existingEntity.setBcwsHQApproverName("HQ Approver");
        existingEntity.setBcwsHQApproverUserGuid("guid-123");
        existingEntity.setBcwsHQApproverUserUserid("idir-123");
        existingEntity.setBcwsHQApprovedTimestamp(new Date());
        existingEntity.setBcwsHQApprovedComment("Approved by HQ");

        ProjectFiscalModel model = new ProjectFiscalModel();
        model.setIsBcwsHQApprovedInd(false);
        model.setBcwsHQApproverName(null);
        model.setBcwsHQApproverUserGuid(null);
        model.setBcwsHQApproverUserUserid(null);
        model.setBcwsHQApprovedTimestamp(null);
        model.setBcwsHQApprovedComment(null);

        ProjectFiscalEntity updatedEntity = assembler.updateEntity(model, existingEntity);

        assertFalse(updatedEntity.getIsBcwsHQApprovedInd());
        assertNull(updatedEntity.getBcwsHQApproverName());
        assertNull(updatedEntity.getBcwsHQApproverUserGuid());
        assertNull(updatedEntity.getBcwsHQApproverUserUserid());
        assertNull(updatedEntity.getBcwsHQApprovedTimestamp());
        assertNull(updatedEntity.getBcwsHQApprovedComment());
    }

    @Test
    void testUpdateEntity_PreservesBcwsHQApprovalFieldsWhenApproved() {
        Date approvalDate = new Date();
        ProjectFiscalEntity existingEntity = new ProjectFiscalEntity();
        existingEntity.setIsBcwsHQApprovedInd(false);

        ProjectFiscalModel model = new ProjectFiscalModel();
        model.setIsBcwsHQApprovedInd(true);
        model.setBcwsHQApproverName("HQ Approver");
        model.setBcwsHQApproverUserGuid("guid-456");
        model.setBcwsHQApproverUserUserid("idir-456");
        model.setBcwsHQApprovedTimestamp(approvalDate);
        model.setBcwsHQApprovedComment("Approved by HQ");

        ProjectFiscalEntity updatedEntity = assembler.updateEntity(model, existingEntity);

        assertTrue(updatedEntity.getIsBcwsHQApprovedInd());
        assertEquals("HQ Approver", updatedEntity.getBcwsHQApproverName());
        assertEquals("guid-456", updatedEntity.getBcwsHQApproverUserGuid());
        assertEquals("idir-456", updatedEntity.getBcwsHQApproverUserUserid());
        assertEquals(approvalDate, updatedEntity.getBcwsHQApprovedTimestamp());
        assertEquals("Approved by HQ", updatedEntity.getBcwsHQApprovedComment());
    }
}
