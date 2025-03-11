package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.ProjectAttachmentController;
import ca.bc.gov.nrs.wfprev.data.entities.AttachmentContentTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FileAttachmentEntity;
import ca.bc.gov.nrs.wfprev.data.entities.SourceObjectNameCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.AttachmentContentTypeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.FileAttachmentModel;
import ca.bc.gov.nrs.wfprev.data.models.SourceObjectNameCodeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class FileAttachmentResourceAssembler extends RepresentationModelAssemblerSupport<FileAttachmentEntity, FileAttachmentModel> {

    public FileAttachmentResourceAssembler() {
        super(ProjectAttachmentController.class, FileAttachmentModel.class);
    }

    public FileAttachmentEntity toEntity(FileAttachmentModel model) {
        FileAttachmentEntity entity = new FileAttachmentEntity();

        entity.setFileAttachmentGuid(UUID.fromString(model.getFileAttachmentGuid()));
        if(model.getSourceObjectNameCode() != null) {
            entity.setSourceObjectNameCode(toSourceObjectNameCodeEntity(model.getSourceObjectNameCode()));
        }
        if(model.getAttachmentContentTypeCode() != null) {
            entity.setAttachmentContentTypeCode(toAttachmentContentTypeCodeEntity(model.getAttachmentContentTypeCode()));
        }
        entity.setSourceObjectUniqueId(model.getSourceObjectUniqueId());
        entity.setDocumentPath(model.getDocumentPath());
        entity.setFileIdentifier(model.getFileIdentifier());
        entity.setWildfireYear(model.getWildfireYear());
        entity.setAttachmentDescription(model.getAttachmentDescription());
        entity.setAttachmentReadOnlyInd(model.getAttachmentReadOnlyInd());
        entity.setUploadedByUserType(model.getUploadedByUserType());
        entity.setUploadedByUserId(model.getUploadedByUserId());
        entity.setUploadedByUserGuid(model.getUploadedByUserGuid());
        entity.setUploadedByTimestamp(model.getUploadedByTimestamp());
        entity.setRevisionCount(model.getRevisionCount());
        entity.setCreateUser(model.getCreateUser());
        entity.setCreateDate(model.getCreateDate());
        entity.setUpdateUser(model.getUpdateUser());
        entity.setUpdateDate(model.getUpdateDate());

        return entity;
    }

    @Override
    public FileAttachmentModel toModel(FileAttachmentEntity entity) {
        FileAttachmentModel model = instantiateModel(entity);

        model.setFileAttachmentGuid(entity.getFileAttachmentGuid().toString());
        if(entity.getSourceObjectNameCode() != null){
            model.setSourceObjectNameCode(toSourceObjectNameCodeModel(entity.getSourceObjectNameCode()));
        }
        if(entity.getAttachmentContentTypeCode() != null){
            model.setAttachmentContentTypeCode(toAttachmentContentTypeCodeModel(entity.getAttachmentContentTypeCode()));
        }
        model.setSourceObjectUniqueId(entity.getSourceObjectUniqueId());
        model.setDocumentPath(entity.getDocumentPath());
        model.setFileIdentifier(entity.getFileIdentifier());
        model.setWildfireYear(entity.getWildfireYear());
        model.setAttachmentDescription(entity.getAttachmentDescription());
        model.setAttachmentReadOnlyInd(entity.getAttachmentReadOnlyInd());
        model.setUploadedByUserType(entity.getUploadedByUserType());
        model.setUploadedByUserId(entity.getUploadedByUserId());
        model.setUploadedByUserGuid(entity.getUploadedByUserGuid());
        model.setUploadedByTimestamp(entity.getUploadedByTimestamp());
        model.setRevisionCount(entity.getRevisionCount());
        model.setCreateUser(entity.getCreateUser());
        model.setCreateDate(entity.getCreateDate());
        model.setUpdateUser(entity.getUpdateUser());
        model.setUpdateDate(entity.getUpdateDate());

        return model;
    }

    @Override
    public CollectionModel<FileAttachmentModel> toCollectionModel(Iterable<? extends FileAttachmentEntity> entities) {
        return super.toCollectionModel(entities);
    }

    private SourceObjectNameCodeEntity toSourceObjectNameCodeEntity(SourceObjectNameCodeModel code) {
        if (code == null) return null;
        SourceObjectNameCodeResourceAssembler ra = new SourceObjectNameCodeResourceAssembler();
        return ra.toEntity(code);
    }

    private SourceObjectNameCodeModel toSourceObjectNameCodeModel(SourceObjectNameCodeEntity code) {
        if (code == null) return null;
        SourceObjectNameCodeResourceAssembler ra = new SourceObjectNameCodeResourceAssembler();
        return ra.toModel(code);
    }

    private AttachmentContentTypeCodeEntity toAttachmentContentTypeCodeEntity(AttachmentContentTypeCodeModel code) {
        AttachmentContentTypeCodeResourceAssembler ra = new AttachmentContentTypeCodeResourceAssembler();
        return ra.toEntity(code);
    }

    private AttachmentContentTypeCodeModel toAttachmentContentTypeCodeModel(AttachmentContentTypeCodeEntity code) {
        AttachmentContentTypeCodeResourceAssembler ra = new AttachmentContentTypeCodeResourceAssembler();
        return ra.toModel(code);
    }

    public FileAttachmentEntity updateEntity(FileAttachmentModel model, FileAttachmentEntity existingEntity) {
        log.debug(">> updateEntity");

        FileAttachmentEntity entity = new FileAttachmentEntity();
        entity.setFileAttachmentGuid(existingEntity.getFileAttachmentGuid());
        entity.setSourceObjectUniqueId(nonNullOrDefault(model.getSourceObjectUniqueId(), existingEntity.getSourceObjectUniqueId()));
        entity.setDocumentPath(nonNullOrDefault(model.getDocumentPath(), existingEntity.getDocumentPath()));
        entity.setFileIdentifier(nonNullOrDefault(model.getFileIdentifier(), existingEntity.getFileIdentifier()));
        entity.setWildfireYear(nonNullOrDefault(model.getWildfireYear(), existingEntity.getWildfireYear()));
        entity.setAttachmentDescription(nonNullOrDefault(model.getAttachmentDescription(), existingEntity.getAttachmentDescription()));
        entity.setAttachmentReadOnlyInd(nonNullOrDefault(model.getAttachmentReadOnlyInd(), existingEntity.getAttachmentReadOnlyInd()));
        entity.setUploadedByUserType(nonNullOrDefault(model.getUploadedByUserType(), existingEntity.getUploadedByUserType()));
        entity.setUploadedByUserId(nonNullOrDefault(model.getUploadedByUserId(), existingEntity.getUploadedByUserId()));
        entity.setUploadedByUserGuid(nonNullOrDefault(model.getUploadedByUserGuid(), existingEntity.getUploadedByUserGuid()));
        entity.setUploadedByTimestamp(nonNullOrDefault(model.getUploadedByTimestamp(), existingEntity.getUploadedByTimestamp()));
        entity.setRevisionCount(nonNullOrDefault(model.getRevisionCount(), existingEntity.getRevisionCount()));
        entity.setCreateUser(existingEntity.getCreateUser());
        entity.setCreateDate(existingEntity.getCreateDate());
        entity.setUpdateUser(nonNullOrDefault(model.getUpdateUser(), existingEntity.getUpdateUser()));
        entity.setUpdateDate(nonNullOrDefault(model.getUpdateDate(), existingEntity.getUpdateDate()));

        if (model.getSourceObjectNameCode() != null) {
            entity.setSourceObjectNameCode(toSourceObjectNameCodeEntity(model.getSourceObjectNameCode()));
        } else {
            entity.setSourceObjectNameCode(existingEntity.getSourceObjectNameCode());
        }

        if (model.getAttachmentContentTypeCode() != null) {
            entity.setAttachmentContentTypeCode(toAttachmentContentTypeCodeEntity(model.getAttachmentContentTypeCode()));
        } else {
            entity.setAttachmentContentTypeCode(existingEntity.getAttachmentContentTypeCode());
        }

        log.debug("Updated entity: {}", entity);
        return entity;
    }

    private <T> T nonNullOrDefault(T newValue, T existingValue) {
        return newValue != null ? newValue : existingValue;
    }
}
