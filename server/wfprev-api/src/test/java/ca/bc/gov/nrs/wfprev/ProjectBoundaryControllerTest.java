package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.serializers.GeoJsonJacksonDeserializer;
import ca.bc.gov.nrs.wfprev.common.serializers.GeoJsonJacksonSerializer;
import ca.bc.gov.nrs.wfprev.controllers.ProjectBoundaryController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.services.ProjectBoundaryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.geolatte.geom.json.GeolatteGeomModule;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    void testGetProjectBoundary_NotFound() throws Exception {
        String guid = UUID.randomUUID().toString();
        when(projectBoundaryService.getProjectBoundaryById(guid)).thenReturn(null);

        mockMvc.perform(get("/projectBoundaries/{id}", guid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
    void testDeleteProjectBoundary() throws Exception {
        String guid = UUID.randomUUID().toString();

        mockMvc.perform(delete("/projectBoundaries/{id}", guid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());
    }

}
