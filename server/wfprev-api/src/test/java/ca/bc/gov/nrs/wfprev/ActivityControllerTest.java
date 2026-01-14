package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.controllers.ActivityController;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.services.ActivityService;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import com.nimbusds.jose.shaded.gson.JsonDeserializer;
import com.nimbusds.jose.shaded.gson.JsonPrimitive;
import com.nimbusds.jose.shaded.gson.JsonSerializer;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class ActivityControllerTest {

    @MockBean
    private ActivityService activityService;

    @Autowired
    private MockMvc mockMvc;

    private Gson gson;

    @MockBean(name = "springSecurityAuditorAware")
    private AuditorAware<String> auditorAware;

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
    void testCreateActivity_Success() throws Exception {
        ActivityModel requestModel = buildActivityModel();

        when(activityService.createActivity(anyString(), anyString(), any(ActivityModel.class)))
                .thenReturn(requestModel);

        mockMvc.perform(post("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activityGuid").value(requestModel.getActivityGuid()));

        verify(activityService).createActivity(anyString(), anyString(), any(ActivityModel.class));
    }

    @Test
    @WithMockUser
    void testCreateActivity_DataIntegrityViolation() throws Exception {
        ActivityModel requestModel = buildActivityModel();

        when(activityService.createActivity(anyString(), anyString(), any(ActivityModel.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        mockMvc.perform(post("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testCreateActivity_ServiceException() throws Exception {
        ActivityModel requestModel = buildActivityModel();

        when(activityService.createActivity(anyString(), anyString(), any(ActivityModel.class)))
                .thenThrow(new ServiceException("Service exception"));

        mockMvc.perform(post("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testCreateActivity_Exception() throws Exception {
        ActivityModel requestModel = buildActivityModel();

        when(activityService.createActivity(anyString(), anyString(), any(ActivityModel.class)))
                .thenThrow(new RuntimeException("Test exception"));

        mockMvc.perform(post("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testCreateActivity_IllegalArgumentException() throws Exception {
        ActivityModel requestModel = buildActivityModel();

        when(activityService.createActivity(anyString(), anyString(), any(ActivityModel.class)))
                .thenThrow(new IllegalArgumentException("Illegal Argument exception"));

        mockMvc.perform(post("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testUpdateActivity_Success() throws Exception {
        ActivityModel requestModel = buildActivityModel();

        when(activityService.updateActivity(anyString(), anyString(), any(ActivityModel.class)))
                .thenReturn(requestModel);

        mockMvc.perform(put("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{id}",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        requestModel.getActivityGuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityGuid").value(requestModel.getActivityGuid()));

        verify(activityService).updateActivity(anyString(), anyString(), any(ActivityModel.class));
    }

    @Test
    @WithMockUser
    void testUpdateActivity_EntityNotFoundException() throws Exception {
        ActivityModel requestModel = buildActivityModel();

        when(activityService.updateActivity(anyString(), anyString(), any(ActivityModel.class)))
                .thenThrow(new EntityNotFoundException("Entity Not Found"));

        mockMvc.perform(put("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{id}",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        requestModel.getActivityGuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testUpdateActivity_Exception() throws Exception {
        ActivityModel requestModel = buildActivityModel();

        when(activityService.updateActivity(anyString(), anyString(), any(ActivityModel.class)))
                .thenThrow(new RuntimeException("Test exception"));

        mockMvc.perform(put("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{id}",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        requestModel.getActivityGuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testUpdateActivity_IllegalArgumentException() throws Exception {
        ActivityModel requestModel = buildActivityModel();

        when(activityService.updateActivity(anyString(), anyString(), any(ActivityModel.class)))
                .thenThrow(new IllegalArgumentException("Illegal Argument exception"));

        mockMvc.perform(put("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{id}",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        requestModel.getActivityGuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testUpdateActivity_NotFound() throws Exception {
        ActivityModel requestModel = buildActivityModel();

        when(activityService.updateActivity(anyString(), anyString(), any(ActivityModel.class)))
                .thenReturn(null);

        mockMvc.perform(put("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{id}",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        requestModel.getActivityGuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testUpdateActivity_IdMismatch() throws Exception {
        ActivityModel requestModel = buildActivityModel();

        mockMvc.perform(put("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{id}",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        "123e4567-e89b-12d3-a456-426614174000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(requestModel)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetAllActivities_Success() throws Exception {
        List<ActivityModel> activities = List.of(
                ActivityModel.builder()
                        .activityGuid("123e4567-e89b-12d3-a456-426614174000")
                        .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174001")
                        .activityName("Activity 1")
                        .activityDescription("Description 1")
                        .build(),
                ActivityModel.builder()
                        .activityGuid("223e4567-e89b-12d3-a456-426614174002")
                        .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174001")
                        .activityName("Activity 2")
                        .activityDescription("Description 2")
                        .build()
        );

        when(activityService.getAllActivities(anyString(), anyString()))
                .thenReturn(CollectionModel.of(activities));

        mockMvc.perform(get("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.activities[0].activityGuid")
                        .value("123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(jsonPath("$._embedded.activities[1].activityGuid")
                        .value("223e4567-e89b-12d3-a456-426614174002"));
    }

    @Test
    @WithMockUser
    void testGetAllActivities_Exception() throws Exception {
        when(activityService.getAllActivities(anyString(), anyString()))
                .thenThrow(new RuntimeException("Test exception"));

        mockMvc.perform(get("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetActivity_Success() throws Exception {
        ActivityModel model = ActivityModel.builder()
                .activityGuid("123e4567-e89b-12d3-a456-426614174000")
                .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174001")
                .activityName("Test Activity")
                .activityDescription("Test Description")
                .build();

        when(activityService.getActivity(anyString(), anyString(), anyString()))
                .thenReturn(model);

        mockMvc.perform(get("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{id}",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        model.getActivityGuid())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityGuid").value(model.getActivityGuid()));
    }

    @Test
    @WithMockUser
    void testGetActivity_Exception() throws Exception {
        ActivityModel model = ActivityModel.builder()
                .activityGuid("123e4567-e89b-12d3-a456-426614174000")
                .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174001")
                .activityName("Test Activity")
                .activityDescription("Test Description")
                .build();

        when(activityService.getActivity(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Test exception"));

        mockMvc.perform(get("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{id}",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        model.getActivityGuid())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetActivity_NotFound() throws Exception {
        when(activityService.getActivity(anyString(), anyString(), anyString()))
                .thenReturn(null);

        mockMvc.perform(get("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{id}",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        "123e4567-e89b-12d3-a456-426614174000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteActivity_Success() throws Exception {
        String activityId = "123e4567-e89b-12d3-a456-426614174000";
        doNothing().when(activityService).deleteActivity(anyString(), anyString(), anyString(), anyBoolean());

        mockMvc.perform(delete("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{id}",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        activityId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(activityService).deleteActivity(anyString(), anyString(), eq(activityId), eq(false));
    }

    @Test
    @WithMockUser
    void testDeleteActivity_NotFound() throws Exception {
        String activityId = "123e4567-e89b-12d3-a456-426614174000";
        doThrow(new EntityNotFoundException("Not found"))
                .when(activityService).deleteActivity(anyString(), anyString(), anyString(), anyBoolean());

        mockMvc.perform(delete("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{id}",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        activityId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteActivity_Exception() throws Exception {
        String activityId = "123e4567-e89b-12d3-a456-426614174000";
        doThrow(new RuntimeException("Test exception"))
                .when(activityService).deleteActivity(anyString(), anyString(), anyString(), anyBoolean());

        mockMvc.perform(delete("/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{id}",
                        "123e4567-e89b-12d3-a456-426614174001",
                        "123e4567-e89b-12d3-a456-426614174002",
                        activityId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    ActivityModel buildActivityModel(){
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400000);
        return ActivityModel.builder()
                .activityGuid("123e4567-e89b-12d3-a456-426614174000")
                .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174001")
                .activityName("New Activity")
                .activityDescription("New Description")
                .activityStartDate(startDate)
                .activityEndDate(endDate)
                .isResultsReportableInd(true)
                .outstandingObligationsInd(false)
                .isSpatialAddedInd(true)
                .build();
    }
}