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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.bc.gov.nrs.wfprev.controllers.ProjectController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;
import ca.bc.gov.nrs.wfprev.services.ProjectService;

@WebMvcTest(ProjectController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class})
class ProjectControllerTest {
  @MockBean
  private ProjectService projectService;

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
  void testGetProject() throws Exception {
    String guid = UUID.randomUUID().toString();

    ProjectModel project = new ProjectModel();
    project.setProjectGuid(guid);

    List<ProjectModel> projectList = Arrays.asList(project);
    CollectionModel<ProjectModel> projectModel = CollectionModel.of(projectList);

    when(projectService.getAllProjects()).thenReturn(projectModel);

    mockMvc.perform(get("/projects")
           .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk());

    when(projectService.getProjectById(guid)).thenReturn(project);

    mockMvc.perform(get("/projects/{id}", guid)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void testCreateUpdateProject() throws Exception {
    ProjectModel project = new ProjectModel();
    project.setProjectTypeCode(new ProjectTypeCodeModel());
    project.setProjectNumber(1);
    project.setSiteUnitName("Test");
    project.setProgramAreaGuid(UUID.randomUUID().toString());
    project.setProjectName("Test");
    project.setIsMultiFiscalYearProj(false);
    project.setLatitude(new BigDecimal(40.99));
    project.setLongitude(new BigDecimal(-115.23));
    project.setLastProgressUpdateTimestamp(new Date());
    
    when(projectService.createOrUpdateProject(project)).thenReturn(project);

    String json = gson.toJson(project);

    mockMvc.perform(post("/projects")
              .contentType(MediaType.APPLICATION_JSON)
              .content(json)
              .accept("application/json")
              .header("Authorization", "Bearer admin-token"))
              .andExpect(status().isCreated());

    project.setClosestCommunityName("Test");
    when(projectService.createOrUpdateProject(project)).thenReturn(project);

    json = gson.toJson(project);

    mockMvc.perform(put("/projects/{id}")
           .content(json)
           .contentType(MediaType.APPLICATION_JSON)
           .header("Authorization", "Bearer admin-token"))
           .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser
  void testDeleteProject() throws Exception {
    ProjectModel project = new ProjectModel();
    when(projectService.createOrUpdateProject(project)).thenReturn(project);

    String json = gson.toJson(project);

    mockMvc.perform(post("/projects")
           .content(json)
           .contentType(MediaType.APPLICATION_JSON)
           .accept("application/json")
           .header("Authorization", "Bearer admin-token"))
           .andExpect(status().isCreated());

    when(projectService.deleteProject(project.getProjectGuid())).thenReturn(null);

    mockMvc.perform(delete("/projects/{id}", project.getProjectGuid())
           .contentType(MediaType.APPLICATION_JSON)
           .header("Authorization", "Bearer admin-token"))
           .andExpect(status().isOk());
  }
}
