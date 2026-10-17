package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSummaryModel;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The @WebMvcTest controller tests don't enable method security, so nothing else checks which
 * authority an endpoint asks for. These pin the ones that must line up with the WebADE roles:
 * WFPREV-CREATOR has every action except DELETE_PREVENTION_PROJECT.
 */
class ControllerAuthorityTest {

    private static String authorityOf(Class<?> controller, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = controller.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize, methodName + " has no @PreAuthorize");
        return preAuthorize.value();
    }

    @Test
    void deleteProjectAttachment_requiresDeleteSpatialUpload() throws NoSuchMethodException {
        assertEquals("hasAuthority('WFPREV.DELETE_SPATIAL_UPLOAD')",
                authorityOf(ProjectAttachmentController.class, "deleteFileAttachment", String.class, String.class, boolean.class));
    }

    @Test
    void createEvaluationCriteria_requiresCreateEvaluationCriteria() throws NoSuchMethodException {
        assertEquals("hasAuthority('WFPREV.CREATE_EVALUATION_CRITERIA')",
                authorityOf(EvaluationCriteriaSummaryController.class, "createEvaluationCriteriaSummary", EvaluationCriteriaSummaryModel.class));
    }

    @Test
    void updateEvaluationCriteria_requiresUpdateEvaluationCriteria() throws NoSuchMethodException {
        assertEquals("hasAuthority('WFPREV.UPDATE_EVALUATION_CRITERIA')",
                authorityOf(EvaluationCriteriaSummaryController.class, "updateEvaluationCriteriaSummary", EvaluationCriteriaSummaryModel.class, String.class));
    }

    @Test
    void deleteEvaluationCriteria_requiresDeleteEvaluationCriteria() throws NoSuchMethodException {
        assertEquals("hasAuthority('WFPREV.DELETE_EVALUATION_CRITERIA')",
                authorityOf(EvaluationCriteriaSummaryController.class, "deleteEvaluationCriteriaSummary", String.class));
    }
}
