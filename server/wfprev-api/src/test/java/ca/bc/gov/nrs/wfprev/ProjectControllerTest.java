package ca.bc.gov.nrs.wfprev;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.bc.gov.nrs.wfprev.controllers.ProjectController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.services.ProjectService;

@WebMvcTest(ProjectController.class)
@Import({SecurityConfig.class, TestcontainersConfiguration.class})
class ProjectControllerTest {
  @MockBean
  private ProjectService projectService;

  @Autowired
  private MockMvc mockMvc;
  
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
  void testCreateProject() throws Exception {
    ProjectModel project = new ProjectModel();
    when(projectService.createOrUpdateProject(project)).thenReturn(project);

    mockMvc.perform(post("/projects", project)
           .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isCreated());
  }
}
