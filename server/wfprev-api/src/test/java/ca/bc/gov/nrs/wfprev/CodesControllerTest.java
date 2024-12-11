package ca.bc.gov.nrs.wfprev;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.models.ForestAreaCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.GeneralScopeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;
import ca.bc.gov.nrs.wfprev.services.CodesService;

@WebMvcTest(CodesController.class)
@Import({SecurityConfig.class, TestcontainersConfiguration.class})
class CodesControllerTest {

    @MockBean
    private CodesService codesService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testGetAllCodes() throws Exception {
      testGetForestAreaCodes();
      testGetGeneralScopeCodes();
      testGetProjectTypeCodes();
    }

    void testGetForestAreaCodes() throws Exception {
      String exampleId1 = UUID.randomUUID().toString();
      String exampleId2 = UUID.randomUUID().toString();

      ForestAreaCodeModel fac1 = new ForestAreaCodeModel();
      fac1.setForestAreaCode(exampleId1);

      ForestAreaCodeModel fac2 = new ForestAreaCodeModel();
      fac2.setForestAreaCode(exampleId2);

      List<ForestAreaCodeModel> facList = Arrays.asList(fac1, fac2);
      CollectionModel<ForestAreaCodeModel> facModel = CollectionModel.of(facList);

      when(codesService.getAllForestAreaCodes()).thenReturn(facModel);

      mockMvc.perform(get("/codes/" + CodeTables.FOREST_AREA_CODE)
              .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk());
    }

    void testGetGeneralScopeCodes() throws Exception {
      String exampleId1 = UUID.randomUUID().toString();
      String exampleId2 = UUID.randomUUID().toString();

      GeneralScopeCodeModel fac1 = new GeneralScopeCodeModel();
      fac1.setGeneralScopeCode(exampleId1);

      GeneralScopeCodeModel fac2 = new GeneralScopeCodeModel();
      fac2.setGeneralScopeCode(exampleId2);

      List<GeneralScopeCodeModel> facList = Arrays.asList(fac1, fac2);
      CollectionModel<GeneralScopeCodeModel> facModel = CollectionModel.of(facList);

      when(codesService.getAllGeneralScopeCodes()).thenReturn(facModel);

      mockMvc.perform(get("/codes/" + CodeTables.GENERAL_SCOPE_CODE)
              .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk());
    }

    void testGetProjectTypeCodes() throws Exception {
      String exampleId1 = UUID.randomUUID().toString();
      String exampleId2 = UUID.randomUUID().toString();

      ProjectTypeCodeModel fac1 = new ProjectTypeCodeModel();
      fac1.setProjectTypeCode(exampleId1);

      ProjectTypeCodeModel fac2 = new ProjectTypeCodeModel();
      fac2.setProjectTypeCode(exampleId2);

      List<ProjectTypeCodeModel> facList = Arrays.asList(fac1, fac2);
      CollectionModel<ProjectTypeCodeModel> facModel = CollectionModel.of(facList);

      when(codesService.getAllProjectTypeCodes()).thenReturn(facModel);

      mockMvc.perform(get("/codes/" + CodeTables.PROJECT_TYPE_CODE)
              .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetCodesById() throws Exception {
        String ptID = UUID.randomUUID().toString();
        ProjectTypeCodeModel projectTypeCode = new ProjectTypeCodeModel();
        projectTypeCode.setProjectTypeCode(ptID);

        when(codesService.getProjectTypeCodeById(ptID)).thenReturn(projectTypeCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.PROJECT_TYPE_CODE, ptID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String gsID = UUID.randomUUID().toString();
        GeneralScopeCodeModel generalScopeCode = new GeneralScopeCodeModel();
        generalScopeCode.setGeneralScopeCode(gsID);

        when(codesService.getGeneralScopeCodeById(gsID)).thenReturn(generalScopeCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.GENERAL_SCOPE_CODE, gsID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String faID = UUID.randomUUID().toString();
        ForestAreaCodeModel forestAreaCode = new ForestAreaCodeModel();
        forestAreaCode.setForestAreaCode(faID);

        when(codesService.getForestAreaCodeById(faID)).thenReturn(forestAreaCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.FOREST_AREA_CODE, faID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testCodesNotFound() throws Exception {
        String nonExistentId = "non-existent-id";

        // Mock service to return null for non-existent IDs
        when(codesService.getForestAreaCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getGeneralScopeCodeById(nonExistentId)).thenReturn(null);
        when(codesService.getProjectTypeCodeById(nonExistentId)).thenReturn(null);

        // Test valid code tables with non-existent ID
        mockMvc.perform(get("/codes/{codeTable}/{id}", "forestAreaCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "generalScopeCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/codes/{codeTable}/{id}", "projectTypeCodes", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());

        // Test invalid code table - should also return 404 since it hits the default case
        mockMvc.perform(get("/codes/{codeTable}/{id}", "invalidCodeTable", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetCodes_ServiceException() throws Exception {
        when(codesService.getAllForestAreaCodes()).thenThrow(new ServiceException("Service error"));

        mockMvc.perform(get("/codes/{codeTable}", CodeTables.FOREST_AREA_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetCodeById_ServiceException() throws Exception {
        String id = UUID.randomUUID().toString();
        when(codesService.getProjectTypeCodeById(id)).thenThrow(new ServiceException("Service error"));

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.PROJECT_TYPE_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetForestAreaCodes_VerifyServiceCall() throws Exception {
        // Mock the service return value
        when(codesService.getAllForestAreaCodes()).thenReturn(CollectionModel.empty());

        // Perform the request
        mockMvc.perform(get("/codes/{codeTable}", CodeTables.FOREST_AREA_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify that the correct service method is called
        verify(codesService, times(1)).getAllForestAreaCodes();
        verifyNoMoreInteractions(codesService); // Ensure no other service methods were called
    }

    @Test
    @WithMockUser
    void testGetGeneralScopeCodes_VerifyServiceCall() throws Exception {
        when(codesService.getAllGeneralScopeCodes()).thenReturn(CollectionModel.empty());

        mockMvc.perform(get("/codes/{codeTable}", CodeTables.GENERAL_SCOPE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(codesService, times(1)).getAllGeneralScopeCodes();
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetProgramAreaCodes_VerifyServiceCall() throws Exception {
        when(codesService.getAllProgramAreaCodes()).thenReturn(CollectionModel.empty());

        mockMvc.perform(get("/codes/{codeTable}", CodeTables.PROGRAM_AREA_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(codesService, times(1)).getAllProgramAreaCodes();
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetForestAreaCodeById_VerifyServiceCall() throws Exception {
        String id = UUID.randomUUID().toString();
        when(codesService.getForestAreaCodeById(id)).thenReturn(null);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.FOREST_AREA_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getForestAreaCodeById(id);
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetGeneralAreaCodeById_VerifyServiceCall() throws Exception {
        String id = UUID.randomUUID().toString();

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.GENERAL_SCOPE_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getGeneralScopeCodeById(id);
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetProjectTypeAreaCodeById_VerifyServiceCall() throws Exception {
        String id = UUID.randomUUID().toString();

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.PROJECT_TYPE_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getProjectTypeCodeById(id);
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetProgramAreaCodeById_VerifyServiceCall() throws Exception {
        String id = UUID.randomUUID().toString();

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.PROGRAM_AREA_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getProgramAreaCodeById(id);
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetForestRegionCodes() throws Exception {
        when(codesService.getAllForestRegionCodes()).thenReturn(CollectionModel.empty());
                mockMvc.perform(get("/codes/{codeTable}", CodeTables.FOREST_REGION_CODE)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());
        verify(codesService, times(1)).getAllForestRegionCodes();
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetForestRegionCodeById_VerifyServiceCall() throws Exception {
        Integer id = 1;
        when(codesService.getForestRegionCodeById(id)).thenReturn(null);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.FOREST_REGION_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getForestRegionCodeById(anyInt());
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetForestDistrictCodeById_VerifyServiceCall() throws Exception {
        Integer id = 1;
        when(codesService.getForestDistrictCodeById(id)).thenReturn(null);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.FOREST_DISTRICT_CODE, id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(codesService, times(1)).getForestDistrictCodeById(anyInt());
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetForestDistrictCodes() throws Exception {
        when(codesService.getAllForestDistrictCodes()).thenReturn(CollectionModel.empty());
        mockMvc.perform(get("/codes/{codeTable}", CodeTables.FOREST_DISTRICT_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(codesService, times(1)).getAllForestDistrictCodes();
        verifyNoMoreInteractions(codesService);
    }
}
