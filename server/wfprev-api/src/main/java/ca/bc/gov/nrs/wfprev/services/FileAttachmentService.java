package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.FileAttachmentResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.AttachmentContentTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FileAttachmentEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.SourceObjectNameCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.FileAttachmentModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.AttachmentContentTypeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.FileAttachmentRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.SourceObjectNameCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class FileAttachmentService implements CommonService {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final FileAttachmentResourceAssembler fileAttachmentResourceAssembler;
    private final AttachmentContentTypeCodeRepository attachmentContentTypeCodeRepository;
    private final SourceObjectNameCodeRepository sourceObjectNameCodeRepository;
    private final ProjectBoundaryRepository projectBoundaryRepository;
    private final ActivityBoundaryRepository activityBoundaryRepository;

    public FileAttachmentService(
            FileAttachmentRepository fileAttachmentRepository,
            FileAttachmentResourceAssembler fileAttachmentResourceAssembler,
            AttachmentContentTypeCodeRepository attachmentContentTypeCodeRepository,
            SourceObjectNameCodeRepository sourceObjectNameCodeRepository,
            ProjectBoundaryRepository projectBoundaryRepository,
            ActivityBoundaryRepository activityBoundaryRepository) {
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.fileAttachmentResourceAssembler = fileAttachmentResourceAssembler;
        this.attachmentContentTypeCodeRepository = attachmentContentTypeCodeRepository;
        this.sourceObjectNameCodeRepository = sourceObjectNameCodeRepository;
        this.projectBoundaryRepository = projectBoundaryRepository;
        this.activityBoundaryRepository = activityBoundaryRepository;
    }

    public CollectionModel<FileAttachmentModel> getAllProjectAttachments(String projectGuid) throws ServiceException {
        try {
            List<ProjectBoundaryEntity> projectBoundaries = projectBoundaryRepository.findByProjectGuid(UUID.fromString(projectGuid));

            List<String> boundaryGuids = new ArrayList<>();
            for (ProjectBoundaryEntity projectBoundary : projectBoundaries) {
                UUID projectBoundaryGuid = projectBoundary.getProjectBoundaryGuid();
                boundaryGuids.add(String.valueOf(projectBoundaryGuid));
            }

            List<FileAttachmentEntity> attachments = fileAttachmentRepository.findAllBySourceObjectUniqueIdIn(boundaryGuids);

            return fileAttachmentResourceAssembler.toCollectionModel(attachments);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<FileAttachmentModel> getAllActivityAttachments(String activityGuid) throws ServiceException {
        try {
            List<ActivityBoundaryEntity> activityBoundaries = activityBoundaryRepository.findByActivityGuid(UUID.fromString(activityGuid));

            List<String> boundaryGuids = new ArrayList<>();
            for (ActivityBoundaryEntity activityBoundary : activityBoundaries) {
                UUID activityBoundaryGuid = activityBoundary.getActivityBoundaryGuid();
                boundaryGuids.add(String.valueOf(activityBoundaryGuid));
            }

            List<FileAttachmentEntity> attachments = fileAttachmentRepository.findAllBySourceObjectUniqueIdIn(boundaryGuids);

            return fileAttachmentResourceAssembler.toCollectionModel(attachments);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public FileAttachmentModel getFileAttachmentById(String id) throws ServiceException {
        try {
            return fileAttachmentRepository.findById(UUID.fromString(id))
                    .map(fileAttachmentResourceAssembler::toModel)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID: " + id, e);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }


    @Transactional
    public FileAttachmentModel createFileAttachment(FileAttachmentModel resource) throws ServiceException {
        initializeNewFileAttachment(resource);
        FileAttachmentEntity entity = fileAttachmentResourceAssembler.toEntity(resource);
        return saveFileAttachment(resource, entity);
    }

    @Transactional
    public FileAttachmentModel updateFileAttachment(FileAttachmentModel resource) {
        UUID guid = UUID.fromString(resource.getFileAttachmentGuid());
        FileAttachmentEntity existingEntity = fileAttachmentRepository.findById(guid)
                .orElseThrow(() -> new EntityNotFoundException("FileAttachment not found: " + resource.getFileAttachmentGuid()));

        FileAttachmentEntity entity = fileAttachmentResourceAssembler.updateEntity(resource, existingEntity);
        return saveFileAttachment(resource, entity);
    }

    private FileAttachmentModel saveFileAttachment(FileAttachmentModel resource, FileAttachmentEntity entity) {
        try {
            assignAssociatedEntities(resource, entity);

            FileAttachmentEntity savedEntity = fileAttachmentRepository.saveAndFlush(entity);
            return fileAttachmentResourceAssembler.toModel(savedEntity);
        } catch (EntityNotFoundException e) {
            throw new ServiceException("Invalid reference data: " + e.getMessage(), e);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw e;
        }
    }
    
    private void initializeNewFileAttachment(FileAttachmentModel resource) {
        resource.setFileAttachmentGuid(UUID.randomUUID().toString());
        resource.setRevisionCount(0);
    }
    
    private void assignAssociatedEntities(FileAttachmentModel resource, FileAttachmentEntity entity) {
        if (resource.getAttachmentContentTypeCode() != null && resource.getAttachmentContentTypeCode().getAttachmentContentTypeCode() != null) {
            entity.setAttachmentContentTypeCode(loadAttachmentContentTypeCode(resource.getAttachmentContentTypeCode().getAttachmentContentTypeCode()));
        }

        if (resource.getSourceObjectNameCode() != null && resource.getSourceObjectNameCode().getSourceObjectNameCode() != null) {
            entity.setSourceObjectNameCode(loadSourceObjectNameCode(resource.getSourceObjectNameCode().getSourceObjectNameCode()));
        }
    }

    private AttachmentContentTypeCodeEntity loadAttachmentContentTypeCode(String attachmentContentTypeCode) {
        return attachmentContentTypeCodeRepository
                .findById(attachmentContentTypeCode)
                .orElseThrow(() -> new IllegalArgumentException("AttachmentContentTypeCode not found: " + attachmentContentTypeCode));
    }

    private SourceObjectNameCodeEntity loadSourceObjectNameCode(String sourceObjectNameCode) {
        return sourceObjectNameCodeRepository
                .findById(sourceObjectNameCode)
                .orElseThrow(() -> new EntityNotFoundException("SourceObjectNameCode not found: " + sourceObjectNameCode));
    }

    @Transactional
    public FileAttachmentModel deleteFileAttachment(String id) throws ServiceException {
        try {
            FileAttachmentEntity entity = fileAttachmentRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new EntityNotFoundException("FileAttachment not found: " + id));

            FileAttachmentModel model = fileAttachmentResourceAssembler.toModel(entity);

            fileAttachmentRepository.delete(entity);

            return model;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }
}