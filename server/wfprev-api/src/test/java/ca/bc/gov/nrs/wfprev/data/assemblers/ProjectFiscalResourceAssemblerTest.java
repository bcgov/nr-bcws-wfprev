package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
                .planFiscalStatusCode("FISCAL1")
                .proposalTypeCode("NEW")
                .projectFiscalName("Test Project Fiscal")
                .isApprovedInd(true)
                .isDelayedInd(false)
                .revisionCount(1)
                .createUser("tester")
                .createDate(new Date())
                .updateUser("tester2")
                .updateDate(new Date())
                .build();

        // Act
        ProjectFiscalModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(model.getProjectPlanFiscalGuid(), entity.getProjectPlanFiscalGuid().toString());
        assertEquals(model.getActivityCategoryCode(), entity.getActivityCategoryCode());
        assertEquals(model.getFiscalYear(), entity.getFiscalYear().longValue());
        assertEquals(model.getProjectPlanStatusCode(), entity.getProjectPlanStatusCode());
        assertEquals(model.getPlanFiscalStatusCode(), entity.getPlanFiscalStatusCode());
        assertEquals(model.getProposalTypeCode(), entity.getProposalTypeCode());
        assertEquals(model.getProjectFiscalName(), entity.getProjectFiscalName());
        assertEquals(model.getIsApprovedInd(), entity.getIsApprovedInd());
        assertEquals(model.getIsDelayedInd(), entity.getIsDelayedInd());
    }

    @Test
    void testToEntity_MapsModelToEntity() {
        // GIVEN a ProjectFiscalModel with populated fields
        ProjectFiscalModel model = new ProjectFiscalModel();
        model.setProjectPlanFiscalGuid(UUID.randomUUID().toString());
        model.setActivityCategoryCode("ACT001");
        model.setFiscalYear(2023L);
        model.setProjectPlanStatusCode("STATUS1");
        model.setPlanFiscalStatusCode("FISCAL1");
        model.setProposalTypeCode("NEW");
        model.setProjectFiscalName("Test Project Fiscal");
        model.setIsApprovedInd(true);
        model.setIsDelayedInd(false);
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
        assertEquals(entity.getPlanFiscalStatusCode(), model.getPlanFiscalStatusCode(), "PlanFiscalStatusCode should match");
        assertEquals(entity.getProposalTypeCode(), model.getProposalTypeCode(), "ProposalTypeCode should match");
        assertEquals(entity.getProjectFiscalName(), model.getProjectFiscalName(), "ProjectFiscalName should match");
        assertEquals(entity.getIsApprovedInd(), model.getIsApprovedInd(), "IsApprovedInd should match");
        assertEquals(entity.getIsDelayedInd(), model.getIsDelayedInd(), "IsDelayedInd should match");
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
}
