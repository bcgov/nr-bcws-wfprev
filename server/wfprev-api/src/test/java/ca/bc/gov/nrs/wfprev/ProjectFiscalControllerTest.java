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
}