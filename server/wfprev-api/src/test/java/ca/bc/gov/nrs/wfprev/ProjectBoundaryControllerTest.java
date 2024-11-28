package ca.bc.gov.nrs.wfprev;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.bc.gov.nrs.wfprev.common.serializers.GeoJsonJacksonDeserializer;
import ca.bc.gov.nrs.wfprev.common.serializers.GeoJsonJacksonSerializer;
import ca.bc.gov.nrs.wfprev.controllers.ProjectBoundaryController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.services.ProjectBoundaryService;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(ProjectBoundaryController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class, MockMvcRestExceptionConfiguration.class})
class ProjectBoundaryControllerTest {
    @MockBean
    private ProjectBoundaryService projectBoundaryService;

    @Autowired
    private MockMvc mockMvc;

    private Gson gson;

    @BeforeEach
    void setup() {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").serializeSpecialFloatingPointValues();
        gson = builder.create();
    }

    @Test
    @WithMockUser
    void testGetProjectBoundary() throws Exception {
        String guid = UUID.randomUUID().toString();

        ProjectBoundaryModel project = new ProjectBoundaryModel();
        project.setProjectGuid(guid);

        List<ProjectBoundaryModel> projectList = Arrays.asList(project);
        CollectionModel<ProjectBoundaryModel> projectModel = CollectionModel.of(projectList);

        when(projectBoundaryService.getAllProjectBoundaries()).thenReturn(projectModel);

        mockMvc.perform(get("/projectBoundaries")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        when(projectBoundaryService.getProjectBoundaryById(guid)).thenReturn(project);

        mockMvc.perform(get("/projectBoundaries/{id}", guid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testCreateUpdateProjectBoundary() throws Exception {
        ProjectBoundaryModel project = new ProjectBoundaryModel();
        String id = UUID.randomUUID().toString();

        project.setProjectGuid(id);
        project.setProjectBoundaryGuid(id);

        when(projectBoundaryService.createOrUpdateProjectBoundary(any(ProjectBoundaryModel.class)))
                .thenReturn(project);

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new GeoJsonJacksonSerializer());
        simpleModule.addDeserializer(Geometry.class, new GeoJsonJacksonDeserializer());
        mapper.registerModule(simpleModule);

        String json = mapper.writeValueAsString(project);

        // Test create
        mockMvc.perform(post("/projectBoundaries")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token")
                        .header("If-Match", "\"1\""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectGuid").value(id))
                .andExpect(jsonPath("$.projectBoundaryGuid").value(id));

        // Test update
        project.setBoundaryComment("Test");
        when(projectBoundaryService.createOrUpdateProjectBoundary(any(ProjectBoundaryModel.class)))
                .thenReturn(project);  // Return updated project with comment

        json = mapper.writeValueAsString(project);

        mockMvc.perform(put("/projectBoundaries/{id}", id)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token")
                        .header("If-Match", "\"1\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectGuid").value(id))
                .andExpect(jsonPath("$.boundaryComment").value("Test"));
    }
    @Test
    @WithMockUser
    void testDeleteProjectBoundary() throws Exception {
        ProjectBoundaryModel project = new ProjectBoundaryModel();
        when(projectBoundaryService.createOrUpdateProjectBoundary(any(ProjectBoundaryModel.class))).thenReturn(project);

        String json = gson.toJson(project);

        mockMvc.perform(post("/projectBoundaries")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isCreated());

        when(projectBoundaryService.deleteProjectBoundary(project.getProjectGuid())).thenReturn(null);

        mockMvc.perform(delete("/projectBoundaries/{id}", project.getProjectGuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }
}
