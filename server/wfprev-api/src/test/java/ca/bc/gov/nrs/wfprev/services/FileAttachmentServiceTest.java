package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.FileAttachmentResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FileAttachmentEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.models.FileAttachmentModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.AttachmentContentTypeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.FileAttachmentRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.SourceObjectNameCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileAttachmentServiceTest {

    @Mock
    private FileAttachmentRepository fileAttachmentRepository;

    @Mock
    private FileAttachmentResourceAssembler fileAttachmentResourceAssembler;

    @Mock
    private AttachmentContentTypeCodeRepository attachmentContentTypeCodeRepository;

    @Mock
    private SourceObjectNameCodeRepository sourceObjectNameCodeRepository;

    @Mock
    private ProjectBoundaryRepository projectBoundaryRepository;

    @Mock
    private ActivityBoundaryRepository activityBoundaryRepository;

    @Mock
    private WildfireDocumentManagerService wildfireDocumentManagerService;

    @InjectMocks
    private FileAttachmentService fileAttachmentService;

    private FileAttachmentEntity mockEntity;
    private FileAttachmentModel mockModel;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        mockEntity = new FileAttachmentEntity();
        mockEntity.setFileIdentifier("test-file-id"); // Set a file identifier for testing
        mockModel = new FileAttachmentModel();
        mockModel.setFileAttachmentGuid(testUuid.toString());
    }

    @Test
    void testGetAllProjectAttachments_Success() throws ServiceException {
        UUID projectGuid = UUID.randomUUID();
        UUID projectBoundaryGuid = UUID.randomUUID();

        ProjectBoundaryEntity mockBoundary = new ProjectBoundaryEntity();
        mockBoundary.setProjectBoundaryGuid(projectBoundaryGuid);

        when(projectBoundaryRepository.findByProjectGuid(projectGuid)).thenReturn(List.of(mockBoundary));
        when(fileAttachmentRepository.findAllBySourceObjectUniqueIdIn(List.of(projectBoundaryGuid.toString())))
                .thenReturn(List.of(mockEntity));
        when(fileAttachmentResourceAssembler.toCollectionModel(anyList()))
                .thenReturn(CollectionModel.of(List.of(mockModel)));

        CollectionModel<FileAttachmentModel> result = fileAttachmentService.getAllProjectAttachments(projectGuid.toString());

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetAllActivityAttachments_Success() throws ServiceException {
        UUID activityGuid = UUID.randomUUID();
        UUID activityBoundaryGuid = UUID.randomUUID();

        ActivityBoundaryEntity mockActivityBoundary = new ActivityBoundaryEntity();
        mockActivityBoundary.setActivityBoundaryGuid(activityBoundaryGuid);

        List<String> sourceObjectIds = List.of(activityBoundaryGuid.toString(), activityGuid.toString());

        when(activityBoundaryRepository.findByActivityGuid(activityGuid)).thenReturn(List.of(mockActivityBoundary));
        when(fileAttachmentRepository.findAllBySourceObjectUniqueIdIn(sourceObjectIds)).thenReturn(List.of(mockEntity));
        when(fileAttachmentResourceAssembler.toCollectionModel(anyList()))
                .thenReturn(CollectionModel.of(List.of(mockModel)));

        CollectionModel<FileAttachmentModel> result = fileAttachmentService.getAllActivityAttachments(activityGuid.toString());

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetFileAttachmentById_Success() throws ServiceException {
        when(fileAttachmentRepository.findById(testUuid))
                .thenReturn(Optional.of(mockEntity));
        when(fileAttachmentResourceAssembler.toModel(mockEntity))
                .thenReturn(mockModel);

        FileAttachmentModel result = fileAttachmentService.getFileAttachmentById(testUuid.toString());

        assertNotNull(result);
        assertEquals(testUuid.toString(), result.getFileAttachmentGuid());
    }

    @Test
    void testGetFileAttachmentById_NotFound() {
        when(fileAttachmentRepository.findById(testUuid)).thenReturn(Optional.empty());

        FileAttachmentModel result = fileAttachmentService.getFileAttachmentById(testUuid.toString());

        assertNull(result);
    }

    @Test
    void testGetFileAttachmentById_ServiceException() {
        String validId = "123e4567-e89b-12d3-a456-426614174000";
        when(fileAttachmentRepository.findById(any(UUID.class)))
                .thenThrow(new RuntimeException("Database error"));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            fileAttachmentService.getFileAttachmentById(validId);
        });

        assertEquals("Database error", exception.getMessage());
    }

    @Test
    void testGetFileAttachmentById_returnsNull_whenFileAttachmentNotFound() {
        String validId = "123e4567-e89b-12d3-a456-426614174000";
        when(fileAttachmentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        FileAttachmentModel result = fileAttachmentService.getFileAttachmentById(validId);

        assertNull(result);
    }

    @Test
    void testGetFileAttachmentById_InvalidUUID() {
        String invalidId = "invalid-uuid";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileAttachmentService.getFileAttachmentById(invalidId);
        });

        assertEquals("Invalid UUID: " + invalidId, exception.getMessage());
    }

    @Test
    void testCreateFileAttachment_Success() throws ServiceException {
        when(fileAttachmentResourceAssembler.toEntity(mockModel)).thenReturn(mockEntity);
        when(fileAttachmentRepository.saveAndFlush(mockEntity)).thenReturn(mockEntity);
        when(fileAttachmentResourceAssembler.toModel(mockEntity)).thenReturn(mockModel);

        FileAttachmentModel result = fileAttachmentService.createFileAttachment(mockModel);

        assertNotNull(result);
    }

    @Test
    void testUpdateFileAttachment_Success() {
        when(fileAttachmentRepository.findById(testUuid)).thenReturn(Optional.of(mockEntity));
        when(fileAttachmentResourceAssembler.updateEntity(mockModel, mockEntity)).thenReturn(mockEntity);
        when(fileAttachmentRepository.saveAndFlush(mockEntity)).thenReturn(mockEntity);
        when(fileAttachmentResourceAssembler.toModel(mockEntity)).thenReturn(mockModel);

        FileAttachmentModel result = fileAttachmentService.updateFileAttachment(mockModel);

        assertNotNull(result);
        assertEquals(testUuid.toString(), result.getFileAttachmentGuid());
    }

    @Test
    void testUpdateFileAttachment_NotFound() {
        when(fileAttachmentRepository.findById(testUuid)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                fileAttachmentService.updateFileAttachment(mockModel)
        );

        assertEquals("FileAttachment not found: " + testUuid, exception.getMessage());
    }

    @Test
    void testDeleteFileAttachment_Success() throws ServiceException {
        when(fileAttachmentRepository.findById(testUuid)).thenReturn(Optional.of(mockEntity));
        when(fileAttachmentResourceAssembler.toModel(mockEntity)).thenReturn(mockModel);

        FileAttachmentModel result = fileAttachmentService.deleteFileAttachment(testUuid.toString());

        assertNotNull(result);
        verify(wildfireDocumentManagerService, times(1)).deleteDocument("test-file-id");
        verify(fileAttachmentRepository, times(1)).delete(any(FileAttachmentEntity.class));
    }

    @Test
    void testDeleteFileAttachment_NotFound() {
        when(fileAttachmentRepository.findById(testUuid)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                fileAttachmentService.deleteFileAttachment(testUuid.toString())
        );

        assertEquals("FileAttachment not found: " + testUuid, exception.getMessage());
    }

    @Test
    void testDeleteFileAttachment_RuntimeException() {
        when(fileAttachmentRepository.findById(testUuid)).thenReturn(Optional.of(mockEntity));
        doThrow(new RuntimeException("Unexpected error"))
                .when(fileAttachmentRepository).delete(any(FileAttachmentEntity.class));

        Exception exception = assertThrows(RuntimeException.class, () ->
                fileAttachmentService.deleteFileAttachment(testUuid.toString())
        );

        assertEquals("Unexpected error", exception.getMessage());
    }

    @Test
    void testDeleteFileAttachment_ServiceException() {
        String validId = "123e4567-e89b-12d3-a456-426614174000";

        when(fileAttachmentRepository.findById(UUID.fromString(validId))).thenReturn(Optional.of(mockEntity));
        doThrow(new RuntimeException("Unexpected error during deletion"))
                .when(fileAttachmentRepository).delete(mockEntity);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            fileAttachmentService.deleteFileAttachment(validId);
        });

        assertEquals("Unexpected error during deletion", exception.getMessage());
    }


    @Test
    void testSaveFileAttachment_DataIntegrityViolationException() {
        when(fileAttachmentResourceAssembler.toEntity(mockModel)).thenReturn(mockEntity);
        when(fileAttachmentRepository.saveAndFlush(mockEntity))
                .thenThrow(new DataIntegrityViolationException("Constraint Violation"));

        Exception exception = assertThrows(DataIntegrityViolationException.class, () ->
                fileAttachmentService.createFileAttachment(mockModel)
        );

        assertEquals("Constraint Violation", exception.getMessage());
    }

    @Test
    void testDeleteAttachmentsBySourceObject_Success() {
        String sourceObjectUniqueId = UUID.randomUUID().toString();
        when(fileAttachmentRepository.findAllBySourceObjectUniqueId(sourceObjectUniqueId))
                .thenReturn(List.of(mockEntity));

        fileAttachmentService.deleteAttachmentsBySourceObject(sourceObjectUniqueId);

        verify(wildfireDocumentManagerService, times(1)).deleteDocument("test-file-id");
        verify(fileAttachmentRepository, times(1)).delete(mockEntity);
    }

    @Test
    void testDeleteAttachmentsBySourceObject_NoAttachments() {
        String sourceObjectUniqueId = UUID.randomUUID().toString();
        when(fileAttachmentRepository.findAllBySourceObjectUniqueId(sourceObjectUniqueId))
                .thenReturn(List.of());

        fileAttachmentService.deleteAttachmentsBySourceObject(sourceObjectUniqueId);

        verify(wildfireDocumentManagerService, times(0)).deleteDocument(any());
        verify(fileAttachmentRepository, times(0)).delete(any(FileAttachmentEntity.class));
    }
}
