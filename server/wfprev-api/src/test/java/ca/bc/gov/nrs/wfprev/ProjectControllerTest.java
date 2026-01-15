package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.controllers.ProjectController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;
import ca.bc.gov.nrs.wfprev.services.ProjectService;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class ProjectControllerTest {
    @MockBean
    private ProjectService projectService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "springSecurityAuditorAware")
    private AuditorAware<String> auditorAware;

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
        mockMvc.perform(get("/projects/{id}", projectGuid)
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

        when(projectService.updateProject(any(ProjectModel.class))).thenReturn(null);

        String json = gson.toJson(project);

        // When
        ResultActions result = mockMvc.perform(put("/projects/{id}", projectGuid)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound());

        // Then
        verify(projectService, times(1)).updateProject(any(ProjectModel.class));
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

        when(projectService.createProject(any(ProjectModel.class))).thenReturn(project);

        String json = gson.toJson(project);

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept("application/json")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isCreated());

        project.setClosestCommunityName("Test");
        when(projectService.updateProject(any(ProjectModel.class))).thenReturn(project);

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

        when(projectService.createProject(any(ProjectModel.class))).thenThrow(new ServiceException("Error creating project"));

        String json = gson.toJson(project);

        // When
        ResultActions result = mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept("application/json")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().is5xxServerError());

        // Then
        verify(projectService, times(1)).createProject(any(ProjectModel.class));
        assertEquals(500, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser
    void testCreateProject_Conflict() throws Exception {
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

        when(projectService.createProject(any(ProjectModel.class))).thenReturn(project);

        String json = gson.toJson(project);

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept("application/json")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isCreated());

        when(projectService.createProject(any(ProjectModel.class))).thenThrow(new DataIntegrityViolationException("Error creating project"));

        // When
        ResultActions result = mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept("application/json")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isConflict());

        // Then
        verify(projectService, times(2)).createProject(any(ProjectModel.class));
        assertEquals(409, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser
    void testUpdateProject_BadRequest() throws Exception {
        // Given
        ProjectModel project = new ProjectModel();
        project.setProjectGuid(UUID.randomUUID().toString());

        when(projectService.updateProject(any(ProjectModel.class))).thenReturn(project);

        String json = gson.toJson(project);

        // When
        ResultActions result = mockMvc.perform(put("/projects/{id}", "invalid")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer admin-token"));
        assertEquals(400, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser
    void testDeleteAProject_Success() throws Exception {
        // GIVEN a valid project  ID
        String projectId = "456e7890-e89b-12d3-a456-426614174001";

        // WHEN the delete endpoint is called
        mockMvc.perform(delete("/projects/{id}", projectId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                // THEN the response status should be 204 No Content
                .andExpect(status().isNoContent());

        // THEN the service's delete method should be called once with the correct ID and deleteFiles=false (default)
        verify(projectService).deleteProject(projectId, false);
    }

    @Test
    @WithMockUser
    void testDeleteAProject_Success_WithDeleteFiles() throws Exception {
        // GIVEN a valid project ID
        String projectId = "456e7890-e89b-12d3-a456-426614174001";

        // WHEN the delete endpoint is called with deleteFiles=true
        mockMvc.perform(delete("/projects/{id}", projectId)
                        .param("deleteFiles", "true")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                // THEN the response status should be 204 No Content
                .andExpect(status().isNoContent());

        // THEN the service's delete method should be called once with deleteFiles=true
        verify(projectService).deleteProject(projectId, true);
    }

    @Test
    @WithMockUser
    void testDeleteAProject_NotFound() throws Exception {
        // GIVEN a project  ID that does not exist
        String projectId = "456e7890-e89b-12d3-a456-426614174001";
        doThrow(new EntityNotFoundException("Not found")).when(projectService).deleteProject(projectId, false);

        // WHEN the delete endpoint is called
        mockMvc.perform(delete("/projects/{id}", projectId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                // THEN the response status should be 404 Not Found
                .andExpect(status().isNotFound());

        // THEN the service's delete method should be called once with the correct ID
        verify(projectService).deleteProject(projectId, false);
    }

    @Test
    @WithMockUser
    void testDeleteAProject_InvalidId() throws Exception {
        // GIVEN an invalid project  ID
        String invalidId = "invalid-uuid";
        doThrow(new IllegalArgumentException("Invalid UUID")).when(projectService).deleteProject(invalidId, false);

        // WHEN the delete endpoint is called
        mockMvc.perform(delete("/projects/{id}", invalidId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                // THEN the response status should be 400 Bad Request
                .andExpect(status().isBadRequest());

        // THEN the service's delete method should be called once with the invalid ID
        verify(projectService).deleteProject(invalidId, false);
    }

    @Test
    @WithMockUser
    void testDeleteAProject_InternalServerError() throws Exception {
        // GIVEN a valid project  ID but an unexpected error occurs
        String projectId = "456e7890-e89b-12d3-a456-426614174001";
        doThrow(new RuntimeException("Unexpected error")).when(projectService).deleteProject(projectId, false);

        // WHEN the delete endpoint is called
        mockMvc.perform(delete("/projects/{id}", projectId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                // THEN the response status should be 500 Internal Server Error
                .andExpect(status().isInternalServerError());

        // THEN the service's delete method should be called once with the correct ID
        verify(projectService).deleteProject(projectId, false);
    }

    @Test
    @WithMockUser
    void testUpdateProject_Success() throws Exception {
        // Given
        ProjectModel project = new ProjectModel();
        String guid = UUID.randomUUID().toString();
        String programAreaGuid = UUID.randomUUID().toString();
        project.setProjectGuid(guid);
        project.setProjectName("Test");
        project.setProjectNumber(1);
        project.setSiteUnitName("Test");
        project.setProgramAreaGuid(programAreaGuid);
        project.setIsMultiFiscalYearProj(false);
        project.setLatitude(new BigDecimal("40.99"));
        project.setLongitude(new BigDecimal("-115.23"));

        when(projectService.updateProject(any(ProjectModel.class))).thenReturn(project);

        // When
        String json = gson.toJson(project);
        ResultActions result = mockMvc.perform(put("/projects/{id}", project.getProjectGuid())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))

                // Then
                .andExpect(status().isOk());

        assertEquals("" +
                        "{\"projectGuid\":\"" + guid + "\",\"projectNumber\":1,\"siteUnitName\":\"Test\",\"programAreaGuid\":\"" + programAreaGuid + "\",\"projectName\":\"Test\",\"isMultiFiscalYearProj\":false,\"latitude\":40.99,\"longitude\":-115.23}",
                result.andReturn().getResponse().getContentAsString());
    }

    @Test
    @WithMockUser
    void testUpdateProject_ServiceException() throws Exception{
        // Given
        ProjectModel project = new ProjectModel();
        String guid = UUID.randomUUID().toString();
        String programAreaGuid = UUID.randomUUID().toString();
        project.setProjectGuid(guid);
        project.setProjectName("Test");
        project.setProjectNumber(1);
        project.setSiteUnitName("Test");
        project.setProgramAreaGuid(programAreaGuid);
        project.setIsMultiFiscalYearProj(false);
        project.setLatitude(new BigDecimal("40.99"));
        project.setLongitude(new BigDecimal("-115.23"));

        when(projectService.updateProject(any(ProjectModel.class))).thenThrow(new ServiceException("Error updating project"));

        // When
        String json = gson.toJson(project);
        mockMvc.perform(put("/projects/{id}", project.getProjectGuid())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().is5xxServerError());

        // Then
        verify(projectService, times(1)).updateProject(any(ProjectModel.class));
    }

    @Test
    @WithMockUser
    void testGetProject_NotFound_NonGuid() throws Exception {
        // Given
        String projectGuid = "invalid";
        when(projectService.getProjectById(projectGuid)).thenThrow(new IllegalArgumentException("Invalid UUID: " + projectGuid));

        // When
        mockMvc.perform(get("/projects/{id}", projectGuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Then
        verify(projectService, times(1)).getProjectById(projectGuid);
    }
}
