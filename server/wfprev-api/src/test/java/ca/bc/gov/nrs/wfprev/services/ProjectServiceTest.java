package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.SpringSecurityAuditorAware;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.GeneralScopeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ObjectiveTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestAreaCodeModel;
import ca.bc.gov.nrs.wfprev.data.entities.EvalCriteriaSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.models.GeneralScopeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ObjectiveTypeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectStatusCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ForestAreaCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.GeneralScopeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ObjectiveTypeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectStatusCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectTypeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectTypeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.EvalCriteriaSectSummRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.EvalCriteriaSelectedRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.EvalCriteriaSummaryRepository;
import ca.bc.gov.nrs.wfprev.data.entities.EvalCriteriaSectSummEntity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ProjectServiceTest {
    private ProjectService projectService;
    private ProjectRepository projectRepository;
    private ProjectResourceAssembler projectResourceAssembler;
    private ForestAreaCodeRepository forestAreaCodeRepository;
    private ProjectTypeCodeRepository projectTypeCodeRepository;
    private GeneralScopeCodeRepository generalScopeCodeRepository;
    private ProjectStatusCodeRepository projectStatusCodeRepository;
    private ObjectiveTypeCodeRepository objectiveTypeCodeRepository;
    private SpringSecurityAuditorAware springSecurityAuditorAware;
    private ProjectBoundaryService projectBoundaryService;
    private ProjectFiscalService projectFiscalService;
    private EvalCriteriaSummaryRepository evalCriteriaSummaryRepository;
    private EvalCriteriaSectSummRepository evalCriteriaSectSummRepository;
    private EvalCriteriaSelectedRepository evalCriteriaSelectedRepository;

    @BeforeEach
    void setup() {
        projectRepository = mock(ProjectRepository.class);
        projectResourceAssembler = mock(ProjectResourceAssembler.class);
        forestAreaCodeRepository = mock(ForestAreaCodeRepository.class);
        projectTypeCodeRepository = mock(ProjectTypeCodeRepository.class);
        generalScopeCodeRepository = mock(GeneralScopeCodeRepository.class);
        projectStatusCodeRepository = mock(ProjectStatusCodeRepository.class);
        springSecurityAuditorAware = mock(SpringSecurityAuditorAware.class);
        objectiveTypeCodeRepository = mock(ObjectiveTypeCodeRepository.class);
        projectBoundaryService = mock(ProjectBoundaryService.class);
        projectFiscalService = mock(ProjectFiscalService.class);
        evalCriteriaSummaryRepository = mock(EvalCriteriaSummaryRepository.class);
        evalCriteriaSummaryRepository = mock(EvalCriteriaSummaryRepository.class);
        evalCriteriaSectSummRepository = mock(EvalCriteriaSectSummRepository.class);
        evalCriteriaSelectedRepository = mock(EvalCriteriaSelectedRepository.class);

        projectService = new ProjectService(projectRepository, projectResourceAssembler, forestAreaCodeRepository,
                projectTypeCodeRepository, generalScopeCodeRepository, projectStatusCodeRepository, objectiveTypeCodeRepository,
                projectBoundaryService, projectFiscalService, evalCriteriaSummaryRepository, evalCriteriaSectSummRepository, evalCriteriaSelectedRepository);
        setField(projectService, "forestAreaCodeRepository", forestAreaCodeRepository);
        setField(projectService, "projectTypeCodeRepository", projectTypeCodeRepository);
        setField(projectService, "generalScopeCodeRepository", generalScopeCodeRepository);
        setField(projectService, "projectStatusCodeRepository", projectStatusCodeRepository);
        setField(projectService, "objectiveTypeCodeRepository", objectiveTypeCodeRepository);
        setField(projectService, "projectBoundaryService", projectBoundaryService);
        setField(projectService, "projectFiscalService", projectFiscalService);
        setField(projectService, "evalCriteriaSummaryRepository", evalCriteriaSummaryRepository);
        setField(projectService, "evalCriteriaSummaryRepository", evalCriteriaSummaryRepository);
        setField(projectService, "evalCriteriaSectSummRepository", evalCriteriaSectSummRepository);
        setField(projectService, "evalCriteriaSelectedRepository", evalCriteriaSelectedRepository);

        ProjectStatusCodeEntity activeStatus = new ProjectStatusCodeEntity();
        activeStatus.setProjectStatusCode("ACTIVE");
        activeStatus.setDescription("Active");

        when(projectStatusCodeRepository.findById("ACTIVE")).thenReturn(Optional.of(activeStatus));
    }

    @Test
    void test_get_all_projects() throws ServiceException {
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
    void test_get_all_projects_with_exception() {
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
    void test_get_project_by_id() {
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
    void test_get_project_by_id_with_exception() {
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
    void testCreate_DataIntegrityViolationException() {
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

        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));

        //Then I should throw a DataIntegrityViolationException
        assertThrows(
                DataIntegrityViolationException.class,
                () -> projectService.createProject(inputModel)
        );
    }

    @Test
    void testCreate_activeStatusNotFound() {
        // Given I am creating a new project
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .siteUnitName("Test Site")
                .projectLead("Test Lead")
                .build();

        ProjectEntity savedEntity = new ProjectEntity();
        when(projectResourceAssembler.toEntity(any(ProjectModel.class))).thenReturn(savedEntity);
        when(projectStatusCodeRepository.findById("ACTIVE")).thenReturn(Optional.empty());

        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));

        // When I submit a project and the ACTIVE status doesn't exist
        // Then an EntityNotFoundException should be thrown
        assertThrows(EntityNotFoundException.class, () -> projectService.createProject(inputModel));
        verify(projectStatusCodeRepository, times(1)).findById("ACTIVE");
        verify(projectRepository, never()).saveAndFlush(any(ProjectEntity.class));
    }

    @Test
    void testCreate_violatesConstraint() {
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

        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));
        // Mock successful lookup of ACTIVE status
        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE")).thenReturn(Optional.of(activeStatus));

        when(projectRepository.saveAndFlush(any(ProjectEntity.class)))
                .thenThrow(new ConstraintViolationException("Site unit name cannot be null", violations));

        // When/Then
        assertThrows(ConstraintViolationException.class, () -> {
            projectService.createProject(inputModel);
        });

        verify(projectRepository, times(1)).saveAndFlush(any(ProjectEntity.class));
        verify(projectResourceAssembler, times(1)).toEntity(any(ProjectModel.class));
    }

    @Test
    void test_create_new_project_with_null_guid() throws ServiceException {
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
        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));

        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));

        // When
        ProjectModel result = projectService.createProject(inputModel);

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

        verify(projectRepository).saveAndFlush(any(ProjectEntity.class));
        verify(projectResourceAssembler).toModel(any(ProjectEntity.class));
    }


    @Test
    void test_create_new_project() throws ServiceException {
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
        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));

        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));

        // When
        projectService.createProject(inputModel);

        // Then
        ArgumentCaptor<ProjectModel> modelCaptor = ArgumentCaptor.forClass(ProjectModel.class);
        verify(projectResourceAssembler).toEntity(modelCaptor.capture());

        ProjectModel capturedModel = modelCaptor.getValue();
        assertNotNull("Should generate project GUID", capturedModel.getProjectGuid());
    }

    @Test
    void test_create_project_with_null_reference_codes() throws ServiceException {
        // Given
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .forestAreaCode(null)         // null ForestAreaCode
                .projectTypeCode(null)        // null ProjectTypeCode
                .generalScopeCode(null)    // null GeneralScopeCode
                .primaryObjectiveTypeCode(null)
                .secondaryObjectiveTypeCode(null)
                .tertiaryObjectiveTypeCode(null)
                .build();

        ProjectEntity savedEntity = new ProjectEntity();
        when(projectResourceAssembler.toEntity(any())).thenReturn(savedEntity);
        when(projectRepository.saveAndFlush(any())).thenReturn(savedEntity);
        when(projectResourceAssembler.toModel(any())).thenReturn(inputModel);
        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));

        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));

        // When
        ProjectModel result = projectService.createProject(inputModel);

        // Then
        verify(forestAreaCodeRepository, never()).findById(any());
        verify(projectTypeCodeRepository, never()).findById(any());
        verify(generalScopeCodeRepository, never()).findById(any());
        verify(objectiveTypeCodeRepository, never()).findById(any());
        assertNotNull(result);
    }

    @Test
    void test_create_project_with_valid_reference_codes() throws ServiceException {
        // Given
        String forestAreaCode = "FAC1";
        String projectTypeCode = "PTC1";
        String generalScopeCode = "GSC1";
        String primaryObjectiveTypeCode = "PTC1";
        String secondaryObjectiveTypeCode = "PTC2";
        String tertiaryObjectiveTypeCode = "PTC3";

        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .forestAreaCode(ForestAreaCodeModel.builder().forestAreaCode(forestAreaCode).build())
                .projectTypeCode(ProjectTypeCodeModel.builder().projectTypeCode(projectTypeCode).build())
                .generalScopeCode(GeneralScopeCodeModel.builder().generalScopeCode(generalScopeCode).build())
                .primaryObjectiveTypeCode(ObjectiveTypeCodeModel.builder().objectiveTypeCode(primaryObjectiveTypeCode).build())
                .secondaryObjectiveTypeCode(ObjectiveTypeCodeModel.builder().objectiveTypeCode(secondaryObjectiveTypeCode).build())
                .tertiaryObjectiveTypeCode(ObjectiveTypeCodeModel.builder().objectiveTypeCode(tertiaryObjectiveTypeCode).build())
                .build();

        ForestAreaCodeEntity forestAreaEntity = ForestAreaCodeEntity.builder().forestAreaCode(forestAreaCode).build();
        ProjectTypeCodeEntity projectTypeEntity = ProjectTypeCodeEntity.builder().projectTypeCode(projectTypeCode).build();
        GeneralScopeCodeEntity generalScopeEntity = GeneralScopeCodeEntity.builder().generalScopeCode(generalScopeCode).build();
        ObjectiveTypeCodeEntity primaryObjectiveTypeCodeEntity = ObjectiveTypeCodeEntity.builder().objectiveTypeCode(primaryObjectiveTypeCode).build();
        ObjectiveTypeCodeEntity secondaryObjectiveTypeCodeEntity = ObjectiveTypeCodeEntity.builder().objectiveTypeCode(secondaryObjectiveTypeCode).build();
        ObjectiveTypeCodeEntity tertiaryObjectiveTypeCodeEntity = ObjectiveTypeCodeEntity.builder().objectiveTypeCode(tertiaryObjectiveTypeCode).build();

        when(forestAreaCodeRepository.findById(forestAreaCode)).thenReturn(Optional.of(forestAreaEntity));
        when(projectTypeCodeRepository.findById(projectTypeCode)).thenReturn(Optional.of(projectTypeEntity));
        when(generalScopeCodeRepository.findById(generalScopeCode)).thenReturn(Optional.of(generalScopeEntity));
        when(objectiveTypeCodeRepository.findById(primaryObjectiveTypeCode)).thenReturn(Optional.of(primaryObjectiveTypeCodeEntity));
        when(objectiveTypeCodeRepository.findById(secondaryObjectiveTypeCode)).thenReturn(Optional.of(secondaryObjectiveTypeCodeEntity));
        when(objectiveTypeCodeRepository.findById(tertiaryObjectiveTypeCode)).thenReturn(Optional.of(tertiaryObjectiveTypeCodeEntity));

        ProjectEntity savedEntity = new ProjectEntity();
        when(projectResourceAssembler.toEntity(any())).thenReturn(savedEntity);
        when(projectRepository.saveAndFlush(any())).thenReturn(savedEntity);
        when(projectResourceAssembler.toModel(any())).thenReturn(inputModel);

        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));

        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));

        // When
        ProjectModel result = projectService.createProject(inputModel);

        // Then
        verify(forestAreaCodeRepository).findById(forestAreaCode);
        verify(projectTypeCodeRepository).findById(projectTypeCode);
        verify(generalScopeCodeRepository).findById(generalScopeCode);
        verify(objectiveTypeCodeRepository).findById(primaryObjectiveTypeCode);
        verify(objectiveTypeCodeRepository).findById(secondaryObjectiveTypeCode);
        verify(objectiveTypeCodeRepository).findById(tertiaryObjectiveTypeCode);
        assertNotNull(result);
    }

    @Test
    void test_update_existing_project() throws ServiceException {
        // Given
        String existingGuid = UUID.randomUUID().toString();
        ProjectModel inputModel = ProjectModel.builder()
                .projectGuid(existingGuid)
                .projectName("Updated Project")
                .siteUnitName("Updated Site")
                .totalActualProjectSizeHa(BigDecimal.valueOf(200))
                .projectStatusCode(ProjectStatusCodeModel.builder().projectStatusCode("ACTIVE").build())
                .totalActualAmount(BigDecimal.valueOf(1000))
                .forestAreaCode(ForestAreaCodeModel.builder().forestAreaCode("FAC1").build())
                .projectTypeCode(ProjectTypeCodeModel.builder().projectTypeCode("PTC1").build())
                .generalScopeCode(GeneralScopeCodeModel.builder().generalScopeCode("GSC1").build())
                .programAreaGuid(UUID.randomUUID().toString())
                .forestRegionOrgUnitId(1)
                .forestDistrictOrgUnitId(2)
                .fireCentreOrgUnitId(3)
                .bcParksRegionOrgUnitId(4)
                .bcParksSectionOrgUnitId(5)
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
        when(forestAreaCodeRepository.findById("FAC1"))
                .thenReturn(Optional.of(ForestAreaCodeEntity.builder().forestAreaCode("FAC1").build()));
        when(projectTypeCodeRepository.findById("PTC1"))
                .thenReturn(Optional.of(ProjectTypeCodeEntity.builder().projectTypeCode("PTC1").build()));
        when(generalScopeCodeRepository.findById("GSC1"))
                .thenReturn(Optional.of(GeneralScopeCodeEntity.builder().generalScopeCode("GSC1").build()));
        ProjectEntity updatedEntity = new ProjectEntity();
        updatedEntity.setProjectGuid(UUID.fromString(inputModel.getProjectGuid()));
        updatedEntity.setProjectName("Updated Project");
        updatedEntity.setSiteUnitName("Updated Site");
        updatedEntity.setTotalActualProjectSizeHa(BigDecimal.valueOf(200));
        updatedEntity.setProjectStatusCode(ProjectStatusCodeEntity.builder().projectStatusCode("ACTIVE").build());
        updatedEntity.setTotalActualAmount(BigDecimal.valueOf(1000));
        updatedEntity.setForestAreaCode(ForestAreaCodeEntity.builder().forestAreaCode("FAC1").build());
        updatedEntity.setProjectTypeCode(ProjectTypeCodeEntity.builder().projectTypeCode("PTC1").build());
        updatedEntity.setGeneralScopeCode(GeneralScopeCodeEntity.builder().generalScopeCode("GSC1").build());
        updatedEntity.setProgramAreaGuid(UUID.fromString(inputModel.getProgramAreaGuid()));
        updatedEntity.setForestRegionOrgUnitId(1);
        updatedEntity.setForestDistrictOrgUnitId(2);
        updatedEntity.setFireCentreOrgUnitId(3);
        updatedEntity.setBcParksRegionOrgUnitId(4);
        updatedEntity.setBcParksSectionOrgUnitId(5);


        when(projectResourceAssembler.updateEntity(any(ProjectModel.class), any(ProjectEntity.class)))
                .thenReturn(updatedEntity);

        // When
        projectService.updateProject(inputModel);

        // Then
        ArgumentCaptor<ProjectModel> modelCaptor = ArgumentCaptor.forClass(ProjectModel.class);
        verify(projectResourceAssembler).updateEntity(modelCaptor.capture(), any());

        ProjectModel capturedModel = modelCaptor.getValue();
        assertEquals(existingGuid, capturedModel.getProjectGuid());
    }

    @Test
    void test_updateProject_entityNotFoundException() {
        // Given
        String nonExistentGuid = UUID.randomUUID().toString();
        ProjectModel inputModel = ProjectModel.builder()
                .projectGuid(nonExistentGuid)
                .build();

        when(projectRepository.findById(UUID.fromString(nonExistentGuid)))
                .thenReturn(Optional.empty()); // Simulate project not found

        // When / Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectService.updateProject(inputModel)
        );

        assertTrue(exception.getMessage().contains("Project not found"));
        verify(projectRepository).findById(UUID.fromString(nonExistentGuid));
        verify(projectRepository, never()).saveAndFlush(any());
    }

    @Test
    void test_updateProject_dataIntegrityViolationException() {
        // Given
        String existingGuid = UUID.randomUUID().toString();
        ProjectModel inputModel = ProjectModel.builder()
                .projectGuid(existingGuid)
                .projectName("Updated Project")
                .build();

        ProjectEntity existingEntity = new ProjectEntity();
        existingEntity.setProjectGuid(UUID.fromString(existingGuid));
        existingEntity.setProjectName("Old Project");

        when(projectRepository.findById(UUID.fromString(existingGuid)))
                .thenReturn(Optional.of(existingEntity));

        when(projectRepository.saveAndFlush(any(ProjectEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(ProjectStatusCodeEntity.builder().projectStatusCode("ACTIVE").build()));

        when(projectResourceAssembler.updateEntity(any(ProjectModel.class), any(ProjectEntity.class)))
                .thenReturn(existingEntity);

        // When / Then
        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> projectService.updateProject(inputModel)
        );

        assertTrue(exception.getMessage().contains("Data integrity violation"));
        verify(projectRepository).saveAndFlush(any(ProjectEntity.class));
    }

    @Test
    void test_updateProject_constraintViolationException() {
        // Given
        String existingGuid = UUID.randomUUID().toString();
        ProjectStatusCodeModel activeStatusModel = ProjectStatusCodeModel.builder()
                .projectStatusCode("ACTIVE")
                .build();

        ProjectStatusCodeEntity activeStatusEntity = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        ProjectModel inputModel = ProjectModel.builder()
                .projectGuid(existingGuid)
                .projectName("Updated Project")
                .projectStatusCode(activeStatusModel)
                .build();

        ProjectEntity existingEntity = new ProjectEntity();
        existingEntity.setProjectGuid(UUID.fromString(existingGuid));
        existingEntity.setProjectName("Old Project");

        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatusEntity));

        when(projectRepository.findById(UUID.fromString(existingGuid)))
                .thenReturn(Optional.of(existingEntity));

        when(projectResourceAssembler.updateEntity(any(ProjectModel.class), any(ProjectEntity.class)))
                .thenReturn(existingEntity);

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Constraint violation occurred");
        violations.add(violation);



        when(projectRepository.saveAndFlush(any(ProjectEntity.class)))
                .thenThrow(new ConstraintViolationException("Constraint violation occurred", violations));

        // When / Then
        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> projectService.updateProject(inputModel)
        );

        assertTrue(exception.getMessage().contains("Constraint violation occurred"));
        verify(projectRepository).saveAndFlush(any(ProjectEntity.class));
    }

    @Test
    void test_create_project_forest_area_code_entity_not_found() {
        // Given
        ForestAreaCodeModel forestArea = ForestAreaCodeModel.builder()
                .forestAreaCode("INVALID")
                .build();

        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .forestAreaCode(forestArea)
                .build();

        ProjectEntity entity = new ProjectEntity();
        entity.setProjectName("Test Project");
        when(projectResourceAssembler.toEntity(any(ProjectModel.class))).thenReturn(entity);
        when(forestAreaCodeRepository.findById("INVALID")).thenReturn(Optional.empty());
        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> projectService.createProject(inputModel)
        );

        assertTrue(
                ex.getMessage().contains("ForestAreaCode not found: INVALID"),
                "Unexpected message: " + ex.getMessage()
        );
    }

    @Test
    void test_create_project_with_exception() {
        // Given
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .siteUnitName("Test Site")
                .totalActualProjectSizeHa(BigDecimal.valueOf(100))
                .build();

        when(projectResourceAssembler.toEntity(any())).thenThrow(new RuntimeException("Error saving project"));

        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));

        // When/Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> projectService.createProject(inputModel)
        );
        assertTrue(exception.getMessage().contains("Error saving project"));
    }

    @Test
    void test_create_project_with_service_exception() {
        // Given
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Test Project")
                .siteUnitName("Test Site")
                .totalActualProjectSizeHa(BigDecimal.valueOf(100))
                .build();

        when(projectResourceAssembler.toEntity(any())).thenThrow(new EntityNotFoundException("Error saving project"));

        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));

        // When/Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectService.createProject(inputModel)
        );
        assertTrue(exception.getMessage().contains("Error saving project"));
    }

    @Test
    void test_delete_project_with_valid_id() {
        // Given
        UUID existingGuid = UUID.randomUUID();
        UUID fiscalGuid = UUID.randomUUID();
        UUID activityGuid = UUID.randomUUID();

        ProjectEntity savedEntity = new ProjectEntity();
        savedEntity.setProjectGuid(existingGuid);
        
        // Mock behaviors
        when(projectRepository.findById(existingGuid)).thenReturn(Optional.of(savedEntity));
        
        UUID summaryGuid = UUID.randomUUID();
        EvalCriteriaSummaryEntity summary = new EvalCriteriaSummaryEntity();
        summary.setEvalCriteriaSummaryGuid(summaryGuid);
        when(evalCriteriaSummaryRepository.findAllByProject_ProjectGuid(existingGuid))
                .thenReturn(Collections.singletonList(summary));

        UUID sectSummGuid = UUID.randomUUID();
        EvalCriteriaSectSummEntity sectSumm = new EvalCriteriaSectSummEntity();
        sectSumm.setEvalCriteriaSectSummGuid(sectSummGuid);
        when(evalCriteriaSectSummRepository.findAllByEvalCriteriaSummary_EvalCriteriaSummaryGuid(summaryGuid))
                .thenReturn(Collections.singletonList(sectSumm));

        // When
        projectService.deleteProject(existingGuid.toString(), false);

        // Then
        // Verify Service calls
        verify(projectBoundaryService).deleteProjectBoundaries(existingGuid.toString(), false);
        verify(projectFiscalService).deleteProjectFiscals(existingGuid.toString(), false);
        verify(evalCriteriaSectSummRepository).deleteByEvalCriteriaSummary_EvalCriteriaSummaryGuid(summaryGuid);
        verify(evalCriteriaSelectedRepository).deleteByEvalCriteriaSectSumm_EvalCriteriaSectSummGuid(sectSummGuid);
        verify(evalCriteriaSummaryRepository).deleteByProject_ProjectGuid(existingGuid);
        
        // Verify Project deletion
        verify(projectRepository).delete(savedEntity);
    }

    @Test
    void test_delete_project_with_files() {
        // Given
        UUID existingGuid = UUID.randomUUID();
        ProjectEntity savedEntity = new ProjectEntity();
        savedEntity.setProjectGuid(existingGuid);

        // Mock behaviors
        when(projectRepository.findById(existingGuid)).thenReturn(Optional.of(savedEntity));
        
        UUID summaryGuid = UUID.randomUUID();
        EvalCriteriaSummaryEntity summary = new EvalCriteriaSummaryEntity();
        summary.setEvalCriteriaSummaryGuid(summaryGuid);
        when(evalCriteriaSummaryRepository.findAllByProject_ProjectGuid(existingGuid))
                .thenReturn(Collections.singletonList(summary));

        UUID sectSummGuid = UUID.randomUUID();
        EvalCriteriaSectSummEntity sectSumm = new EvalCriteriaSectSummEntity();
        sectSumm.setEvalCriteriaSectSummGuid(sectSummGuid);
        when(evalCriteriaSectSummRepository.findAllByEvalCriteriaSummary_EvalCriteriaSummaryGuid(summaryGuid))
                .thenReturn(Collections.singletonList(sectSumm));

        // When
        projectService.deleteProject(existingGuid.toString(), true);

        // Then
        // Verify Service calls
        verify(projectBoundaryService).deleteProjectBoundaries(existingGuid.toString(), true);
        verify(projectFiscalService).deleteProjectFiscals(existingGuid.toString(), true);
        verify(evalCriteriaSectSummRepository).deleteByEvalCriteriaSummary_EvalCriteriaSummaryGuid(summaryGuid);
        verify(evalCriteriaSelectedRepository).deleteByEvalCriteriaSectSumm_EvalCriteriaSectSummGuid(sectSummGuid);
        verify(evalCriteriaSummaryRepository).deleteByProject_ProjectGuid(existingGuid);

        // Verify Project deletion
        verify(projectRepository).delete(savedEntity);
    }

    @Test
    void test_delete_project_exception() throws ServiceException {
        // Given
        String id = UUID.randomUUID().toString();
        when(projectRepository.findById(UUID.fromString(id)))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> projectService.deleteProject(id, false));

        // Then
        assertEquals("Database error", exception.getMessage());
        verify(projectRepository, never()).delete(any(ProjectEntity.class));  // Specify the type
    }

    @Test
    void test_delete_project_with_exception() {
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
                () -> projectService.deleteProject(existingGuid, false)
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
        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));
        ProjectStatusCodeEntity activeStatus = ProjectStatusCodeEntity.builder()
                .projectStatusCode("ACTIVE")
                .build();
        when(projectStatusCodeRepository.findById("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));
        // When
        projectService.createProject(inputModel);

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

        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));

        // When
        projectService.createProject(inputModel);

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
        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));

        // When
        projectService.createProject(inputModel);

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

    @Test
    void test_save_project_should_throw_validation_exception_for_duplicate_name_on_create() {
        // Given: New project (no projectGuid yet)
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Duplicate Project")
                .build();
        ProjectEntity entity = ProjectEntity.builder()
                .projectName("Duplicate Project")
                .build();

        when(projectRepository.existsByProjectNameIgnoreCase("Duplicate Project")).thenReturn(true);

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            projectService.saveProject(inputModel, entity);
        });

        assertTrue(exception.getMessage().contains("Project name already exists"));
    }

    @Test
    void test_save_project_should_throw_validation_exception_for_duplicate_name_on_update() {
        // Given: Existing project (has projectGuid)
        UUID existingGuid = UUID.randomUUID();
        ProjectModel inputModel = ProjectModel.builder()
                .projectGuid(existingGuid.toString())
                .projectName("Duplicate Project")
                .build();
        ProjectEntity entity = ProjectEntity.builder()
                .projectGuid(existingGuid)
                .projectName("Duplicate Project")
                .build();

        ProjectEntity otherEntityWithSameName = ProjectEntity.builder()
                .projectGuid(UUID.randomUUID()) // Different GUID
                .projectName("Duplicate Project")
                .build();

        when(projectRepository.findByProjectNameIgnoreCase("Duplicate Project"))
                .thenReturn(Collections.singletonList(otherEntityWithSameName));

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            projectService.saveProject(inputModel, entity);
        });

        assertTrue(exception.getMessage().contains("Project name already exists"));
    }

    @Test
    void test_save_project_should_save_successfully_when_no_duplicate_exists() {
        // Given: New project
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("Unique Project")
                .build();
        ProjectEntity entity = ProjectEntity.builder()
                .projectName("Unique Project")
                .build();

        when(projectRepository.existsByProjectNameIgnoreCase("Unique Project")).thenReturn(false);
        when(projectRepository.saveAndFlush(any(ProjectEntity.class))).thenReturn(entity);
        when(projectResourceAssembler.toModel(any(ProjectEntity.class))).thenReturn(inputModel);

        // When
        ProjectModel result = projectService.saveProject(inputModel, entity);

        // Then
        assertNotNull(result);
        verify(projectRepository).saveAndFlush(entity);
        verify(projectResourceAssembler).toModel(entity);
    }

    @Test
        void test_save_project_should_throw_validation_exception_for_duplicate_name_case_insensitive() {
        ProjectEntity otherEntityWithSameName = ProjectEntity.builder()
                .projectGuid(UUID.randomUUID())
                .projectName("duplicate project")
                .build();

        UUID existingGuid = UUID.randomUUID();
        ProjectModel inputModel = ProjectModel.builder()
                .projectGuid(existingGuid.toString())
                .projectName("DUPLICATE PROJECT")
                .build();
        ProjectEntity entity = ProjectEntity.builder()
                .projectGuid(existingGuid)
                .projectName("DUPLICATE PROJECT")
                .build();

        when(projectRepository.findByProjectNameIgnoreCase("DUPLICATE PROJECT"))
                .thenReturn(Collections.singletonList(otherEntityWithSameName));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
                projectService.saveProject(inputModel, entity);
        });

        assertTrue(exception.getMessage().contains("Project name already exists"));
        }

    @Test
    void test_save_project_trims_name_before_duplicate_check_on_create() {
        ProjectModel inputModel = ProjectModel.builder()
                .projectName("  Duplicate Project  ")
                .build();
        ProjectEntity entity = ProjectEntity.builder()
                .projectName("  Duplicate Project  ")
                .build();

        when(projectRepository.existsByProjectNameIgnoreCase("Duplicate Project")).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> projectService.saveProject(inputModel, entity));

        assertTrue(ex.getMessage().contains("Project name already exists"));
        verify(projectRepository).existsByProjectNameIgnoreCase("Duplicate Project"); // trimmed
        verify(projectRepository, never()).saveAndFlush(any());
    }

    @Test
    void test_save_project_update_allows_same_guid_same_name() {
        UUID guid = UUID.randomUUID();

        ProjectModel inputModel = ProjectModel.builder()
                .projectGuid(guid.toString())
                .projectName("Same Name")
                .build();
        ProjectEntity entity = ProjectEntity.builder()
                .projectGuid(guid)
                .projectName("Same Name")
                .build();

        ProjectEntity same = ProjectEntity.builder()
                .projectGuid(guid)
                .projectName("Same Name")
                .build();
        when(projectRepository.findByProjectNameIgnoreCase("Same Name"))
                .thenReturn(Collections.singletonList(same));

        when(projectRepository.saveAndFlush(any(ProjectEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(projectResourceAssembler.toModel(any(ProjectEntity.class))).thenReturn(inputModel);

        ProjectModel result = projectService.saveProject(inputModel, entity);

        assertNotNull(result);
        verify(projectRepository).findByProjectNameIgnoreCase("Same Name");
        verify(projectRepository).saveAndFlush(entity);
        verify(projectResourceAssembler).toModel(entity);
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

    @Test
    void saveProject_throwsIllegalArgument_whenResourceIsNull() {
        ProjectEntity entity = mock(ProjectEntity.class);

        assertThrows(IllegalArgumentException.class, () ->
                projectService.saveProject(null, entity)
        );

        verifyNoInteractions(projectRepository, projectResourceAssembler);
    }

    @Test
    void saveProject_throwsIllegalArgument_whenEntityIsNull() {
        ProjectModel resource = new ProjectModel();

        assertThrows(IllegalArgumentException.class, () ->
                projectService.saveProject(resource, null)
        );

        verifyNoInteractions(projectRepository, projectResourceAssembler);
    }
}