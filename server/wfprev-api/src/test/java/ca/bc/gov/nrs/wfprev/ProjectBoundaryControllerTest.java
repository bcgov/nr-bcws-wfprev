package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.controllers.ProjectBoundaryController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.services.ProjectBoundaryService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectBoundaryController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class ProjectBoundaryControllerTest {

    @MockBean
    private ProjectBoundaryService projectBoundaryService;

    @Autowired
    private MockMvc mockMvc;

    private Gson gson;

    @MockBean(name = "springSecurityAuditorAware")
    private AuditorAware<String> auditorAware;

    String projectBoundaryJson = """
                {
                    "projectBoundaryGuid": "%s",
                    "projectGuid": "%s",
                    "collectionDate": %d,
                    "boundarySizeHa": 20.5,
                    "systemStartTimestamp": %d,
                    "systemEndTimestamp": %d,
                    "locationGeometry": {
                        "coordinates": [[
                            [-123.3656, 48.4284],
                            [-123.3657, 48.4285],
                            [-123.3658, 48.4284],
                            [-123.3656, 48.4284]
                        ]]
                    },
                    "boundaryGeometry": {
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
    void testGetAllProjectBoundaries_Success() throws Exception {
        List<ProjectBoundaryModel> boundaries = List.of(
                ProjectBoundaryModel.builder().projectBoundaryGuid(UUID.randomUUID().toString()).build(),
                ProjectBoundaryModel.builder().projectBoundaryGuid(UUID.randomUUID().toString()).build()
        );

        when(projectBoundaryService.getAllProjectBoundaries(anyString()))
                .thenReturn(CollectionModel.of(boundaries));

        mockMvc.perform(get("/projects/{projectId}/projectBoundary", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetProjectBoundary_NotFound() throws Exception {
        UUID boundaryId = UUID.randomUUID();
        when(projectBoundaryService.getProjectBoundary(anyString(), anyString()))
                .thenThrow(new EntityNotFoundException("Boundary not found"));

        mockMvc.perform(get("/projects/{projectId}/projectBoundary/{id}", UUID.randomUUID(), boundaryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testDeleteProjectBoundary_Success() throws Exception {
        UUID boundaryId = UUID.randomUUID();
        doNothing().when(projectBoundaryService).deleteProjectBoundary(anyString(), anyString());

        mockMvc.perform(delete("/projects/{projectId}/projectBoundary/{id}", UUID.randomUUID(), boundaryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isNoContent());
    }

}
