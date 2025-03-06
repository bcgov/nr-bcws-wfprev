package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.FileAttachmentResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.FileAttachmentEntity;
import ca.bc.gov.nrs.wfprev.data.models.FileAttachmentModel;
import ca.bc.gov.nrs.wfprev.data.repositories.AttachmentContentTypeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.FileAttachmentRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.SourceObjectNameCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileAttachmentServiceTest {

    @Mock
    private FileAttachmentRepository fileAttachmentRepository;

    @Mock
    private FileAttachmentResourceAssembler fileAttachmentResourceAssembler;

    @Mock
    private AttachmentContentTypeCodeRepository attachmentContentTypeCodeRepository;

    @Mock
    private SourceObjectNameCodeRepository sourceObjectNameCodeRepository;

    @InjectMocks
    private FileAttachmentService fileAttachmentService;

    private FileAttachmentEntity mockEntity;
    private FileAttachmentModel mockModel;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        mockEntity = new FileAttachmentEntity();
        mockModel = new FileAttachmentModel();
        mockModel.setFileAttachmentGuid(testUuid.toString());
    }

    @Test
    void getAllFileAttachments_Success() throws ServiceException {
        when(fileAttachmentRepository.findAllBySourceObjectUniqueId("123"))
                .thenReturn(List.of(mockEntity));
        when(fileAttachmentResourceAssembler.toCollectionModel(anyList()))
                .thenReturn(CollectionModel.of(List.of(mockModel)));

        CollectionModel<FileAttachmentModel> result = fileAttachmentService.getAllFileAttachments("123");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getFileAttachmentById_Success() throws ServiceException {
        when(fileAttachmentRepository.findById(testUuid))
                .thenReturn(Optional.of(mockEntity));
        when(fileAttachmentResourceAssembler.toModel(mockEntity))
                .thenReturn(mockModel);

        FileAttachmentModel result = fileAttachmentService.getFileAttachmentById(testUuid.toString());

        assertNotNull(result);
        assertEquals(testUuid.toString(), result.getFileAttachmentGuid());
    }

    @Test
    void getFileAttachmentById_NotFound() {
        when(fileAttachmentRepository.findById(testUuid)).thenReturn(Optional.empty());

        FileAttachmentModel result = fileAttachmentService.getFileAttachmentById(testUuid.toString());

        assertNull(result);
    }

    @Test
    void createFileAttachment_Success() throws ServiceException {
        when(fileAttachmentResourceAssembler.toEntity(mockModel)).thenReturn(mockEntity);
        when(fileAttachmentRepository.saveAndFlush(mockEntity)).thenReturn(mockEntity);
        when(fileAttachmentResourceAssembler.toModel(mockEntity)).thenReturn(mockModel);

        FileAttachmentModel result = fileAttachmentService.createFileAttachment(mockModel);

        assertNotNull(result);
    }

    @Test
    void updateFileAttachment_Success() {
        when(fileAttachmentRepository.findById(testUuid)).thenReturn(Optional.of(mockEntity));
        when(fileAttachmentResourceAssembler.updateEntity(mockModel, mockEntity)).thenReturn(mockEntity);
        when(fileAttachmentRepository.saveAndFlush(mockEntity)).thenReturn(mockEntity);
        when(fileAttachmentResourceAssembler.toModel(mockEntity)).thenReturn(mockModel);

        FileAttachmentModel result = fileAttachmentService.updateFileAttachment(mockModel);

        assertNotNull(result);
        assertEquals(testUuid.toString(), result.getFileAttachmentGuid());
    }

    @Test
    void updateFileAttachment_NotFound() {
        when(fileAttachmentRepository.findById(testUuid)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                fileAttachmentService.updateFileAttachment(mockModel)
        );

        assertEquals("FileAttachment not found: " + testUuid, exception.getMessage());
    }

    @Test
    void deleteFileAttachment_Success() throws ServiceException {
        when(fileAttachmentRepository.findById(testUuid)).thenReturn(Optional.of(mockEntity));
        when(fileAttachmentResourceAssembler.toModel(mockEntity)).thenReturn(mockModel);

        FileAttachmentModel result = fileAttachmentService.deleteFileAttachment(testUuid.toString());

        assertNotNull(result);
        verify(fileAttachmentRepository, times(1)).delete(any(FileAttachmentEntity.class));
    }

    @Test
    void deleteFileAttachment_NotFound() {
        when(fileAttachmentRepository.findById(testUuid)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                fileAttachmentService.deleteFileAttachment(testUuid.toString())
        );

        assertEquals("FileAttachment not found: " + testUuid, exception.getMessage());
    }

    @Test
    void saveFileAttachment_ThrowsDataIntegrityViolationException() {
        when(fileAttachmentResourceAssembler.toEntity(mockModel)).thenReturn(mockEntity);
        when(fileAttachmentRepository.saveAndFlush(mockEntity))
                .thenThrow(new DataIntegrityViolationException("Constraint Violation"));

        Exception exception = assertThrows(DataIntegrityViolationException.class, () ->
                fileAttachmentService.createFileAttachment(mockModel)
        );

        assertEquals("Constraint Violation", exception.getMessage());
    }
}
