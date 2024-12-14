package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.GeneralScopeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestAreaCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.GeneralScopeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectStatusCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ForestAreaCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.GeneralScopeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectStatusCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectTypeCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectServiceTest {
    private ProjectService projectService;
    private ProjectRepository projectRepository;
    private ProjectResourceAssembler projectResourceAssembler;
    private ForestAreaCodeRepository forestAreaCodeRepository;
    private ProjectTypeCodeRepository projectTypeCodeRepository;
    private GeneralScopeCodeRepository generalScopeCodeRepository;
    private ProjectStatusCodeRepository projectStatusCodeRepository;

    @BeforeEach
    public void setup() {
        projectRepository = mock(ProjectRepository.class);
        projectResourceAssembler = mock(ProjectResourceAssembler.class);
        forestAreaCodeRepository = mock(ForestAreaCodeRepository.class);
        projectTypeCodeRepository = mock(ProjectTypeCodeRepository.class);
        generalScopeCodeRepository = mock(GeneralScopeCodeRepository.class);
        projectStatusCodeRepository = mock(ProjectStatusCodeRepository.class);

        projectService = new ProjectService(projectRepository, projectResourceAssembler, forestAreaCodeRepository,
                projectTypeCodeRepository, generalScopeCodeRepository, projectStatusCodeRepository);
        setField(projectService, "forestAreaCodeRepository", forestAreaCodeRepository);
        setField(projectService, "projectTypeCodeRepository", projectTypeCodeRepository);
        setField(projectService, "generalScopeCodeRepository", generalScopeCodeRepository);
        setField(projectService, "projectStatusCodeRepository", projectStatusCodeRepository);
    }

    @Test
    public void test_get_all_projects() throws ServiceException {
        // Given
        UUID guid1 = UUID.randomUUID();
        UUID guid2 = UUID.randomUUID();
        UUID programArea1 = UUID.randomUUID();
        UUID programArea2 = UUID.randomUUID();

        List<ProjectEntity> entities = Arrays.asList(
                ProjectEntity.builder()
                        .projectGuid(guid1)
                        .programAreaGuid(programArea1)
                        .projectName("Project 1")
                        .build(),
                ProjectEntity.builder()
                        .projectGuid(guid2)
                        .programAreaGuid(programArea2)
                        .projectName("Project 2")
                        .build()
        );

        List<ProjectModel> models = Arrays.asList(
                ProjectModel.builder()
                        .projectGuid(guid1.toString())
                        .programAreaGuid(programArea1.toString())
                        .projectName("Project 1")
                        .build(),
                ProjectModel.builder()
                        .projectGuid(guid2.toString())
                        .programAreaGuid(programArea2.toString())
                        .projectName("Project 2")
                        .build()
        );

        when(projectRepository.findAll()).thenReturn(entities);
        when(projectResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(models));

        // When
        CollectionModel<ProjectModel> result = projectService.getAllProjects();

        // Then
        assertNotNull(result);
        List<ProjectModel> resultList = new ArrayList<>();
        result.getContent().forEach(resultList::add);
        assertEquals(2, resultList.size());
        verify(projectRepository).findAll();
        verify(projectResourceAssembler).toCollectionModel(entities);
    }

    @Test
    public void test_get_all_projects_with_exception() {
        // Given
        when(projectRepository.findAll()).thenThrow(new RuntimeException("Error fetching projects"));

        // When/Then
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> projectService.getAllProjects()
        );
        assertTrue(exception.getMessage().contains("Error fetching projects"));
    }

    @Test
    public void test_get_project_by_id() {
        // Given
        UUID guid = UUID.randomUUID();
        UUID programArea = UUID.randomUUID();

        ProjectEntity entity = ProjectEntity.builder()
                .projectGuid(guid)
                .programAreaGuid(programArea)
                .projectName("Test Project")
                .build();

        ProjectModel model = ProjectModel.builder()
                .projectGuid(guid.toString())
                .programAreaGuid(programArea.toString())
                .projectName("Test Project")
                .build();

        when(projectRepository.findById(guid)).thenReturn(Optional.of(entity));
        when(projectResourceAssembler.toModel(entity)).thenReturn(model);

        // When
        ProjectModel result = projectService.getProjectById(guid.toString());

        // Then
        assertNotNull(result);
        assertEquals(guid.toString(), result.getProjectGuid());
        assertEquals(programArea.toString(), result.getProgramAreaGuid());
        assertEquals("Test Project", result.getProjectName());
        verify(projectRepository).findById(guid);
        verify(projectResourceAssembler).toModel(entity);
    }

    @Test
    public void test_get_project_by_id_with_exception() {
        // Given
        UUID guid = UUID.randomUUID();
        when(projectRepository.findById(guid)).thenThrow(new RuntimeException("Error fetching project"));

        // When/Then
        String id = guid.toString();
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> projectService.getProjectById(id)
        );
        assertTrue(exception.getMessage().contains("Error fetching project"));
    }

    @Test
    public void testCreate_DataIntegrityViolationException() {
        // Given I am creating a new project
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .siteUnitName("Test Site")
                .projectLead("Test Lead")
                .projectLeadEmailAddress("test@example.com")
                .isMultiFiscalYearProj(false)
                .totalActualProjectSizeHa(BigDecimal.valueOf(100))
                .build();

        // When I call the createOrUpdateProject method ith a duplicate project number combo causing a DataIntegrityViolationException
        when(projectResourceAssembler.toEntity(any())).thenThrow(new DataIntegrityViolationException("Error saving project"));

        //Then I should throw a DataIntegrityViolationException
        assertThrows(
                DataIntegrityViolationException.class,
                () -> projectService.createOrUpdateProject(inputModel)
        );
    }

    @Test
    public void testCreate_activeStatusNotFound() {
        // Given I am creating a new project
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .siteUnitName("Test Site")
                .projectLead("Test Lead")
                .build();

        ProjectEntity savedEntity = new ProjectEntity();
        when(projectResourceAssembler.toEntity(any(ProjectModel.class))).thenReturn(savedEntity);
        when(projectStatusCodeRepository.findById("ACTIVE")).thenReturn(Optional.empty());

        // When I submit a project and the ACTIVE status doesn't exist
        // Then an EntityNotFoundException should be thrown
        assertThrows(ServiceException.class, () -> projectService.createOrUpdateProject(inputModel));
        verify(projectStatusCodeRepository, times(1)).findById("ACTIVE");
        verify(projectRepository, never()).saveAndFlush(any(ProjectEntity.class));
    }

    @Test
    public void testCreate_violatesConstraint() {
        // Given I am creating a new project with a missing required field
        ProjectModel inputModel = ProjectModel.builder()
                // Missing required siteUnitName
                .projectLead("Test Lead")
                .build();

        ProjectEntity entity = new ProjectEntity();
        when(projectResourceAssembler.toEntity(any(ProjectModel.class))).thenReturn(entity);

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Site unit name cannot be null");
        when(violation.getPropertyPath()).thenReturn(PathImpl.createPathFromString("siteUnitName"));
        violations.add(violation);

        // Mock successful lookup of ACTIVE status
        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE")).thenReturn(Optional.of(activeStatus));

        when(projectRepository.saveAndFlush(any(ProjectEntity.class)))
                .thenThrow(new ConstraintViolationException("Site unit name cannot be null", violations));

        // When/Then
        assertThrows(ConstraintViolationException.class, () -> {
            projectService.createOrUpdateProject(inputModel);
        });

        verify(projectRepository, times(1)).saveAndFlush(any(ProjectEntity.class));
        verify(projectResourceAssembler, times(1)).toEntity(any(ProjectModel.class));
    }

    @Test
    public void test_create_new_project_with_null_guid() throws ServiceException {
        // Given
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("New Test Project")
                .siteUnitName("Test Site")
                .programAreaGuid(UUID.randomUUID().toString())
                .totalActualAmount(BigDecimal.valueOf(1000))
                .isMultiFiscalYearProj(false)
                .build(); // No projectGuid set

        ProjectEntity savedEntity = new ProjectEntity();
        when(projectResourceAssembler.toEntity(any(ProjectModel.class))).thenReturn(savedEntity);
        when(projectRepository.saveAndFlush(any(ProjectEntity.class))).thenReturn(savedEntity);
        when(projectResourceAssembler.toModel(any(ProjectEntity.class))).thenReturn(inputModel);

        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));

        // When
        ProjectModel result = projectService.createOrUpdateProject(inputModel);

        // Then
        assertNotNull(result.getProjectGuid(), "ProjectGuid should be generated");
        assertEquals("New Test Project", result.getProjectName(), "Project name should be preserved");
        assertEquals("Test Site", result.getSiteUnitName(), "Site unit name should be preserved");

        // Verify the sequence of operations
        ArgumentCaptor<ProjectModel> modelCaptor = ArgumentCaptor.forClass(ProjectModel.class);
        verify(projectResourceAssembler).toEntity(modelCaptor.capture());

        ProjectModel capturedModel = modelCaptor.getValue();
        assertNotNull(capturedModel.getProjectGuid(), "Generated GUID should be passed to assembler");
        assertTrue(
                capturedModel.getProjectGuid().matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"),
                "GUID should be a valid UUID"
        );

        assertNotNull(capturedModel.getCreateDate(), "Create date should be set");
        verify(projectRepository).saveAndFlush(any(ProjectEntity.class));
        verify(projectResourceAssembler).toModel(any(ProjectEntity.class));
    }


    @Test
    public void test_create_new_project() throws ServiceException {
        // Given
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .siteUnitName("Test Site")
                .projectLead("Test Lead")
                .projectLeadEmailAddress("test@example.com")
                .isMultiFiscalYearProj(false)
                .totalActualProjectSizeHa(BigDecimal.valueOf(100))
                .build();

        ProjectEntity savedEntity = new ProjectEntity();
        when(projectResourceAssembler.toEntity(any())).thenReturn(savedEntity);
        when(projectRepository.saveAndFlush(any())).thenReturn(savedEntity);
        when(projectResourceAssembler.toModel(any())).thenReturn(inputModel);

        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));

        // When
        projectService.createOrUpdateProject(inputModel);

        // Then
        ArgumentCaptor<ProjectModel> modelCaptor = ArgumentCaptor.forClass(ProjectModel.class);
        verify(projectResourceAssembler).toEntity(modelCaptor.capture());

        ProjectModel capturedModel = modelCaptor.getValue();
        assertNotNull("Should generate project GUID", capturedModel.getProjectGuid());
    }

    @Test
    public void test_create_project_with_null_reference_codes() throws ServiceException {
        // Given
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .forestAreaCode(null)         // null ForestAreaCode
                .projectTypeCode(null)        // null ProjectTypeCode
                .generalScopeCode(null)       // null GeneralScopeCode
                .build();

        ProjectEntity savedEntity = new ProjectEntity();
        when(projectResourceAssembler.toEntity(any())).thenReturn(savedEntity);
        when(projectRepository.saveAndFlush(any())).thenReturn(savedEntity);
        when(projectResourceAssembler.toModel(any())).thenReturn(inputModel);

        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));

        // When
        ProjectModel result = projectService.createOrUpdateProject(inputModel);

        // Then
        verify(forestAreaCodeRepository, never()).findById(any());
        verify(projectTypeCodeRepository, never()).findById(any());
        verify(generalScopeCodeRepository, never()).findById(any());
        assertNotNull(result);
    }

    @Test
    public void test_create_project_with_valid_reference_codes() throws ServiceException {
        // Given
        String forestAreaCode = "FAC1";
        String projectTypeCode = "PTC1";
        String generalScopeCode = "GSC1";

        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .forestAreaCode(ForestAreaCodeModel.builder().forestAreaCode(forestAreaCode).build())
                .projectTypeCode(ProjectTypeCodeModel.builder().projectTypeCode(projectTypeCode).build())
                .generalScopeCode(GeneralScopeCodeModel.builder().generalScopeCode(generalScopeCode).build())
                .build();

        ForestAreaCodeEntity forestAreaEntity = ForestAreaCodeEntity.builder().forestAreaCode(forestAreaCode).build();
        ProjectTypeCodeEntity projectTypeEntity = ProjectTypeCodeEntity.builder().projectTypeCode(projectTypeCode).build();
        GeneralScopeCodeEntity generalScopeEntity = GeneralScopeCodeEntity.builder().generalScopeCode(generalScopeCode).build();

        when(forestAreaCodeRepository.findById(forestAreaCode)).thenReturn(Optional.of(forestAreaEntity));
        when(projectTypeCodeRepository.findById(projectTypeCode)).thenReturn(Optional.of(projectTypeEntity));
        when(generalScopeCodeRepository.findById(generalScopeCode)).thenReturn(Optional.of(generalScopeEntity));

        ProjectEntity savedEntity = new ProjectEntity();
        when(projectResourceAssembler.toEntity(any())).thenReturn(savedEntity);
        when(projectRepository.saveAndFlush(any())).thenReturn(savedEntity);
        when(projectResourceAssembler.toModel(any())).thenReturn(inputModel);

        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));

        // When
        ProjectModel result = projectService.createOrUpdateProject(inputModel);

        // Then
        verify(forestAreaCodeRepository).findById(forestAreaCode);
        verify(projectTypeCodeRepository).findById(projectTypeCode);
        verify(generalScopeCodeRepository).findById(generalScopeCode);
        assertNotNull(result);
    }

    @Test
    public void test_update_existing_project() throws ServiceException {
        // Given
        String existingGuid = UUID.randomUUID().toString();
        ProjectModel inputModel = ProjectModel.builder()
                .projectGuid(existingGuid)
                .projectName("Updated Project")
                .siteUnitName("Updated Site")
                .totalActualProjectSizeHa(BigDecimal.valueOf(200))
                .build();

        ProjectEntity savedEntity = new ProjectEntity();
        savedEntity.setProjectName("Test Project");
        savedEntity.setSiteUnitName("Test Site");
        savedEntity.setTotalActualProjectSizeHa(BigDecimal.valueOf(100));
        savedEntity.setProjectGuid(UUID.fromString(existingGuid));
        savedEntity.setProjectStatusCode(ProjectStatusCodeEntity.builder().projectStatusCode("ACTIVE").build());
        savedEntity.setCreateDate(new Date());
        savedEntity.setRevisionCount(0);
        savedEntity.setForestAreaCode(ForestAreaCodeEntity.builder().forestAreaCode("FAC1").build());
        savedEntity.setProjectTypeCode(ProjectTypeCodeEntity.builder().projectTypeCode("PTC1").build());
        savedEntity.setGeneralScopeCode(GeneralScopeCodeEntity.builder().generalScopeCode("GSC1").build());
        savedEntity.setProgramAreaGuid(UUID.randomUUID());
        savedEntity.setForestRegionOrgUnitId(1);
        savedEntity.setForestDistrictOrgUnitId(2);
        savedEntity.setFireCentreOrgUnitId(3);
        savedEntity.setBcParksRegionOrgUnitId(4);
        savedEntity.setBcParksSectionOrgUnitId(5);

        when(projectResourceAssembler.toEntity(any())).thenReturn(savedEntity);
        when(projectRepository.saveAndFlush(any())).thenReturn(savedEntity);
        when(projectResourceAssembler.toModel(any())).thenReturn(inputModel);

        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));
        when(projectRepository.findById(UUID.fromString(existingGuid)))
                .thenReturn(Optional.of(savedEntity));
        // When
        projectService.createOrUpdateProject(inputModel);

        // Then
        ArgumentCaptor<ProjectModel> modelCaptor = ArgumentCaptor.forClass(ProjectModel.class);
        verify(projectResourceAssembler).updateEntity(modelCaptor.capture(), any());

        ProjectModel capturedModel = modelCaptor.getValue();
        assertEquals(existingGuid, capturedModel.getProjectGuid());
    }

    @Test
    public void test_create_project_forest_area_code_entity_not_found() {
        // Given
        ForestAreaCodeModel forestArea = ForestAreaCodeModel.builder()
                .forestAreaCode("INVALID")
                .build();

        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .forestAreaCode(forestArea)
                .build();

        when(forestAreaCodeRepository.findById("INVALID")).thenReturn(Optional.empty());

        // When/Then
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> projectService.createOrUpdateProject(inputModel)
        );
        assertTrue(exception.getMessage().contains("ForestAreaCode not found: INVALID"));
    }

    @Test
    public void test_create_project_with_exception() {
        // Given
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .siteUnitName("Test Site")
                .totalActualProjectSizeHa(BigDecimal.valueOf(100))
                .build();

        when(projectResourceAssembler.toEntity(any())).thenThrow(new RuntimeException("Error saving project"));

        // When/Then
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> projectService.createOrUpdateProject(inputModel)
        );
        assertTrue(exception.getMessage().contains("Error saving project"));
    }

    @Test
    public void test_create_project_with_service_exception() {
        // Given
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .siteUnitName("Test Site")
                .totalActualProjectSizeHa(BigDecimal.valueOf(100))
                .build();

        when(projectResourceAssembler.toEntity(any())).thenThrow(new EntityNotFoundException("Error saving project"));

        // When/Then
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> projectService.createOrUpdateProject(inputModel)
        );
        assertTrue(exception.getMessage().contains("Error saving project"));
    }

    @Test
    void test_delete_project_with_valid_id() {
        // Given
        String existingGuid = UUID.randomUUID().toString();
        ProjectModel inputModel = ProjectModel.builder()
                .projectGuid(existingGuid)
                .projectName("Updated Project")
                .siteUnitName("Updated Site")
                .totalPlannedProjectSizeHa(BigDecimal.valueOf(200))
                .build();

        ProjectEntity savedEntity = new ProjectEntity();
        when(projectResourceAssembler.toEntity(any())).thenReturn(savedEntity);
        when(projectRepository.saveAndFlush(any())).thenReturn(savedEntity);
        when(projectResourceAssembler.toModel(any())).thenReturn(inputModel);
        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));

        // Return savedEntity the first time, then empty the second time
        when(projectRepository.findById(UUID.fromString(existingGuid)))
                .thenReturn(Optional.of(savedEntity))
                .thenReturn(Optional.empty());

        // When
        projectService.deleteProject(existingGuid);
        // Then
        verify(projectRepository).delete(savedEntity);
        ProjectModel projectById = projectService.getProjectById(existingGuid);

        // Assert that the project is no longer retrievable
        assertNull(projectById);
    }

    @Test
    public void test_delete_project_exception() throws ServiceException {
        // Given
        String id = UUID.randomUUID().toString();
        when(projectRepository.findById(UUID.fromString(id)))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> projectService.deleteProject(id));

        // Then
        assertEquals("Database error", exception.getMessage());
        verify(projectRepository, never()).delete(any(ProjectEntity.class));  // Specify the type
    }

    @Test
    public void test_delete_project_with_exception() {
        // Given
        String existingGuid = UUID.randomUUID().toString();
        ProjectModel inputModel = ProjectModel.builder()
                .projectGuid(existingGuid)
                .projectName("Updated Project")
                .siteUnitName("Updated Site")
                .totalPlannedProjectSizeHa(BigDecimal.valueOf(200))
                .build();

        ProjectEntity savedEntity = new ProjectEntity();
        when(projectResourceAssembler.toEntity(any())).thenReturn(savedEntity);
        when(projectRepository.saveAndFlush(any())).thenReturn(savedEntity);
        when(projectResourceAssembler.toModel(any())).thenReturn(inputModel);
        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));
        when(projectRepository.findById(any())).thenReturn(Optional.of(savedEntity));


        // When
        doThrow(new RuntimeException("Error deleting project")).when(projectRepository).delete(savedEntity);

        // Then
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> projectService.deleteProject(existingGuid)
        );
        assertEquals("Error deleting project", exception.getMessage());
    }

    @Test
    void test_set_reference_entities() throws ServiceException {
        // Given
        String testForestAreaCode = "TEST-FAC";
        ForestAreaCodeEntity forestAreaEntity = ForestAreaCodeEntity.builder()
                .forestAreaCode(testForestAreaCode)
                .build();

        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .forestAreaCode(ForestAreaCodeModel.builder()
                        .forestAreaCode(testForestAreaCode)
                        .build())
                .build();

        when(forestAreaCodeRepository.findById(testForestAreaCode))
                .thenReturn(Optional.of(forestAreaEntity));

        ProjectEntity entityToSave = new ProjectEntity();
        when(projectResourceAssembler.toEntity(any())).thenReturn(entityToSave);
        when(projectRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0)); // Return what was passed in
        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));
        // When
        projectService.createOrUpdateProject(inputModel);

        // Then
        ArgumentCaptor<ProjectEntity> captor = ArgumentCaptor.forClass(ProjectEntity.class);
        verify(projectRepository).saveAndFlush(captor.capture());

        ProjectEntity savedEntity = captor.getValue();
        assertNotNull(savedEntity.getForestAreaCode());
        assertEquals(testForestAreaCode, savedEntity.getForestAreaCode().getForestAreaCode());
    }

    @Test
    void test_reference_code_setters() throws ServiceException {
        // Given
        ProjectEntity testEntity = new ProjectEntity(); // Real entity that can have values set

        // Set up the model with all reference codes
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .forestAreaCode(ForestAreaCodeModel.builder().forestAreaCode("FAC1").build())
                .projectTypeCode(ProjectTypeCodeModel.builder().projectTypeCode("PTC1").build())
                .generalScopeCode(GeneralScopeCodeModel.builder().generalScopeCode("GSC1").build())
                .build();

        // Set up the entities that should be found
        ForestAreaCodeEntity forestAreaEntity = new ForestAreaCodeEntity();
        forestAreaEntity.setForestAreaCode("FAC1");

        ProjectTypeCodeEntity projectTypeEntity = new ProjectTypeCodeEntity();
        projectTypeEntity.setProjectTypeCode("PTC1");

        GeneralScopeCodeEntity generalScopeEntity = new GeneralScopeCodeEntity();
        generalScopeEntity.setGeneralScopeCode("GSC1");

        // Mock repository responses
        when(forestAreaCodeRepository.findById("FAC1")).thenReturn(Optional.of(forestAreaEntity));
        when(projectTypeCodeRepository.findById("PTC1")).thenReturn(Optional.of(projectTypeEntity));
        when(generalScopeCodeRepository.findById("GSC1")).thenReturn(Optional.of(generalScopeEntity));

        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));

        // Return a real entity that can have values set
        when(projectResourceAssembler.toEntity(any())).thenReturn(testEntity);
        when(projectRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        // When
        projectService.createOrUpdateProject(inputModel);

        // Then
        ArgumentCaptor<ProjectEntity> entityCaptor = ArgumentCaptor.forClass(ProjectEntity.class);
        verify(projectRepository).saveAndFlush(entityCaptor.capture());

        ProjectEntity capturedEntity = entityCaptor.getValue();
        assertNotNull(capturedEntity.getForestAreaCode(), "Forest Area Code should be set");
        assertEquals("FAC1", capturedEntity.getForestAreaCode().getForestAreaCode());

        assertNotNull(capturedEntity.getProjectTypeCode(), "Project Type Code should be set");
        assertEquals("PTC1", capturedEntity.getProjectTypeCode().getProjectTypeCode());

        assertNotNull(capturedEntity.getGeneralScopeCode(), "General Scope Code should be set");
        assertEquals("GSC1", capturedEntity.getGeneralScopeCode().getGeneralScopeCode());
    }

    @Test
    void test_set_reference_entities_direct() throws ServiceException {
        // Given
        ProjectEntity projectEntity = new ProjectEntity();  // Create a real entity

        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .forestAreaCode(ForestAreaCodeModel.builder()
                        .forestAreaCode("TEST-CODE")
                        .build())
                .build();

        when(projectResourceAssembler.toEntity(any())).thenReturn(projectEntity);

        ForestAreaCodeEntity forestAreaEntity = new ForestAreaCodeEntity();  // Create a real entity
        when(forestAreaCodeRepository.findById("TEST-CODE"))
                .thenReturn(Optional.of(forestAreaEntity));

        when(projectRepository.saveAndFlush(any(ProjectEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));

        // When
        projectService.createOrUpdateProject(inputModel);

        // Then
        ArgumentCaptor<ProjectEntity> captor = ArgumentCaptor.forClass(ProjectEntity.class);
        verify(projectRepository).saveAndFlush(captor.capture());

        ProjectEntity savedEntity = captor.getValue();
        assertSame(forestAreaEntity, savedEntity.getForestAreaCode());
    }

    @Test
    void testGetProject_invalidUUID() {
        // Given
        String invalidGuid = "invalid-uuid";
        when(projectRepository.findById(any())).thenThrow(new IllegalArgumentException("Invalid UUID string: " + invalidGuid));

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projectService.getProjectById(invalidGuid)
        );
        assertEquals("Invalid UUID: invalid-uuid", exception.getMessage());
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}