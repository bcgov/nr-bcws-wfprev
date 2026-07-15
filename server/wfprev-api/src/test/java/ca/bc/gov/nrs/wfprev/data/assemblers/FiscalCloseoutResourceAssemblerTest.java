package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.FiscalCloseoutEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.models.FiscalCloseoutResponse;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class FiscalCloseoutResourceAssemblerTest {

    FiscalCloseoutResourceAssembler assembler = new FiscalCloseoutResourceAssembler();

    @Test
    void testToModel_MapsEntityToModel() {
        UUID projectPlanFiscalGuid = UUID.randomUUID();
        String submittedByGuid = UUID.randomUUID().toString();
        Date submittedTimestamp = new Date();
        ProjectFiscalEntity projectFiscalEntity = new ProjectFiscalEntity();
        projectFiscalEntity.setProjectPlanFiscalGuid(projectPlanFiscalGuid);
        
        UUID projectPlanFiscalCloseoutGuid = UUID.randomUUID();
        FiscalCloseoutEntity entity = new FiscalCloseoutEntity();
        entity.setProjectPlanFiscalCloseoutGuid(projectPlanFiscalCloseoutGuid);
        entity.setProjectFiscal(projectFiscalEntity);
        entity.setOutcomeComment("Test comment");
        entity.setSubmittedByName("Doe, John");
        entity.setSubmittedByUserid("IDIR\\JDOE");
        entity.setSubmittedByGuid(submittedByGuid);
        entity.setSubmittedTimestamp(submittedTimestamp);

        FiscalCloseoutResponse model = assembler.toModel(entity);

        assertNotNull(model);
        assertEquals(projectPlanFiscalCloseoutGuid.toString(), model.getProjectPlanFiscalCloseoutGuid());
        assertEquals(projectPlanFiscalGuid.toString(), model.getProjectPlanFiscalGuid());
        assertEquals("Test comment", model.getOutcomeComment());
        assertEquals("Doe, John", model.getSubmittedByName());
        assertEquals("IDIR\\JDOE", model.getSubmittedByUserid());
        assertEquals(submittedByGuid, model.getSubmittedByGuid());
        assertEquals(submittedTimestamp, model.getSubmittedTimestamp());
    }

    @Test
    void testToEntity_MapsModelToEntity() {
        UUID projectPlanFiscalGuid = UUID.randomUUID();
        String submittedByGuid = UUID.randomUUID().toString();
        Date submittedTimestamp = new Date();
        FiscalCloseoutResponse model = new FiscalCloseoutResponse();
        model.setProjectPlanFiscalGuid(projectPlanFiscalGuid.toString());
        model.setOutcomeComment("Test comment");
        model.setSubmittedByName("Doe, John");
        model.setSubmittedByUserid("IDIR\\JDOE");
        model.setSubmittedByGuid(submittedByGuid);
        model.setSubmittedTimestamp(submittedTimestamp);

        ProjectFiscalEntity projectFiscalEntity = new ProjectFiscalEntity();
        projectFiscalEntity.setProjectPlanFiscalGuid(projectPlanFiscalGuid);

        FiscalCloseoutEntity entity = assembler.toEntity(model, projectFiscalEntity);

        assertNotNull(entity);
        assertEquals(projectPlanFiscalGuid, entity.getProjectFiscal().getProjectPlanFiscalGuid());
        assertEquals("Test comment", entity.getOutcomeComment());
        assertEquals("Doe, John", model.getSubmittedByName());
        assertEquals("IDIR\\JDOE", entity.getSubmittedByUserid());
        assertEquals(submittedByGuid, entity.getSubmittedByGuid());
        assertEquals(submittedTimestamp, entity.getSubmittedTimestamp());
    }
}
