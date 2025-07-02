package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSectionCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class EvaluationCriteriaSectionCodeResourceAssemblerTest {

    private EvaluationCriteriaSectionCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new EvaluationCriteriaSectionCodeResourceAssembler();
    }

    @Test
    void testToEntity_MapsModelToEntity() {
        // Arrange
        EvaluationCriteriaSectionCodeModel model = new EvaluationCriteriaSectionCodeModel();
        model.setEvaluationCriteriaSectionCode("FINE_FLT");
        model.setDescription("Fine Filter");
        model.setDisplayOrder(1);
        model.setEffectiveDate(new Date());
        model.setExpiryDate(new Date());
        model.setRevisionCount(1);
        model.setCreateUser("creator");
        model.setCreateDate(new Date());
        model.setUpdateUser("updater");
        model.setUpdateDate(new Date());

        // Act
        EvaluationCriteriaSectionCodeEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals("FINE_FLT", entity.getEvaluationCriteriaSectionCode());
        assertEquals("Fine Filter", entity.getDescription());
        assertEquals(1, entity.getDisplayOrder());
        assertNotNull(entity.getEffectiveDate());
        assertNotNull(entity.getExpiryDate());
        assertEquals(1, entity.getRevisionCount());
        assertEquals("creator", entity.getCreateUser());
        assertEquals("updater", entity.getUpdateUser());
    }

    @Test
    void testToModel_MapsEntityToModel() {
        // Arrange
        EvaluationCriteriaSectionCodeEntity entity = new EvaluationCriteriaSectionCodeEntity();
        entity.setEvaluationCriteriaSectionCode("COARSE_FLT");
        entity.setDescription("Coarse Filter");
        entity.setDisplayOrder(2);
        entity.setEffectiveDate(new Date());
        entity.setExpiryDate(new Date());
        entity.setRevisionCount(2);
        entity.setCreateUser("creator2");
        entity.setCreateDate(new Date());
        entity.setUpdateUser("updater2");
        entity.setUpdateDate(new Date());

        // Act
        EvaluationCriteriaSectionCodeModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals("COARSE_FLT", model.getEvaluationCriteriaSectionCode());
        assertEquals("Coarse Filter", model.getDescription());
        assertEquals(2, model.getDisplayOrder());
        assertNotNull(model.getEffectiveDate());
        assertNotNull(model.getExpiryDate());
        assertEquals(2, model.getRevisionCount());
        assertEquals("creator2", model.getCreateUser());
        assertEquals("updater2", model.getUpdateUser());
    }

    @Test
    void testToEntity_NullModelReturnsNull() {
        assertNull(assembler.toEntity(null));
    }
}
