package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.common.exceptions.ServiceException;
import ca.bc.gov.nrs.wfprev.common.serializers.GeoJsonJacksonDeserializer;
import ca.bc.gov.nrs.wfprev.common.serializers.GeoJsonJacksonSerializer;
import ca.bc.gov.nrs.wfprev.controllers.ProjectBoundaryController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.services.ProjectBoundaryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.persistence.EntityNotFoundException;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.json.GeolatteGeomModule;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectBoundaryController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class, MockMvcRestExceptionConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class ProjectBoundaryControllerTest {

    @MockBean
    private ProjectBoundaryService projectBoundaryService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "springSecurityAuditorAware")
    private AuditorAware<String> auditorAware;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        SimpleModule geoJsonModule = new SimpleModule();
        geoJsonModule.addSerializer(Geometry.class, new GeoJsonJacksonSerializer());
        geoJsonModule.addDeserializer(Geometry.class, new GeoJsonJacksonDeserializer());
        objectMapper.registerModule(geoJsonModule);
        objectMapper.registerModule(new GeolatteGeomModule());
    }

    @Test
    @WithMockUser
    void testGetAllProjectBoundaries_Success() throws Exception {
        // GIVEN: Mock service response with a collection of ProjectBoundaryModel
        ProjectBoundaryModel project1 = new ProjectBoundaryModel();
        project1.setProjectGuid(UUID.randomUUID().toString());

        ProjectBoundaryModel project2 = new ProjectBoundaryModel();
        project2.setProjectGuid(UUID.randomUUID().toString());

        CollectionModel<ProjectBoundaryModel> responseCollection = CollectionModel.of(List.of(project1, project2));

        when(projectBoundaryService.getAllProjectBoundaries()).thenReturn(responseCollection);

        // WHEN / THEN: Perform GET request and expect 200 OK with correct response
        mockMvc.perform(get("/projectBoundaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetAllProjectBoundaries_RuntimeException() throws Exception {
        // GIVEN: Service throws RuntimeException
        when(projectBoundaryService.getAllProjectBoundaries()).thenThrow(new RuntimeException("Unexpected error"));

        // WHEN / THEN: Expect 500 Internal Server Error
        mockMvc.perform(get("/projectBoundaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    @WithMockUser
    void testGetProjectBoundary() throws Exception {
        String guid = UUID.randomUUID().toString();

        ProjectBoundaryModel project = new ProjectBoundaryModel();
        project.setProjectGuid(guid);

        when(projectBoundaryService.getProjectBoundaryById(guid)).thenReturn(project);

        mockMvc.perform(get("/projectBoundaries/{id}", guid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectGuid").value(guid));
    }

    @Test
    @WithMockUser
    void testGetById_Success() throws Exception {
        // GIVEN
        String guid = UUID.randomUUID().toString();
        ProjectBoundaryModel project = new ProjectBoundaryModel();
        project.setProjectGuid(guid);

        when(projectBoundaryService.getProjectBoundaryById(guid)).thenReturn(project);

        // WHEN / THEN
        mockMvc.perform(get("/projectBoundaries/{id}", guid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectGuid").value(guid));
    }

    @Test
    @WithMockUser
    void testGetById_NotFound_NullResponse() throws Exception {
        // GIVEN
        String guid = UUID.randomUUID().toString();
        when(projectBoundaryService.getProjectBoundaryById(guid)).thenReturn(null); // Simulate not found

        // WHEN / THEN
        mockMvc.perform(get("/projectBoundaries/{id}", guid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetById_EntityNotFoundException() throws Exception {
        // GIVEN
        String guid = UUID.randomUUID().toString();
        when(projectBoundaryService.getProjectBoundaryById(guid)).thenThrow(new EntityNotFoundException("Not found"));

        // WHEN / THEN
        mockMvc.perform(get("/projectBoundaries/{id}", guid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetById_InternalServerError() throws Exception {
        // GIVEN
        String guid = UUID.randomUUID().toString();
        when(projectBoundaryService.getProjectBoundaryById(guid)).thenThrow(new RuntimeException("Unexpected error"));

        // WHEN / THEN
        mockMvc.perform(get("/projectBoundaries/{id}", guid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    @WithMockUser
    void testCreateProjectBoundary() throws Exception {
        String guid = UUID.randomUUID().toString();
        ProjectBoundaryModel project = new ProjectBoundaryModel();
        project.setProjectGuid(guid);
        project.setProjectBoundaryGuid(guid);

        when(projectBoundaryService.createOrUpdateProjectBoundary(any(ProjectBoundaryModel.class)))
                .thenReturn(project);

        String json = objectMapper.writeValueAsString(project);

        mockMvc.perform(post("/projectBoundaries")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectGuid").value(guid));
    }

    @Test
    @WithMockUser
    void testCreateProjectBoundary_DataIntegrityViolationException() throws Exception {
        ProjectBoundaryModel project = new ProjectBoundaryModel();

        when(projectBoundaryService.createOrUpdateProjectBoundary(any(ProjectBoundaryModel.class)))
                .thenThrow(DataIntegrityViolationException.class);

        String json = objectMapper.writeValueAsString(project);

        mockMvc.perform(post("/projectBoundaries")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testUpdateProjectBoundary() throws Exception {
        String guid = UUID.randomUUID().toString();
        ProjectBoundaryModel project = new ProjectBoundaryModel();
        project.setProjectGuid(guid);
        project.setProjectBoundaryGuid(guid);
        project.setBoundaryComment("Updated Comment");

        when(projectBoundaryService.createOrUpdateProjectBoundary(any(ProjectBoundaryModel.class)))
                .thenReturn(project);

        String json = objectMapper.writeValueAsString(project);

        mockMvc.perform(put("/projectBoundaries/{id}", guid)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boundaryComment").value("Updated Comment"));
    }

    @Test
    @WithMockUser
    void testUpdateProjectBoundary_DataIntegrityViolationException() throws Exception {
        String guid = UUID.randomUUID().toString();
        ProjectBoundaryModel project = new ProjectBoundaryModel();
        project.setProjectGuid(guid);
        project.setProjectBoundaryGuid(guid);

        when(projectBoundaryService.createOrUpdateProjectBoundary(any(ProjectBoundaryModel.class)))
                .thenThrow(DataIntegrityViolationException.class);

        String json = objectMapper.writeValueAsString(project);

        mockMvc.perform(put("/projectBoundaries/{id}", guid)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testDeleteProjectBoundary() throws Exception {
        String guid = UUID.randomUUID().toString();

        mockMvc.perform(delete("/projectBoundaries/{id}", guid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void testDeleteProjectBoundary_EntityNotFoundException() throws Exception {
        String guid = UUID.randomUUID().toString();
        when(projectBoundaryService.deleteProjectBoundary(guid)).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(delete("/projectBoundaries/{id}", guid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteProjectBoundary_InternalServerError() throws Exception {
        String guid = UUID.randomUUID().toString();
        when(projectBoundaryService.deleteProjectBoundary(guid)).thenThrow(RuntimeException.class);

        mockMvc.perform(delete("/projectBoundaries/{id}", guid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isInternalServerError());
    }
}


