package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.controllers.ActivityBoundaryController;
import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import ca.bc.gov.nrs.wfprev.services.ActivityBoundaryService;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import com.nimbusds.jose.shaded.gson.JsonDeserializer;
import com.nimbusds.jose.shaded.gson.JsonPrimitive;
import com.nimbusds.jose.shaded.gson.JsonSerializer;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityBoundaryController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class ActivityBoundaryControllerTest {

    @MockBean
    private ActivityBoundaryService activityBoundaryService;

    @Autowired
    private MockMvc mockMvc;

    private Gson gson;

    @MockBean(name = "springSecurityAuditorAware")
    private AuditorAware<String> auditorAware;

    String activityBoundaryJson = """
                {
                    "activityBoundaryGuid": "%s",
                    "activityGuid": "%s",
                    "systemStartTimestamp": %d,
                    "systemEndTimestamp": %d,
                    "collectionDate": %d,
                    "boundarySizeHa": 10.5,
                    "geometry": {
                        "coordinates": [[
                            [-123.3656, 48.4284],
                            [-123.3657, 48.4285],
                            [-123.3658, 48.4284],
                            [-123.3656, 48.4284]
                        ]]
                    }
                }
            """;

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
    @WithMockUser
    void testGetAllActivityBoundaries_Success() throws Exception {
        List<ActivityBoundaryModel> boundaries = List.of(
                ActivityBoundaryModel.builder().activityBoundaryGuid(UUID.randomUUID().toString()).build(),
                ActivityBoundaryModel.builder().activityBoundaryGuid(UUID.randomUUID().toString()).build()
        );

        when(activityBoundaryService.getAllActivityBoundaries(anyString(), anyString(), anyString()))
                .thenReturn(CollectionModel.of(boundaries));

        mockMvc.perform(get("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetAllActivityBoundaries_RuntimeException() throws Exception {
        when(activityBoundaryService.getAllActivityBoundaries(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetAllActivityBoundaries_Exception() throws Exception {
        when(activityBoundaryService.getAllActivityBoundaries(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Runtime exception"));

        mockMvc.perform(get("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetActivityBoundary_Success() throws Exception {
        UUID boundaryId = UUID.randomUUID();
        ActivityBoundaryModel model = ActivityBoundaryModel.builder().activityBoundaryGuid(boundaryId.toString()).build();

        when(activityBoundaryService.getActivityBoundary(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(model);

        mockMvc.perform(get("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityBoundaryGuid").value(model.getActivityBoundaryGuid()));
    }

    @Test
    @WithMockUser
    void testGetActivityBoundary_Exception() throws Exception {
        UUID boundaryId = UUID.randomUUID();

        when(activityBoundaryService.getActivityBoundary(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Runtime exception"));

        mockMvc.perform(get("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetActivityBoundary_NotFound() throws Exception {
        UUID boundaryId = UUID.randomUUID();

        when(activityBoundaryService.getActivityBoundary(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new EntityNotFoundException("Boundary not found"));

        mockMvc.perform(get("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetActivityBoundary_EntityNotFoundException() throws Exception {
        UUID boundaryId = UUID.randomUUID();

        when(activityBoundaryService.getActivityBoundary(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new EntityNotFoundException("Boundary not found"));

        mockMvc.perform(get("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testCreateActivityBoundary_Success() throws Exception {
        ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

        when(activityBoundaryService.createActivityBoundary(anyString(), anyString(), anyString(), any(ActivityBoundaryModel.class)))
                .thenReturn(requestModel);

        String requestJson = activityBoundaryJson.formatted(
                requestModel.getActivityBoundaryGuid(),
                requestModel.getActivityGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(post("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activityBoundaryGuid").value(requestModel.getActivityBoundaryGuid()));
    }

    @Test
    @WithMockUser
    void testCreateActivityBoundary_DataIntegrityViolationException() throws Exception {
        ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

        when(activityBoundaryService.createActivityBoundary(anyString(), anyString(), anyString(), any(ActivityBoundaryModel.class)))
                .thenThrow(new DataIntegrityViolationException("Data Integrity Violation"));

        String requestJson = activityBoundaryJson.formatted(
                requestModel.getActivityBoundaryGuid(),
                requestModel.getActivityGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(post("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testCreateActivityBoundary_Exception() throws Exception {
        ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

        when(activityBoundaryService.createActivityBoundary(anyString(), anyString(), anyString(), any(ActivityBoundaryModel.class)))
                .thenThrow(new RuntimeException("Runtime exception"));

        String requestJson = activityBoundaryJson.formatted(
                requestModel.getActivityBoundaryGuid(),
                requestModel.getActivityGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(post("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testCreateActivityBoundary_IllegalArgumentException() throws Exception {
        ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

        when(activityBoundaryService.createActivityBoundary(anyString(), anyString(), anyString(), any(ActivityBoundaryModel.class)))
                .thenThrow(new IllegalArgumentException("Illegal Argument exception"));

        String requestJson = activityBoundaryJson.formatted(
                requestModel.getActivityBoundaryGuid(),
                requestModel.getActivityGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(post("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testUpdateActivityBoundary_Success() throws Exception {
        ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

        when(activityBoundaryService.updateActivityBoundary(anyString(), anyString(), anyString(), any(ActivityBoundaryModel.class)))
                .thenReturn(requestModel);

        String requestJson = activityBoundaryJson.formatted(
                requestModel.getActivityBoundaryGuid(),
                requestModel.getActivityGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(put("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), requestModel.getActivityBoundaryGuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityBoundaryGuid").value(requestModel.getActivityBoundaryGuid()));
    }

    @Test
    @WithMockUser
    void testUpdateActivityBoundary_EntityNotFoundException() throws Exception {
        UUID boundaryId = UUID.randomUUID();
        ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

        when(activityBoundaryService.updateActivityBoundary(anyString(), anyString(), anyString(), any(ActivityBoundaryModel.class)))
                .thenThrow(new EntityNotFoundException("Boundary not found"));

        String requestJson = activityBoundaryJson.formatted(
                requestModel.getActivityBoundaryGuid(),
                requestModel.getActivityGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(put("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testUpdateActivityBoundary_Exception() throws Exception {
        ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

        when(activityBoundaryService.updateActivityBoundary(anyString(), anyString(), anyString(), any(ActivityBoundaryModel.class)))
                .thenThrow(new RuntimeException("Runtime exception"));

        String requestJson = activityBoundaryJson.formatted(
                requestModel.getActivityBoundaryGuid(),
                requestModel.getActivityGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(put("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), requestModel.getActivityBoundaryGuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testUpdateActivityBoundary_IllegalArgumentException() throws Exception {
        ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

        when(activityBoundaryService.updateActivityBoundary(anyString(), anyString(), anyString(), any(ActivityBoundaryModel.class)))
                .thenThrow(new IllegalArgumentException("Runtime exception"));

        String requestJson = activityBoundaryJson.formatted(
                requestModel.getActivityBoundaryGuid(),
                requestModel.getActivityGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(put("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), requestModel.getActivityBoundaryGuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testUpdateActivityBoundary_DataIntegrityViolationException() throws Exception {
        ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

        when(activityBoundaryService.updateActivityBoundary(anyString(), anyString(), anyString(), any(ActivityBoundaryModel.class)))
                .thenThrow(new DataIntegrityViolationException("Data Integrity Violation"));

        String requestJson = activityBoundaryJson.formatted(
                requestModel.getActivityBoundaryGuid(),
                requestModel.getActivityGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(put("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), requestModel.getActivityBoundaryGuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testDeleteActivityBoundary_Success() throws Exception {
        UUID boundaryId = UUID.randomUUID();
        doNothing().when(activityBoundaryService).deleteActivityBoundary(anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(delete("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isNoContent());

        verify(activityBoundaryService).deleteActivityBoundary(anyString(), anyString(), anyString(), eq(boundaryId.toString()));
    }

    ActivityBoundaryModel buildActivityBoundaryRequestModel() {
        return ActivityBoundaryModel.builder()
                .activityBoundaryGuid(UUID.randomUUID().toString())
                .activityGuid(UUID.randomUUID().toString())
                .systemStartTimestamp(new Date())
                .systemEndTimestamp(new Date())
                .collectionDate(new Date())
                .boundarySizeHa(new BigDecimal("10.5"))
                .geometry(new PGpolygon(new PGpoint[]{
                        new PGpoint(-123.3656, 48.4284),
                        new PGpoint(-123.3657, 48.4285),
                        new PGpoint(-123.3658, 48.4284),
                        new PGpoint(-123.3656, 48.4284)
                }))
                .build();
    }

}
