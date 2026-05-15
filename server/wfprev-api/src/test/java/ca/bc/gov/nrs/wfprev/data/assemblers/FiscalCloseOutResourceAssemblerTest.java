package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.FiscalCloseOutEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.models.FiscalCloseOutModel;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class FiscalCloseOutResourceAssemblerTest {

    FiscalCloseOutResourceAssembler assembler = new FiscalCloseOutResourceAssembler();

    @Test
    void testToModel_MapsEntityToModel() {
        UUID projectPlanFiscalGuid = UUID.randomUUID();
        ProjectFiscalEntity projectFiscalEntity = new ProjectFiscalEntity();
        projectFiscalEntity.setProjectPlanFiscalGuid(projectPlanFiscalGuid);
        
        FiscalCloseOutEntity entity = new FiscalCloseOutEntity();
        entity.setProjectFiscal(projectFiscalEntity);
        entity.setOutcomeComment("Test comment");

        FiscalCloseOutModel model = assembler.toModel(entity);

        assertNotNull(model);
        assertEquals(projectPlanFiscalGuid.toString(), model.getProjectPlanFiscalGuid());
        assertEquals("Test comment", model.getOutcomeComment());
    }

    @Test
    void testToEntity_MapsModelToEntity() {
        UUID projectPlanFiscalGuid = UUID.randomUUID();
        FiscalCloseOutModel model = new FiscalCloseOutModel();
        model.setProjectPlanFiscalGuid(projectPlanFiscalGuid.toString());
        model.setOutcomeComment("Test comment");

        ProjectFiscalEntity projectFiscalEntity = new ProjectFiscalEntity();
        projectFiscalEntity.setProjectPlanFiscalGuid(projectPlanFiscalGuid);

        FiscalCloseOutEntity entity = assembler.toEntity(model, projectFiscalEntity);

        assertNotNull(entity);
        assertEquals(projectPlanFiscalGuid, entity.getProjectFiscal().getProjectPlanFiscalGuid());
        assertEquals("Test comment", entity.getOutcomeComment());
    }
}
