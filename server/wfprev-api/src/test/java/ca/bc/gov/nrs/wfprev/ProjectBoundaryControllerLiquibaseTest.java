package ca.bc.gov.nrs.wfprev;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.bc.gov.nrs.wfprev.common.serializers.GeoJsonJacksonDeserializer;
import ca.bc.gov.nrs.wfprev.common.serializers.GeoJsonJacksonSerializer;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.services.ProjectBoundaryService;

/**
 * Identical test to the Project Boundary Controller test,
 * However this one will instantiate and execute the Liquibase scripts
 * so instead of just mocks, we could use the container and write to the DB
 * directly.
 * 
 * Note that this will mean that the "DB" folder will need to be on the classpath
 * for liquibase to find it, either copy to resources, or link through target on build
 * Currently DB must be on classpath, but there is an open ticket to change to relative
 * https://github.com/liquibase/liquibase/issues/2687
 */

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, MockMvcRestExceptionConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(value = {
        "classpath:application-test.properties"
})
class ProjectBoundaryControllerLiquibaseTest {
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
  void testCreateUpdateProjectBoundary() throws Exception {
    ProjectBoundaryModel project = new ProjectBoundaryModel();

    project.setBoundaryComment("test");
    project.setCollectionMethod("test");
    project.setSystemStartTimestamp(new Date());
    project.setSystemEndTimestamp(new Date());
    project.setProjectBoundaryGuid(UUID.randomUUID().toString());
    project.setCollectionDate(new Date());
    project.setBoundarySizeHa(BigDecimal.ONE);
    project.setLocationGeometry((Geometry) new GeometryFactory().createPoint(new Coordinate(1, 1)));

    Coordinate[] coords = new Coordinate[] {new Coordinate(1, 1), new Coordinate(2, 1), new Coordinate(2, 2), new Coordinate(1, 2), new Coordinate(1, 1)};
    project.setBoundaryGeometry(new GeometryFactory().createPolygon(coords));

    when(projectBoundaryService.createOrUpdateProjectBoundary(project)).thenReturn(project);

    // use the jackson mapper for handling Geometry types, Gson converters aren't
    // implemented yet
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(new GeoJsonJacksonSerializer());
    simpleModule.addDeserializer(Geometry.class, new GeoJsonJacksonDeserializer());
    mapper.registerModule(simpleModule);

    String json = mapper.writeValueAsString(project);

    mockMvc.perform(post("/projectBoundaries")
           .content(json)
           .contentType(MediaType.APPLICATION_JSON)
           .accept("application/json")
           .header("Authorization", "Bearer admin-token"))
           .andExpect(status().isCreated());


    project.setBoundaryComment("Test");
    when(projectBoundaryService.createOrUpdateProjectBoundary(project)).thenReturn(project);

    json = gson.toJson(project);

    mockMvc.perform(put("/projectBoundaries/{id}")
           .content(json)
           .contentType(MediaType.APPLICATION_JSON)
           .header("Authorization", "Bearer admin-token"))
           .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser
  void testDeleteProjectBoundary() throws Exception {
    ProjectBoundaryModel project = new ProjectBoundaryModel();
    when(projectBoundaryService.createOrUpdateProjectBoundary(project)).thenReturn(project);

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
