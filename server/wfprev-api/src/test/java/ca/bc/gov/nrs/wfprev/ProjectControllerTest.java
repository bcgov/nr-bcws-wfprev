package ca.bc.gov.nrs.wfprev;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ca.bc.gov.nrs.wfprev.services.ProjectService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.bc.gov.nrs.wfprev.controllers.ProjectController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;
import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import org.springframework.test.web.servlet.ResultActions;

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
    void testGetAllProjects_Empty() throws Exception {

        List<ProjectModel> projectList = Collections.emptyList();
        CollectionModel<ProjectModel> projectModel = CollectionModel.of(projectList);

        when(projectService.getAllProjects()).thenReturn(projectModel);

        ResultActions result = mockMvc.perform(get("/projects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals("{}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    @WithMockUser
    void testGetAllProjects_ServiceException() throws Exception {
        when(projectService.getAllProjects()).thenThrow(new ServiceException("Error getting projects"));

        ResultActions result = mockMvc.perform(get("/projects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        assertEquals(500, result.andReturn().getResponse().getStatus());
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
    void testGetProject_NotFound() throws Exception {
        // Given
        String projectGuid = UUID.randomUUID().toString();
        when(projectService.getProjectById(projectGuid)).thenReturn(null);

        // When
        ResultActions result = mockMvc.perform(get("/projects/{id}", projectGuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Then
        verify(projectService, times(1)).getProjectById(projectGuid);
    }

    @Test
    @WithMockUser
    void testGetProject_ServiceException() throws Exception {
        // Given
        String projectGuid = UUID.randomUUID().toString();
        when(projectService.getProjectById(projectGuid)).thenThrow(new ServiceException("Error getting project"));

        // When
        ResultActions result = mockMvc.perform(get("/projects/{id}", projectGuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        // Then
        verify(projectService, times(1)).getProjectById(projectGuid);
        assertEquals(500, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser
    void testUpdateProject_NotFound() throws Exception {
        // Given
        String projectGuid = UUID.randomUUID().toString();
        ProjectModel project = new ProjectModel();
        project.setProjectGuid(projectGuid);

        when(projectService.createOrUpdateProject(any(ProjectModel.class))).thenReturn(null);

        String json = gson.toJson(project);

        // When
        ResultActions result = mockMvc.perform(put("/projects/{id}", projectGuid)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound());

        // Then
        verify(projectService, times(1)).createOrUpdateProject(any(ProjectModel.class));
        assertEquals(404, result.andReturn().getResponse().getStatus());
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
        String projectGuid = UUID.randomUUID().toString();
        project.setProjectGuid(projectGuid);

        when(projectService.createOrUpdateProject(any(ProjectModel.class))).thenReturn(project);

        String json = gson.toJson(project);

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept("application/json")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isCreated());

        project.setClosestCommunityName("Test");
        when(projectService.createOrUpdateProject(any(ProjectModel.class))).thenReturn(project);

        json = gson.toJson(project);

        mockMvc.perform(put("/projects/{id}", projectGuid)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testCreateProject_ServiceException() throws Exception {
        // Given
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
        String projectGuid = UUID.randomUUID().toString();
        project.setProjectGuid(projectGuid);

        when(projectService.createOrUpdateProject(any(ProjectModel.class))).thenThrow(new ServiceException("Error creating project"));

        String json = gson.toJson(project);

        // When
        ResultActions result = mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept("application/json")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().is5xxServerError());

        // Then
        verify(projectService, times(1)).createOrUpdateProject(any(ProjectModel.class));
        assertEquals(500, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser
    void testUpdateProject_BadRequest() throws Exception {
        // Given
        ProjectModel project = new ProjectModel();
        project.setProjectGuid(UUID.randomUUID().toString());

        when(projectService.createOrUpdateProject(any(ProjectModel.class))).thenReturn(project);

        String json = gson.toJson(project);

        // When
        ResultActions result = mockMvc.perform(put("/projects/{id}", "invalid")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer admin-token"));
        Assertions.assertEquals(400, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser
    void testDeleteProject() throws Exception {
        // Given
        String projectGuid = UUID.randomUUID().toString();
        ProjectModel project = new ProjectModel();
        project.setProjectGuid(projectGuid);

        when(projectService.deleteProject(projectGuid)).thenReturn(project);

        // When
        mockMvc.perform(delete("/projects/{id}", projectGuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());

        // Then
        verify(projectService, times(1)).deleteProject(projectGuid);
    }

    @Test
    @WithMockUser
    void testDeleteProject_Exception() throws Exception {
        // Given
        String projectGuid = UUID.randomUUID().toString();
        when(projectService.deleteProject(projectGuid)).thenThrow(new ServiceException("Error deleting project"));

        // When
        ResultActions result = mockMvc.perform(delete("/projects/{id}", projectGuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().is5xxServerError());

        // Then
        verify(projectService, times(1)).deleteProject(projectGuid);
        Assertions.assertEquals(500, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser
    void testDeleteProject_notFound() throws Exception {
        // Given
        String projectGuid = UUID.randomUUID().toString();
        when(projectService.deleteProject(projectGuid)).thenReturn(null);
        // When
        ResultActions result = mockMvc.perform(delete("/projects/{id}", projectGuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().is4xxClientError());

        // Then
        verify(projectService, times(1)).deleteProject(projectGuid);
        Assertions.assertEquals(404, result.andReturn().getResponse().getStatus());
    }
}
