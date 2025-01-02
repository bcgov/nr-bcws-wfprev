package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    void testUpdateProjectFiscal_Exception() throws Exception {
        ProjectFiscalModel inputModel = ProjectFiscalModel.builder()
                .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174000")
                .projectGuid("123e4567-e89b-12d3-a456-426614174001")
                .activityCategoryCode("Tactical Planning")
                .fiscalYear(2024L)
                .submissionTimestamp(new Date(1672531200000L))
                .build();

        String inputJson = gson.toJson(inputModel);

        when(projectFiscalService.updateProjectFiscal(inputModel))
                .thenThrow(new RuntimeException("Test exception"));

        mockMvc.perform(MockMvcRequestBuilders.put("/projectFiscals/{id}", inputModel.getProjectPlanFiscalGuid())
                        .content(inputJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testUpdateProjectFiscal_NotFound() throws Exception {
        ProjectFiscalModel inputModel = ProjectFiscalModel.builder()
                .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174000")
                .projectGuid("123e4567-e89b-12d3-a456-426614174001")
                .activityCategoryCode("Tactical Planning")
                .fiscalYear(2024L)
                .build();

        String inputJson = gson.toJson(inputModel);

        when(projectFiscalService.updateProjectFiscal(inputModel)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.put("/projectFiscals/{id}", inputModel.getProjectPlanFiscalGuid())
                        .content(inputJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testUpdateProjectFiscal_BadRequestUpdatedProjectFiscalGuid() throws Exception {
        ProjectFiscalModel inputModel = ProjectFiscalModel.builder()
                .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174000")
                .projectGuid("123e4567-e89b-12d3-a456-426614174001")
                .activityCategoryCode("Tactical Planning")
                .fiscalYear(2024L)
                .build();

        String inputJson = gson.toJson(inputModel);

        mockMvc.perform(MockMvcRequestBuilders.put("/projectFiscals/{id}", "123e4567-e89b-12d3-a456-426614174002")
                        .content(inputJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testUpdateProjectFiscal_Success() throws Exception {
        ProjectFiscalModel inputModel = ProjectFiscalModel.builder()
                .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174000")
                .projectGuid("123e4567-e89b-12d3-a456-426614174001")
                .activityCategoryCode("Tactical Planning")
                .fiscalYear(2024L)
                .build();

        String inputJson = gson.toJson(inputModel);

        when(projectFiscalService.updateProjectFiscal(inputModel)).thenReturn(inputModel);

        mockMvc.perform(MockMvcRequestBuilders.put("/projectFiscals/{id}", inputModel.getProjectPlanFiscalGuid())
                        .content(inputJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectPlanFiscalGuid").value(inputModel.getProjectPlanFiscalGuid()));
    }

    @Test
    @WithMockUser
    void testGetAllProjectFiscals_Success() throws Exception {
        // GIVEN a list of ProjectFiscalModel
        List<ProjectFiscalModel> projectFiscalModels = List.of(
                ProjectFiscalModel.builder()
                        .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174000")
                        .projectGuid("123e4567-e89b-12d3-a456-426614174001")
                        .activityCategoryCode("Tactical Planning")
                        .fiscalYear(2024L)
                        .submissionTimestamp(new Date(1672531200000L))
                        .build(),
                ProjectFiscalModel.builder()
                        .projectPlanFiscalGuid("223e4567-e89b-12d3-a456-426614174002")
                        .projectGuid("223e4567-e89b-12d3-a456-426614174003")
                        .activityCategoryCode("Strategic Planning")
                        .fiscalYear(2025L)
                        .submissionTimestamp(new Date(1672627600000L))
                        .build()
        );

        when(projectFiscalService.getAllProjectFiscals()).thenReturn(CollectionModel.of(projectFiscalModels));

        // WHEN we call the getAllProjectFiscals method
        mockMvc.perform(get("/projectFiscals")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN we expect a 200 OK response
                .andExpect(status().isOk())
                // AND validate the entire response structure for the first projectFiscalModel
                .andExpect(jsonPath("$._embedded.projectFiscals[0].projectPlanFiscalGuid")
                        .value("123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(jsonPath("$._embedded.projectFiscals[0].projectGuid")
                        .value("123e4567-e89b-12d3-a456-426614174001"))
                .andExpect(jsonPath("$._embedded.projectFiscals[0].activityCategoryCode")
                        .value("Tactical Planning"))
                .andExpect(jsonPath("$._embedded.projectFiscals[0].fiscalYear")
                        .value(2024))
                .andExpect(jsonPath("$._embedded.projectFiscals[0].submissionTimestamp")
                        .value(1672531200000L))
                // AND validate the entire response structure for the second projectFiscalModel
                .andExpect(jsonPath("$._embedded.projectFiscals[1].projectPlanFiscalGuid")
                        .value("223e4567-e89b-12d3-a456-426614174002"))
                .andExpect(jsonPath("$._embedded.projectFiscals[1].projectGuid")
                        .value("223e4567-e89b-12d3-a456-426614174003"))
                .andExpect(jsonPath("$._embedded.projectFiscals[1].activityCategoryCode")
                        .value("Strategic Planning"))
                .andExpect(jsonPath("$._embedded.projectFiscals[1].fiscalYear")
                        .value(2025))
                .andExpect(jsonPath("$._embedded.projectFiscals[1].submissionTimestamp")
                        .value(1672627600000L));
    }

    @Test
    @WithMockUser
    void testGetAllProjectFiscals_ServiceException() throws Exception {
        // GIVEN the service throws a ServiceException
        when(projectFiscalService.getAllProjectFiscals()).thenThrow(new ServiceException("Test ServiceException"));
        // WHEN we call the getAllProjectFiscals method
        mockMvc.perform(get("/projectFiscals")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN we expect a 500 Internal Server Error
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").doesNotExist()); // Ensure no body is returned
    }

    @Test
    @WithMockUser
    void testGetAllProjectFiscals_Empty() throws Exception {
        // GIVEN an empty list from the service
        when(projectFiscalService.getAllProjectFiscals()).thenReturn(CollectionModel.of(Collections.emptyList()));

        // WHEN we call the getAllProjectFiscals method
        mockMvc.perform(get("/projectFiscals")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN we expect a 200 OK response
                .andExpect(status().isOk())
                // AND the response body should have no projectFiscals
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    @WithMockUser
    void testGetAllProjectFiscals_UnexpectedException() throws Exception {
        // GIVEN the service throws a RuntimeException
        when(projectFiscalService.getAllProjectFiscals()).thenThrow(new RuntimeException("Unexpected error"));

        // WHEN we call the getAllProjectFiscals method
        mockMvc.perform(get("/projectFiscals")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN we expect a 500 Internal Server Error
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").doesNotExist()); // Ensure no body is returned
    }

    @Test
    @WithMockUser
    void testCreateProjectFiscal_Success() throws Exception {
        // GIVEN a ProjectFiscalModel
        ProjectFiscalModel inputModel = ProjectFiscalModel.builder()
                .projectPlanFiscalGuid("123e4567-e89b-12d3-a456-426614174000")
                .projectGuid("123e4567-e89b-12d3-a456-426614174001")
                .activityCategoryCode("Tactical Planning")
                .fiscalYear(2024L)
                .submissionTimestamp(new Date(1672531200000L))
                .build();

        // WHEN we call the createProjectFiscal method
        when(projectFiscalService.createProjectFiscal(inputModel)).thenReturn(inputModel);

        // THEN we expect a 201 Created response
        // AND the response body should match the input model
        mockMvc.perform(MockMvcRequestBuilders.post("/projectFiscals")
                        .content(gson.toJson(inputModel))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectPlanFiscalGuid").value(inputModel.getProjectPlanFiscalGuid()));
    }

    @Test
    @WithMockUser
    void testCreateProjectFiscal_ServiceException() throws Exception {
        // GIVEN I am creating a new ProjectFiscalModel
        // WHEN the service throws a ServiceException
        when(projectFiscalService.createProjectFiscal(new ProjectFiscalModel()))
                .thenThrow(new ServiceException("Test ServiceException"));
        // THEN I expect a 500 Internal Server Error
        // AND the response body should be empty
        mockMvc.perform(MockMvcRequestBuilders.post("/projectFiscals")
                        .content(gson.toJson(new ProjectFiscalModel()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").doesNotExist());
    }
}