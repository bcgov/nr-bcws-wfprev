package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.controllers.ProjectBoundaryController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.services.CoordinatesService;
import ca.bc.gov.nrs.wfprev.services.ProjectBoundaryService;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import com.nimbusds.jose.shaded.gson.JsonDeserializer;
import com.nimbusds.jose.shaded.gson.JsonPrimitive;
import com.nimbusds.jose.shaded.gson.JsonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
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

@WebMvcTest(ProjectBoundaryController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class ProjectBoundaryControllerTest {

    @MockBean
    private ProjectBoundaryService projectBoundaryService;

    @MockBean
    private CoordinatesService coordinatesService;

    @Autowired
    private MockMvc mockMvc;

    private Gson gson;

    @MockBean(name = "springSecurityAuditorAware")
    private AuditorAware<String> auditorAware;

    String projectBoundaryJson = """
                {
                    "projectBoundaryGuid": "%s",
                    "projectGuid": "%s",
                    "systemStartTimestamp": %d,
                    "systemEndTimestamp": %d,
                    "collectionDate": %d,
                    "boundarySizeHa": 10.5,
                    "locationGeometry": [-123.3656, 48.4284],
                    "boundaryGeometry": {
                        "type": "MultiPolygon",
                        "coordinates": [[[
                            [-123.3656, 48.4284],
                            [-123.3657, 48.4285],
                            [-123.3658, 48.4284],
                            [-123.3656, 48.4284]
                        ]]]
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

        mockMvc.perform(get("/projects/{projectId}/projectBoundary",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetAllProjectBoundaries_RuntimeException() throws Exception {
        when(projectBoundaryService.getAllProjectBoundaries(anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/projects/{projectId}/projectBoundary",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetAllProjectBoundaries_Exception() throws Exception {
        when(projectBoundaryService.getAllProjectBoundaries(anyString()))
                .thenThrow(new RuntimeException("Runtime exception"));

        mockMvc.perform(get("/projects/{projectId}/projectBoundary",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetProjectBoundary_Success() throws Exception {
        UUID boundaryId = UUID.randomUUID();
        ProjectBoundaryModel model = ProjectBoundaryModel.builder().projectBoundaryGuid(boundaryId.toString()).build();

        when(projectBoundaryService.getProjectBoundary(anyString(), anyString()))
                .thenReturn(model);

        mockMvc.perform(get("/projects/{projectId}/projectBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectBoundaryGuid").value(model.getProjectBoundaryGuid()));
    }

    @Test
    @WithMockUser
    void testGetProjectBoundary_Exception() throws Exception {
        UUID boundaryId = UUID.randomUUID();

        when(projectBoundaryService.getProjectBoundary(anyString(), anyString()))
                .thenThrow(new RuntimeException("Runtime exception"));

        mockMvc.perform(get("/projects/{projectId}/projectBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testCreateProjectBoundary_DataIntegrityViolationException() throws Exception {
        ProjectBoundaryModel requestModel = buildProjectBoundaryRequestModel();

        when(projectBoundaryService.createProjectBoundary(anyString(), any(ProjectBoundaryModel.class)))
                .thenThrow(new DataIntegrityViolationException("Data Integrity Violation"));

        String requestJson = projectBoundaryJson.formatted(
                requestModel.getProjectBoundaryGuid(),
                requestModel.getProjectGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(post("/projects/{projectId}/projectBoundary",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testCreateProjectBoundary_IllegalArgumentException() throws Exception {
        ProjectBoundaryModel requestModel = buildProjectBoundaryRequestModel();

        when(projectBoundaryService.createProjectBoundary(anyString(), any(ProjectBoundaryModel.class)))
                .thenThrow(new IllegalArgumentException("Illegal Argument exception"));

        String requestJson = projectBoundaryJson.formatted(
                requestModel.getProjectBoundaryGuid(),
                requestModel.getProjectGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(post("/projects/{projectId}/projectBoundary",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testUpdateProjectBoundary_Success() throws Exception {
        ProjectBoundaryModel requestModel = buildProjectBoundaryRequestModel();

        when(projectBoundaryService.updateProjectBoundary(anyString(), any(ProjectBoundaryModel.class)))
                .thenReturn(requestModel);

        String requestJson = projectBoundaryJson.formatted(
                requestModel.getProjectBoundaryGuid(),
                requestModel.getProjectGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(put("/projects/{projectId}/projectBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), requestModel.getProjectBoundaryGuid())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectBoundaryGuid").value(requestModel.getProjectBoundaryGuid()));
    }

    @Test
    @WithMockUser
    void testUpdateProjectBoundary_IllegalArgumentException() throws Exception {
        ProjectBoundaryModel requestModel = buildProjectBoundaryRequestModel();

        when(projectBoundaryService.updateProjectBoundary(anyString(), any(ProjectBoundaryModel.class)))
                .thenThrow(new IllegalArgumentException("Runtime exception"));

        String requestJson = projectBoundaryJson.formatted(
                requestModel.getProjectBoundaryGuid(),
                requestModel.getProjectGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(put("/projects/{projectId}/projectBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), requestModel.getProjectBoundaryGuid())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testUpdateProjectBoundary_DataIntegrityViolationException() throws Exception {
        ProjectBoundaryModel requestModel = buildProjectBoundaryRequestModel();

        when(projectBoundaryService.updateProjectBoundary(anyString(), any(ProjectBoundaryModel.class)))
                .thenThrow(new DataIntegrityViolationException("Data Integrity Violation"));

        String requestJson = projectBoundaryJson.formatted(
                requestModel.getProjectBoundaryGuid(),
                requestModel.getProjectGuid(),
                requestModel.getSystemStartTimestamp().getTime(),
                requestModel.getSystemEndTimestamp().getTime() + 1000,
                requestModel.getCollectionDate().getTime()
        );

        mockMvc.perform(put("/projects/{projectId}/projectBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), requestModel.getProjectBoundaryGuid())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testDeleteProjectBoundary_Success() throws Exception {

        UUID boundaryId = UUID.randomUUID();
        doNothing().when(projectBoundaryService).deleteProjectBoundary(anyString(), anyString(), anyBoolean());

        mockMvc.perform(delete("/projects/{projectId}/projectBoundary/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isNoContent());

        verify(projectBoundaryService).deleteProjectBoundary(anyString(), anyString(), eq(false));
    }

    ProjectBoundaryModel buildProjectBoundaryRequestModel() {
        GeometryFactory geometryFactory = new GeometryFactory();
        Point locationPoint = geometryFactory.createPoint(new Coordinate(-123.3656, 48.4284));
        Coordinate[] coordinates = {
                new Coordinate(-123.3656, 48.4284),
                new Coordinate(-123.3657, 48.4285),
                new Coordinate(-123.3658, 48.4284),
                new Coordinate(-123.3656, 48.4284)
        };

        LinearRing shell = geometryFactory.createLinearRing(coordinates);
        Polygon polygon = geometryFactory.createPolygon(shell);
        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(new Polygon[]{polygon});

        return ProjectBoundaryModel.builder()
                .projectBoundaryGuid(UUID.randomUUID().toString())
                .projectGuid(UUID.randomUUID().toString())
                .systemStartTimestamp(new Date())
                .systemEndTimestamp(new Date())
                .collectionDate(new Date())
                .boundarySizeHa(new BigDecimal("10.5"))
                .locationGeometry(locationPoint)
                .boundaryGeometry(multiPolygon)
                .build();
    }

}
