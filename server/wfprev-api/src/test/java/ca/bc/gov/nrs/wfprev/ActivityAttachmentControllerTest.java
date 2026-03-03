package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.controllers.ActivityAttachmentController;
import ca.bc.gov.nrs.wfprev.data.models.AttachmentContentTypeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.FileAttachmentModel;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.data.models.SourceObjectNameCodeModel;
import ca.bc.gov.nrs.wfprev.services.FileAttachmentService;
import ca.bc.gov.nrs.wfprev.services.ActivityService;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import com.nimbusds.jose.shaded.gson.JsonDeserializer;
import com.nimbusds.jose.shaded.gson.JsonPrimitive;
import com.nimbusds.jose.shaded.gson.JsonSerializer;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityAttachmentController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class, MockMvcRestExceptionConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class ActivityAttachmentControllerTest {

    @MockBean
    private FileAttachmentService fileAttachmentService;

    @MockBean
    private ActivityService activityService;

    @MockBean(name = "springSecurityAuditorAware")
    private AuditorAware<String> auditorAware;

    @Spy
    @InjectMocks
    private ActivityAttachmentController activityAttachmentController;

    @Autowired
    private MockMvc mockMvc;

    private Gson gson;

    @BeforeEach
    void setup() {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls()
                .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getTime()))
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsLong()))
                .serializeSpecialFloatingPointValues();
        gson = builder.create();
    }

    @Test
    void testCreateFileAttachment_Success() throws Exception {
        FileAttachmentModel requestModel = buildFileAttachmentModel();

        when(fileAttachmentService.createFileAttachment(any(FileAttachmentModel.class)))
                .thenReturn(requestModel);
        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());

        mockMvc.perform(post("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileAttachmentGuid").value(requestModel.getFileAttachmentGuid()));

        verify(fileAttachmentService).createFileAttachment(any(FileAttachmentModel.class));
    }

    @Test
    void testCreateFileAttachment_DataIntegrityViolation() throws Exception {
        FileAttachmentModel requestModel = buildFileAttachmentModel();

        when(fileAttachmentService.createFileAttachment(any(FileAttachmentModel.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));
        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());

        mockMvc.perform(post("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateFileAttachment_IllegalArgumentException() throws Exception {
        FileAttachmentModel requestModel = buildFileAttachmentModel();

        when(fileAttachmentService.createFileAttachment(any(FileAttachmentModel.class)))
                .thenThrow(new IllegalArgumentException("Illegal argument"));
        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());

        mockMvc.perform(post("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateFileAttachment_RuntimeException() throws Exception {
        FileAttachmentModel requestModel = buildFileAttachmentModel();

        when(fileAttachmentService.createFileAttachment(any(FileAttachmentModel.class)))
                .thenThrow(new RuntimeException("Runtime exception"));
        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());

        mockMvc.perform(post("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetFileAttachment_Success() throws Exception {
        FileAttachmentModel model = buildFileAttachmentModel();

        when(fileAttachmentService.getFileAttachmentById(anyString()))
                .thenReturn(model);
        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());

        mockMvc.perform(get("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments/{id}", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003", model.getFileAttachmentGuid())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileAttachmentGuid").value(model.getFileAttachmentGuid()));
    }

    @Test
    void testGetFileAttachment_NotFound() throws Exception {
        when(fileAttachmentService.getFileAttachmentById(anyString()))
                .thenReturn(null);

        mockMvc.perform(get("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments/{id}", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003", "non-existing-guid")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllFileAttachments_Success() throws Exception {
        List<FileAttachmentModel> mockAttachments = List.of(buildFileAttachmentModel());

        when(activityService.getActivity(anyString(), anyString(), anyString()))
                .thenReturn(new ActivityModel());
        when(fileAttachmentService.getAllActivityAttachments(anyString()))
                .thenReturn(CollectionModel.of(mockAttachments));

        mockMvc.perform(get("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        "123e4567-e89b-12d3-a456-426614174003")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllFileAttachments_InvalidActivity() throws Exception {
        when(activityService.getActivity(anyString(), anyString(), anyString()))
                .thenReturn(null);

        mockMvc.perform(get("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        "invalid-guid")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllFileAttachments_RuntimeException() throws Exception {
        when(activityService.getActivity(anyString(), anyString(), anyString()))
                .thenReturn(new ActivityModel());
        when(fileAttachmentService.getAllActivityAttachments(anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        "123e4567-e89b-12d3-a456-426614174003")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void testDeleteFileAttachment_Success() throws Exception {
        String attachmentId = "123e4567-e89b-12d3-a456-426614174000";
        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());

        mockMvc.perform(delete("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments/{id}", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003", attachmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(fileAttachmentService).deleteFileAttachment(anyString(), eq(false));
    }

    @Test
    void testDeleteFileAttachment_Success_WithFlag() throws Exception {
        String attachmentId = "123e4567-e89b-12d3-a456-426614174000";
        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());

        mockMvc.perform(delete("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments/{id}", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003", attachmentId)
                        .param("deleteFileFromWfdm", "true")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(fileAttachmentService).deleteFileAttachment(anyString(), eq(true));
    }

    @Test
    void testDeleteFileAttachment_EntityNotFound() throws Exception {
        String attachmentId = "123e4567-e89b-12d3-a456-426614174000";

        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());
        doThrow(new EntityNotFoundException("Attachment not found"))
                .when(fileAttachmentService).deleteFileAttachment(anyString(), any(Boolean.class));

        mockMvc.perform(delete("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments/{id}", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003", attachmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404
    }

    @Test
    void testDeleteFileAttachment_Exception() throws Exception {
        String attachmentId = "123e4567-e89b-12d3-a456-426614174000";

        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());
        doThrow(new RuntimeException("Unexpected error"))
                .when(fileAttachmentService).deleteFileAttachment(anyString(), any(Boolean.class));

        mockMvc.perform(delete("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments/{id}", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003", attachmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()); // 500
    }


    @Test
    void testUpdateFileAttachment_Success() throws Exception {
        FileAttachmentModel requestModel = buildFileAttachmentModel();

        when(fileAttachmentService.updateFileAttachment(any(FileAttachmentModel.class)))
                .thenReturn(requestModel);
        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());

        mockMvc.perform(put("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments/{id}", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003", requestModel.getFileAttachmentGuid())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileAttachmentGuid").value(requestModel.getFileAttachmentGuid()));

        verify(fileAttachmentService).updateFileAttachment(any(FileAttachmentModel.class));
    }

    @Test
    void testUpdateFileAttachment_NotFound() throws Exception {
        FileAttachmentModel requestModel = buildFileAttachmentModel();

        when(fileAttachmentService.updateFileAttachment(any(FileAttachmentModel.class)))
                .thenReturn(null);

        mockMvc.perform(put("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments/{id}", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003", requestModel.getFileAttachmentGuid())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateFileAttachment_DataIntegrityViolation() throws Exception {
        FileAttachmentModel requestModel = buildFileAttachmentModel();

        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());
        when(fileAttachmentService.updateFileAttachment(any(FileAttachmentModel.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));

        mockMvc.perform(put("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments/{id}", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003", requestModel.getFileAttachmentGuid())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateFileAttachment_EntityNotFound() throws Exception {
        FileAttachmentModel requestModel = buildFileAttachmentModel();

        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());
        when(fileAttachmentService.updateFileAttachment(any(FileAttachmentModel.class)))
                .thenThrow(new EntityNotFoundException("Attachment not found"));

        mockMvc.perform(put("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments/{id}", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003", requestModel.getFileAttachmentGuid())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateFileAttachment_IllegalArgumentException() throws Exception {
        FileAttachmentModel requestModel = buildFileAttachmentModel();

        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());
        when(fileAttachmentService.updateFileAttachment(any(FileAttachmentModel.class)))
                .thenThrow(new IllegalArgumentException("Invalid input"));

        mockMvc.perform(put("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments/{id}", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003", requestModel.getFileAttachmentGuid())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateFileAttachment_RuntimeException() throws Exception {
        FileAttachmentModel requestModel = buildFileAttachmentModel();

        when(activityService.getActivity(anyString(), anyString(), anyString())).thenReturn(new ActivityModel());
        when(fileAttachmentService.updateFileAttachment(any(FileAttachmentModel.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments/{id}", "123e4567-e89b-12d3-a456-426614174001", "123e4567-e89b-12d3-a456-426614174002", "123e4567-e89b-12d3-a456-426614174003", requestModel.getFileAttachmentGuid())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isInternalServerError());
    }


    private FileAttachmentModel buildFileAttachmentModel() {
        return FileAttachmentModel.builder()
                .fileAttachmentGuid("123e4567-e89b-12d3-a456-426614174000")
                .sourceObjectNameCode(new SourceObjectNameCodeModel())
                .sourceObjectUniqueId("source-unique-id-001")
                .documentPath("/documents/attachment.pdf")
                .fileIdentifier("file-identifier-001")
                .wildfireYear(2024)
                .attachmentContentTypeCode(new AttachmentContentTypeCodeModel())
                .attachmentDescription("Sample file attachment")
                .attachmentReadOnlyInd(false)
                .uploadedByUserType("USER")
                .uploadedByUserId("user123")
                .uploadedByUserGuid("user-guid-123")
                .uploadedByTimestamp(new Date())
                .build();
    }

}
