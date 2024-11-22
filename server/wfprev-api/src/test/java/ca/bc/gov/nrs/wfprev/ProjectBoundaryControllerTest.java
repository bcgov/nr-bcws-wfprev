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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.bc.gov.nrs.wfprev.controllers.ProjectBoundaryController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.services.ProjectBoundaryService;

@WebMvcTest(ProjectBoundaryController.class)
@Import({SecurityConfig.class, TestcontainersConfiguration.class})
class ProjectBoundaryControllerTest {
  @MockBean
  private ProjectBoundaryService projectBoundaryService;

  @Autowired
  private MockMvc mockMvc;
  
  @Test
  @WithMockUser
  void testGetProject() throws Exception {
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
  void testCreateUpdateProject() throws Exception {
    ProjectBoundaryModel project = new ProjectBoundaryModel();
    when(projectBoundaryService.createOrUpdateProjectBoundary(project)).thenReturn(project);

    mockMvc.perform(post("/projectBoundaries", project)
           .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isCreated());


    project.setBoundaryComment("Test");
    when(projectBoundaryService.createOrUpdateProjectBoundary(project)).thenReturn(project);

    mockMvc.perform(put("/projectBoundaries", project)
           .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser
  void testDeleteProject() throws Exception {
    ProjectBoundaryModel project = new ProjectBoundaryModel();
    when(projectBoundaryService.createOrUpdateProjectBoundary(project)).thenReturn(project);

    mockMvc.perform(post("/projectBoundaries", project)
           .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isCreated());

    when(projectBoundaryService.deleteProjectBoundary(project.getProjectGuid())).thenReturn(null);

    mockMvc.perform(delete("/projectBoundaries/{id}", project.getProjectGuid())
           .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk());
  }
}
