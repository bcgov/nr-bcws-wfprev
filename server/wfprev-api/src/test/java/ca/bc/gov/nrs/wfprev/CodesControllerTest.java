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
        ForestAreaCodeModel forestAreaCode = new ForestAreaCodeModel();
        GeneralScopeCodeModel generalScopeCode = new GeneralScopeCodeModel();
        ProjectTypeCodeModel projectTypeCode = new ProjectTypeCodeModel();

        when(codesService.getForestAreaCodeById(null)).thenReturn(forestAreaCode);
        when(codesService.getGeneralScopeCodeById(null)).thenReturn(generalScopeCode);
        when(codesService.getProjectTypeCodeById(null)).thenReturn(projectTypeCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.FOREST_AREA_CODE, null)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.GENERAL_SCOPE_CODE, null)
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.PROJECT_TYPE_CODE, null)
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }
}
