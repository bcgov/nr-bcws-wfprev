package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSelectedEntity;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSelectedModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationCriteriaSelectedResourceAssemblerTest {

    private EvaluationCriteriaSelectedResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new EvaluationCriteriaSelectedResourceAssembler();
    }

    @Test
    void testToEntity_MapsModelToEntity() {
        // Arrange
        EvaluationCriteriaSelectedModel model = new EvaluationCriteriaSelectedModel();
        UUID selectedGuid = UUID.randomUUID();
        UUID criteriaGuid = UUID.randomUUID();
        UUID sectionSummaryGuid = UUID.randomUUID();

        model.setEvaluationCriteriaSelectedGuid(selectedGuid.toString());
        model.setEvaluationCriteriaGuid(criteriaGuid.toString());
        model.setEvaluationCriteriaSectionSummaryGuid(sectionSummaryGuid.toString());
        model.setIsEvaluationCriteriaSelectedInd(true);
        model.setRevisionCount(1);
        model.setCreateUser("tester");
        model.setCreateDate(new Date());
        model.setUpdateUser("updater");
        model.setUpdateDate(new Date());

        // Act
        EvaluationCriteriaSelectedEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(criteriaGuid, entity.getEvaluationCriteriaGuid());
        assertEquals(sectionSummaryGuid, entity.getEvaluationCriteriaSectionSummaryGuid());
        assertTrue(entity.getIsEvaluationCriteriaSelectedInd());
        assertEquals(1, entity.getRevisionCount());
        assertEquals("tester", entity.getCreateUser());
        assertEquals("updater", entity.getUpdateUser());
    }

    @Test
    void testToModel_MapsEntityToModel() {
        // Arrange
        EvaluationCriteriaSelectedEntity entity = new EvaluationCriteriaSelectedEntity();
        UUID selectedGuid = UUID.randomUUID();
        UUID criteriaGuid = UUID.randomUUID();
        UUID sectionSummaryGuid = UUID.randomUUID();

        entity.setEvaluationCriteriaSelectedGuid(selectedGuid);
        entity.setEvaluationCriteriaGuid(criteriaGuid);
        entity.setEvaluationCriteriaSectionSummaryGuid(sectionSummaryGuid);
        entity.setIsEvaluationCriteriaSelectedInd(false);
        entity.setRevisionCount(2);
        entity.setCreateUser("creator");
        entity.setCreateDate(new Date());
        entity.setUpdateUser("updater");
        entity.setUpdateDate(new Date());

        // Act
        EvaluationCriteriaSelectedModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(selectedGuid.toString(), model.getEvaluationCriteriaSelectedGuid());
        assertEquals(criteriaGuid.toString(), model.getEvaluationCriteriaGuid());
        assertEquals(sectionSummaryGuid.toString(), model.getEvaluationCriteriaSectionSummaryGuid());
        assertFalse(model.getIsEvaluationCriteriaSelectedInd());
        assertEquals(2, model.getRevisionCount());
        assertEquals("creator", model.getCreateUser());
        assertEquals("updater", model.getUpdateUser());
    }

    @Test
    void testUpdateEntity_UpdatesExistingEntityFields() {
        // Arrange
        EvaluationCriteriaSelectedModel model = new EvaluationCriteriaSelectedModel();
        UUID criteriaGuid = UUID.randomUUID();
        UUID sectionSummaryGuid = UUID.randomUUID();

        model.setEvaluationCriteriaGuid(criteriaGuid.toString());
        model.setEvaluationCriteriaSectionSummaryGuid(sectionSummaryGuid.toString());
        model.setIsEvaluationCriteriaSelectedInd(true);
        model.setRevisionCount(3);
        model.setUpdateUser("updater");
        model.setUpdateDate(new Date());

        EvaluationCriteriaSelectedEntity existingEntity = new EvaluationCriteriaSelectedEntity();
        existingEntity.setEvaluationCriteriaSelectedGuid(UUID.randomUUID());
        existingEntity.setCreateUser("creator");
        existingEntity.setCreateDate(new Date());

        // Act
        EvaluationCriteriaSelectedEntity updated = assembler.updateEntity(model, existingEntity);

        // Assert
        assertEquals(criteriaGuid, updated.getEvaluationCriteriaGuid());
        assertEquals(sectionSummaryGuid, updated.getEvaluationCriteriaSectionSummaryGuid());
        assertTrue(updated.getIsEvaluationCriteriaSelectedInd());
        assertEquals(3, updated.getRevisionCount());
        assertEquals("creator", updated.getCreateUser()); // should remain unchanged
        assertEquals("updater", updated.getUpdateUser());
    }
}
