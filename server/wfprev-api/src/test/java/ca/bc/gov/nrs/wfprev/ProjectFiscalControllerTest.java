package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.controllers.ProjectFiscalController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.services.ProjectFiscalService;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import com.nimbusds.jose.shaded.gson.JsonDeserializer;
import com.nimbusds.jose.shaded.gson.JsonPrimitive;
import com.nimbusds.jose.shaded.gson.JsonSerializer;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectFiscalController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class ProjectFiscalControllerTest {

    @MockBean
    private ProjectFiscalService projectFiscalService;

    @Autowired
    private MockMvc mockMvc;

    private Gson gson;

    @MockBean(name = "springSecurityAuditorAware")
    private AuditorAware<String> auditorAware;

    @BeforeEach
    void setup() {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls()
                .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getTime())) // Serialize Date as UNIX time
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsLong())) // Deserialize UNIX time to Date
                .serializeSpecialFloatingPointValues();
        gson = builder.create();
    }

    @Test
    @WithMockUser
    void testGetAllProjectFiscals_Empty() throws Exception {

        List<ProjectFiscalModel> projectFiscalList = Collections.emptyList();
        CollectionModel<ProjectFiscalModel> projectFiscalModel = CollectionModel.of(projectFiscalList);

        when(projectFiscalService.getAllProjectFiscals()).thenReturn(projectFiscalModel);

        ResultActions result = mockMvc.perform(get("/projectFiscals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals("{}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    @WithMockUser
    void testGetAllProjectFiscals_Success() throws Exception {
        ProjectFiscalModel projectFiscalModel1 = ProjectFiscalModel.builder()
                .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174000")
                .projectGuid("123e4567-e89b-12d3-a456-426614174001")
                .activityCategoryCode("Tactical Planning")
                .fiscalYear(2024L)
                .projectFiscalName("Project Fiscal Name 1")
                .fiscalAllocatedAmount(new BigDecimal("10000.00"))
                .isApprovedInd(true)
                .build();

        ProjectFiscalModel projectFiscalModel2 = ProjectFiscalModel.builder()
                .projectPlanFiscalGuid("223e4567-e89b-12d3-a456-426614174002")
                .projectGuid("223e4567-e89b-12d3-a456-426614174003")
                .activityCategoryCode("Maintenance - Survey")
                .fiscalYear(2025L)
                .projectFiscalName("Project Fiscal Name 2")
                .fiscalAllocatedAmount(new BigDecimal("20000.00"))
                .isApprovedInd(false)
                .build();

        List<ProjectFiscalModel> projectFiscalList = List.of(projectFiscalModel1, projectFiscalModel2);
        CollectionModel<ProjectFiscalModel> projectFiscalModelList = CollectionModel.of(projectFiscalList);

        when(projectFiscalService.getAllProjectFiscals()).thenReturn(projectFiscalModelList);

        ResultActions result = mockMvc.perform(get("/projectFiscals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String expectedJson = "{\"_embedded\":{\"projectFiscals\":["
                + "{\"projectPlanFiscalGuid\":\"123e4567-e89b-12d3-a456-426614174000\",\"projectGuid\":\"123e4567-e89b-12d3-a456-426614174001\",\"activityCategoryCode\":\"Tactical Planning\",\"fiscalYear\":2024,\"projectFiscalName\":\"Project Fiscal Name 1\",\"fiscalAllocatedAmount\":10000.00,\"isApprovedInd\":true},"
                + "{\"projectPlanFiscalGuid\":\"223e4567-e89b-12d3-a456-426614174002\",\"projectGuid\":\"223e4567-e89b-12d3-a456-426614174003\",\"activityCategoryCode\":\"Maintenance - Survey\",\"fiscalYear\":2025,\"projectFiscalName\":\"Project Fiscal Name 2\",\"fiscalAllocatedAmount\":20000.00,\"isApprovedInd\":false}]}}";

        assertEquals(expectedJson, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    @WithMockUser
    void testCreateProjectFiscal_Success() throws Exception {
        ProjectFiscalModel inputModel = ProjectFiscalModel.builder()
                .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174000")
                .projectGuid("123e4567-e89b-12d3-a456-426614174001")
                .activityCategoryCode("Tactical Planning")
                .fiscalYear(2024L)
                .submissionTimestamp(new Date(1672531200000L))
                .endorsementEvalTimestamp(new Date(1672531300000L))
                .endorsementTimestamp(new Date(1672531400000L))
                .approvedTimestamp(new Date(1672531500000L))
                .lastProgressUpdateTimestamp(new Date(1672531600000L))
                .isApprovedInd(true)
                .build();

        when(projectFiscalService.createProjectFiscal(inputModel)).thenReturn(inputModel);

        String inputJson = gson.toJson(inputModel);

        String expectedJson = "{\"projectPlanFiscalGuid\":\"123e4567-e89b-12d3-a456-426614174000\","
                + "\"projectGuid\":\"123e4567-e89b-12d3-a456-426614174001\","
                + "\"activityCategoryCode\":\"Tactical Planning\","
                + "\"fiscalYear\":2024,"
                + "\"submissionTimestamp\":1672531200000,"
                + "\"endorsementEvalTimestamp\":1672531300000,"
                + "\"endorsementTimestamp\":1672531400000,"
                + "\"isApprovedInd\":true,"
                + "\"approvedTimestamp\":1672531500000,"
                + "\"lastProgressUpdateTimestamp\":1672531600000}";

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/projectFiscals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inputJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(expectedJson, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    @WithMockUser
    void testUpdateProjectFiscal_Success() throws Exception {
        // GIVEN a project fiscal model
        ProjectFiscalModel inputModel = ProjectFiscalModel.builder()
                .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174000")
                .projectGuid("123e4567-e89b-12d3-a456-426614174001")
                .activityCategoryCode("Tactical Planning")
                .fiscalYear(2024L)
                .submissionTimestamp(new Date(1672531200000L))
                .endorsementEvalTimestamp(new Date(1672531300000L))
                .endorsementTimestamp(new Date(1672531400000L))
                .approvedTimestamp(new Date(1672531500000L))
                .lastProgressUpdateTimestamp(new Date(1672531600000L))
                .isApprovedInd(true)
                .build();

        String inputJson = gson.toJson(inputModel);

        // WHEN the project fiscal model is updated
        when(projectFiscalService.updateProjectFiscal(inputModel)).thenReturn(inputModel);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/projectFiscals/{id}", inputModel.getProjectPlanFiscalGuid())
                        .content(inputJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Build the expected JSON to match the actual response
        String expectedJson = "{"
                + "\"projectPlanFiscalGuid\":\"123e4567-e89b-12d3-a456-426614174000\","
                + "\"projectGuid\":\"123e4567-e89b-12d3-a456-426614174001\","
                + "\"activityCategoryCode\":\"Tactical Planning\","
                + "\"fiscalYear\":2024,"
                + "\"submissionTimestamp\":1672531200000,"
                + "\"endorsementEvalTimestamp\":1672531300000,"
                + "\"endorsementTimestamp\":1672531400000,"
                + "\"isApprovedInd\":true,"
                + "\"approvedTimestamp\":1672531500000,"
                + "\"lastProgressUpdateTimestamp\":1672531600000"
                + "}";

        // THEN assert the response matches the expected JSON
        assertEquals(expectedJson, result.andReturn().getResponse().getContentAsString());
    }
}