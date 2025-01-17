package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.models.*;
import ca.bc.gov.nrs.wfprev.services.CodesService;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CodesController.class)
@Import({SecurityConfig.class, TestcontainersConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class CodesControllerTest {

    @MockBean
    private CodesService codesService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "springSecurityAuditorAware")  // Changed to match the expected bean name
    private AuditorAware<String> auditorAware;

    @Test
    @WithMockUser
    void testGetAllCodes() throws Exception {
        testGetForestAreaCodes();
        testGetGeneralScopeCodes();
        testGetProjectTypeCodes();
        testGetObjectiveTypeCodes();
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

        ProjectTypeCodeModel projectTypeCodeModel1 = new ProjectTypeCodeModel();
        projectTypeCodeModel1.setProjectTypeCode(exampleId1);

        ProjectTypeCodeModel projectTypeCodeModel2 = new ProjectTypeCodeModel();
        projectTypeCodeModel2.setProjectTypeCode(exampleId2);

        List<ProjectTypeCodeModel> projectTypeCodeModelList = Arrays.asList(projectTypeCodeModel1, projectTypeCodeModel2);
        CollectionModel<ProjectTypeCodeModel> projectTypeCodeModels = CollectionModel.of(projectTypeCodeModelList);

        when(codesService.getAllProjectTypeCodes()).thenReturn(projectTypeCodeModels);

        mockMvc.perform(get("/codes/" + CodeTables.PROJECT_TYPE_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void testGetObjectiveTypeCodes() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        ObjectiveTypeCodeModel otc1 = new ObjectiveTypeCodeModel();
        otc1.setObjectiveTypeCode(exampleId1);

        ObjectiveTypeCodeModel otc2 = new ObjectiveTypeCodeModel();
        otc1.setObjectiveTypeCode(exampleId2);

        List<ObjectiveTypeCodeModel> otcList = Arrays.asList(otc1, otc2);
        CollectionModel<ObjectiveTypeCodeModel> otcModel = CollectionModel.of(otcList);

        when(codesService.getAllObjectiveTypeCodes()).thenReturn(otcModel);

        mockMvc.perform(get("/codes/" + CodeTables.OBJECTIVE_TYPE_CODE)
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

        String otID = UUID.randomUUID().toString();
        ObjectiveTypeCodeModel objectiveTypeCode = new ObjectiveTypeCodeModel();
        objectiveTypeCode.setObjectiveTypeCode(otID);

        when(codesService.getObjectiveTypeCodeById(otID)).thenReturn(objectiveTypeCode);

        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.OBJECTIVE_TYPE_CODE, otID)
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
        when(codesService.getObjectiveTypeCodeById(nonExistentId)).thenReturn(null);

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

        mockMvc.perform(get("/codes/{codeTable}/{id}", "objectTypeCodes", nonExistentId)
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

    @Test
    @WithMockUser
    void testGetBCParksRegionCodes() throws Exception {
        // GIVEN
        BCParksRegionCodeModel bcparksRegionCodeModel = new BCParksRegionCodeModel();
        bcparksRegionCodeModel.setBcParksOrgUnitTypeCode("REGION");
        bcparksRegionCodeModel.setOrgUnitName("Region 1");
        bcparksRegionCodeModel.setOrgUnitId(1);
        bcparksRegionCodeModel.setEffectiveDate(new Date());
        bcparksRegionCodeModel.setExpiryDate(new Date());
        bcparksRegionCodeModel.setCharacterAlias("R1");
        bcparksRegionCodeModel.setIntegerAlias(1);

        CollectionModel<BCParksRegionCodeModel> bcparksRegionCodeModelCollection = CollectionModel.of(Arrays.asList(bcparksRegionCodeModel));
        when(codesService.getAllBCParksRegionCodes()).thenReturn(bcparksRegionCodeModelCollection);

        // WHEN
        mockMvc.perform(get("/codes/{codeTable}", CodeTables.BC_PARKS_REGION_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // THEN
                .andExpect(jsonPath("$._embedded.bcParksRegionCode[0].bcParksOrgUnitTypeCode").value("REGION"))
                .andExpect(jsonPath("$._embedded.bcParksRegionCode[0].orgUnitName").value("Region 1"))
                .andExpect(jsonPath("$._embedded.bcParksRegionCode[0].orgUnitId").value(1))
                .andExpect(jsonPath("$._embedded.bcParksRegionCode[0].characterAlias").value("R1"))
                .andExpect(jsonPath("$._embedded.bcParksRegionCode[0].integerAlias").value(1));

        verify(codesService, times(1)).getAllBCParksRegionCodes();
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetBCParksRegionCodesById_Success() throws Exception {
        // Given
        BCParksRegionCodeModel bcparksRegionCodeModel = new BCParksRegionCodeModel();
        bcparksRegionCodeModel.setBcParksOrgUnitTypeCode("REGION");
        bcparksRegionCodeModel.setOrgUnitName("Region 1");
        bcparksRegionCodeModel.setOrgUnitId(1);
        bcparksRegionCodeModel.setEffectiveDate(new Date());
        bcparksRegionCodeModel.setExpiryDate(new Date());
        bcparksRegionCodeModel.setCharacterAlias("R1");
        bcparksRegionCodeModel.setIntegerAlias(1);

        when(codesService.getBCParksRegionCodeById(anyInt())).thenReturn(bcparksRegionCodeModel);

        // When
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.BC_PARKS_REGION_CODE, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bcParksOrgUnitTypeCode").value("REGION"))
                .andExpect(jsonPath("$.orgUnitName").value("Region 1"))
                .andExpect(jsonPath("$.orgUnitId").value(1))
                .andExpect(jsonPath("$.characterAlias").value("R1"))
                .andExpect(jsonPath("$.integerAlias").value(1));
    }

    @Test
    @WithMockUser
    void testGetBCParksRegionCodesById_NotFound() throws Exception {
        // Given
        when(codesService.getBCParksRegionCodeById(anyInt())).thenReturn(null);

        // When
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.BC_PARKS_REGION_CODE, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetBCParksRegionCodesById_ServiceException() throws Exception {
        // Given
        when(codesService.getBCParksRegionCodeById(anyInt())).thenThrow(new ServiceException("Service error"));

        // When
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.BC_PARKS_REGION_CODE, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void getBCParksSectionCodes() throws Exception {
        // GIVEN
        BCParksSectionCodeModel bcparksSectionCodeModel = new BCParksSectionCodeModel();
        bcparksSectionCodeModel.setBcParksOrgUnitTypeCode("SECTION");
        bcparksSectionCodeModel.setOrgUnitName("Section 1");
        bcparksSectionCodeModel.setOrgUnitId(1);
        bcparksSectionCodeModel.setEffectiveDate(new Date());
        bcparksSectionCodeModel.setExpiryDate(new Date());
        bcparksSectionCodeModel.setCharacterAlias("S1");
        bcparksSectionCodeModel.setIntegerAlias(1);

        CollectionModel<BCParksSectionCodeModel> bcparksSectionCodeCollectionModel = CollectionModel.of(Arrays.asList(bcparksSectionCodeModel));


        when(codesService.getAllBCParksSectionCodes()).thenReturn(bcparksSectionCodeCollectionModel);

        // WHEN
        mockMvc.perform(get("/codes/{codeTable}", CodeTables.BC_PARKS_SECTION_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bcParksSectionCode[0].bcParksOrgUnitTypeCode").value("SECTION"))
                .andExpect(jsonPath("$._embedded.bcParksSectionCode[0].orgUnitName").value("Section 1"))
                .andExpect(jsonPath("$._embedded.bcParksSectionCode[0].orgUnitId").value(1))
                .andExpect(jsonPath("$._embedded.bcParksSectionCode[0].characterAlias").value("S1"))
                .andExpect(jsonPath("$._embedded.bcParksSectionCode[0].integerAlias").value(1));

        verify(codesService, times(1)).getAllBCParksSectionCodes();
        verifyNoMoreInteractions(codesService);
    }

    @Test
    @WithMockUser
    void testGetBCParksSectionCodesById_Success() throws Exception {
        // Given
        BCParksSectionCodeModel bcparksSectionCodeModel = new BCParksSectionCodeModel();
        bcparksSectionCodeModel.setBcParksOrgUnitTypeCode("SECTION");
        bcparksSectionCodeModel.setOrgUnitName("Section 1");
        bcparksSectionCodeModel.setOrgUnitId(1);
        bcparksSectionCodeModel.setEffectiveDate(new Date());
        bcparksSectionCodeModel.setExpiryDate(new Date());
        bcparksSectionCodeModel.setCharacterAlias("S1");
        bcparksSectionCodeModel.setIntegerAlias(1);

        when(codesService.getBCParksSectionCodeById(anyInt())).thenReturn(bcparksSectionCodeModel);

        // When
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.BC_PARKS_SECTION_CODE, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bcParksOrgUnitTypeCode").value("SECTION"))
                .andExpect(jsonPath("$.orgUnitName").value("Section 1"))
                .andExpect(jsonPath("$.orgUnitId").value(1))
                .andExpect(jsonPath("$.characterAlias").value("S1"))
                .andExpect(jsonPath("$.integerAlias").value(1));
    }

    @Test
    @WithMockUser
    void testGetBCParksSectionCodesById_NotFound() throws Exception {
        // Given
        when(codesService.getBCParksSectionCodeById(anyInt())).thenReturn(null);

        // When
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.BC_PARKS_SECTION_CODE, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetBCParksSectionCodesById_ServiceException() throws Exception {
        // Given
        when(codesService.getBCParksSectionCodeById(anyInt())).thenThrow(new ServiceException("Service error"));

        // When
        mockMvc.perform(get("/codes/{codeTable}/{id}", CodeTables.BC_PARKS_SECTION_CODE, "1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isInternalServerError());
    }

}
