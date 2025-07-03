package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.TestSpringSecurity;
import ca.bc.gov.nrs.wfprev.controllers.EvaluationCriteriaSummaryController;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSummaryModel;
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
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.Date;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvaluationCriteriaSummaryController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class EvaluationCriteriaSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EvaluationCriteriaSummaryService summaryService;

    @MockBean(name = "springSecurityAuditorAware")
    private AuditorAware<String> auditorAware;

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
    @WithMockUser
    void testGetAll_Success() throws Exception {
        when(summaryService.getAllEvaluationCriteriaSummaries(anyString()))
                .thenReturn(CollectionModel.empty());

        mockMvc.perform(get("/projects/{projectGuid}/evaluationCriteriaSummary", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testCreate_Success() throws Exception {
        EvaluationCriteriaSummaryModel model = new EvaluationCriteriaSummaryModel();
        model.setEvaluationCriteriaSummaryGuid(UUID.randomUUID().toString());

        when(summaryService.createEvaluationCriteriaSummary(any(EvaluationCriteriaSummaryModel.class)))
                .thenReturn(model);

        mockMvc.perform(post("/projects/{projectGuid}/evaluationCriteriaSummary", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(model)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.evaluationCriteriaSummaryGuid").value(model.getEvaluationCriteriaSummaryGuid()));
    }

    @Test
    @WithMockUser
    void testUpdate_MismatchId() throws Exception {
        EvaluationCriteriaSummaryModel model = new EvaluationCriteriaSummaryModel();
        model.setEvaluationCriteriaSummaryGuid(UUID.randomUUID().toString());

        mockMvc.perform(put("/projects/{projectGuid}/evaluationCriteriaSummary/{id}", UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(model)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testGet_Success() throws Exception {
        EvaluationCriteriaSummaryModel model = new EvaluationCriteriaSummaryModel();
        String id = UUID.randomUUID().toString();
        model.setEvaluationCriteriaSummaryGuid(id);

        when(summaryService.getEvaluationCriteriaSummary(eq(id))).thenReturn(model);

        mockMvc.perform(get("/projects/{projectGuid}/evaluationCriteriaSummary/{id}", UUID.randomUUID(), id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluationCriteriaSummaryGuid").value(id));
    }

    @Test
    @WithMockUser
    void testDelete_Success() throws Exception {
        String id = UUID.randomUUID().toString();
        doNothing().when(summaryService).deleteEvaluationCriteriaSummary(eq(id));

        mockMvc.perform(delete("/projects/{projectGuid}/evaluationCriteriaSummary/{id}", UUID.randomUUID(), id))
                .andExpect(status().isNoContent());
    }
}
