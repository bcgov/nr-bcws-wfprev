package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProgramAreaResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProgramAreaEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProgramAreaModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProgramAreaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProgramAreaServiceTest {
    private ProgramAreaService programAreaService;
    private ProgramAreaRepository programAreaRepository;
    private ProgramAreaResourceAssembler programAreaResourceAssembler;

    @BeforeEach
    void setup() {
        programAreaRepository = mock(ProgramAreaRepository.class);
        programAreaResourceAssembler = mock(ProgramAreaResourceAssembler.class);

        programAreaService = new ProgramAreaService(programAreaRepository, programAreaResourceAssembler);
    }

    @Test
    void testGetAllProgramAreas_Success() throws ServiceException {
        // Given
        List<ProgramAreaEntity> entities = new ArrayList<>();
        entities.add(new ProgramAreaEntity());
        entities.add(new ProgramAreaEntity());
        when(programAreaRepository.findAll()).thenReturn(entities);
        when(programAreaResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // When
        CollectionModel<ProgramAreaModel> result = programAreaService.getAllProgramAreas();

        // Then
        assertNotNull(result);
    }

    @Test
    void testGetAllProgramAreas_Exception() {
        // Given
        when(programAreaRepository.findAll()).thenThrow(new RuntimeException("Error fetching program areas"));

        // When
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> programAreaService.getAllProgramAreas()
        );

        // Then
        assertEquals("Error fetching program areas", exception.getMessage());
    }

    @Test
    void testGetProgramAreaById_Success() throws ServiceException {
        // Given
        String exampleId = UUID.randomUUID().toString();
        ProgramAreaEntity entity = new ProgramAreaEntity();
        when(programAreaRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(programAreaResourceAssembler.toModel(entity))
                .thenReturn(new ProgramAreaModel());

        // When
        ProgramAreaModel result = programAreaService.getProgramAreaById(exampleId);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGetProgramAreaById_NotFound() throws ServiceException {
        // Given
        String nonExistentId = UUID.randomUUID().toString();
        when(programAreaRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // When
        ProgramAreaModel result = programAreaService.getProgramAreaById(nonExistentId);

        // Then
        assertNull(result);
    }

    @Test
    void testGetProgramAreaById_Exception() {
        // Given
        String exampleId = UUID.randomUUID().toString();
        when(programAreaRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching program area"));

        // When
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> programAreaService.getProgramAreaById(exampleId)
        );

        // Then
        assertTrue(exception.getMessage().contains("Error fetching program area"));
    }

    @Test
    void testCreateOrUpdateProgramArea_Success() throws ServiceException {
        // Given
        ProgramAreaModel inputModel = new ProgramAreaModel();
        inputModel.setProgramAreaGuid(UUID.randomUUID().toString());
        ProgramAreaEntity savedEntity = new ProgramAreaEntity();
        when(programAreaResourceAssembler.toEntity(inputModel))
                .thenReturn(savedEntity);
        when(programAreaRepository.saveAndFlush(savedEntity))
                .thenReturn(savedEntity);
        when(programAreaResourceAssembler.toModel(savedEntity))
                .thenReturn(inputModel);

        // When
        ProgramAreaModel result = programAreaService.createOrUpdateProgramArea(inputModel);

        // Then
        assertNotNull(result);
        assertEquals(inputModel.getProgramAreaGuid(), result.getProgramAreaGuid());
    }

    @Test
    void testCreateOrUpdateProgramArea_Exception() {
        // Given
        ProgramAreaModel inputModel = new ProgramAreaModel();
        when(programAreaResourceAssembler.toEntity(inputModel))
                .thenThrow(new RuntimeException("Error saving program area"));

        // When
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> programAreaService.createOrUpdateProgramArea(inputModel)
        );

        // Then
        assertTrue(exception.getMessage().contains("Error saving program area"));
    }

    @Test
    void testDeleteProgramArea_Success() throws ServiceException {
        // Given
        String exampleId = UUID.randomUUID().toString();
        ProgramAreaModel model = new ProgramAreaModel();
        model.setProgramAreaGuid(exampleId);
        ProgramAreaEntity entity = new ProgramAreaEntity();
        entity.setProgramAreaGuid(exampleId);
        when(programAreaRepository.findById(exampleId))
                .thenReturn(Optional.of(entity))
                .thenReturn(Optional.empty());
        when(programAreaResourceAssembler.toModel(entity))
                .thenReturn(model);
        when(programAreaResourceAssembler.toEntity(any(ProgramAreaModel.class)))
                .thenReturn(entity);

        // When
        ProgramAreaModel result = programAreaService.deleteProgramArea(exampleId);

        // Then
        verify(programAreaRepository).delete(entity);
        ProgramAreaModel programAreaById = programAreaService.getProgramAreaById(exampleId);
        assertNull(programAreaById);
    }

    @Test
    void testDeleteProgramArea_Exception() {
        // Given
        String exampleId = UUID.randomUUID().toString();
        ProgramAreaModel model = new ProgramAreaModel();
        model.setProgramAreaGuid(exampleId);
        ProgramAreaEntity entity = new ProgramAreaEntity();
        //Mock for getById
        when(programAreaRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(programAreaResourceAssembler.toModel(entity))
                .thenReturn(model);
        //Mock for delete
        when(programAreaResourceAssembler.toEntity(any(ProgramAreaModel.class)))
                .thenReturn(entity);
        doThrow(new RuntimeException("Error deleting program area"))
                .when(programAreaRepository).delete(any(ProgramAreaEntity.class));

        // When
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> programAreaService.deleteProgramArea(exampleId)
        );

        // Then
        assertTrue(exception.getMessage().contains("Error deleting program area"));
    }
}