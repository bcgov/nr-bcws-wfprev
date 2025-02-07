package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.AttachmentContentTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.AttachmentContentTypeCodeModel;

@Component
public class AttachmentContentTypeCodeResourceAssembler extends RepresentationModelAssemblerSupport<AttachmentContentTypeCodeEntity, AttachmentContentTypeCodeModel> {

    public AttachmentContentTypeCodeResourceAssembler() {
        super(CodesController.class, AttachmentContentTypeCodeModel.class);
    }

    public AttachmentContentTypeCodeEntity toEntity(AttachmentContentTypeCodeModel model) {
        if(model == null) {
            return null;
        }
        AttachmentContentTypeCodeEntity entity = new AttachmentContentTypeCodeEntity();

        entity.setAttachmentContentTypeCode(model.getAttachmentContentTypeCode());
        entity.setDescription(model.getDescription());
        entity.setDisplayOrder(model.getDisplayOrder());
        entity.setEffectiveDate(model.getEffectiveDate());
        entity.setExpiryDate(model.getExpiryDate());
        entity.setRevisionCount(model.getRevisionCount());
        entity.setCreateUser(model.getCreateUser());
        entity.setCreateDate(model.getCreateDate());
        entity.setUpdateUser(model.getUpdateUser());
        entity.setUpdateDate(model.getUpdateDate());

        return entity;
    }

    @Override
    public AttachmentContentTypeCodeModel toModel(AttachmentContentTypeCodeEntity entity) {
        AttachmentContentTypeCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.RISK_RATING_CODE, entity.getAttachmentContentTypeCode()))
                .withSelfRel());

        model.setAttachmentContentTypeCode(entity.getAttachmentContentTypeCode());
        model.setDescription(entity.getDescription());
        model.setDisplayOrder(entity.getDisplayOrder());
        model.setEffectiveDate(entity.getEffectiveDate());
        model.setExpiryDate(entity.getExpiryDate());
        model.setRevisionCount(entity.getRevisionCount());
        model.setCreateUser(entity.getCreateUser());
        model.setCreateDate(entity.getCreateDate());
        model.setUpdateUser(entity.getUpdateUser());
        model.setUpdateDate(entity.getUpdateDate());

        return model;
    }

    @Override
    public CollectionModel<AttachmentContentTypeCodeModel> toCollectionModel(Iterable<? extends AttachmentContentTypeCodeEntity> entities)
    {
        CollectionModel<AttachmentContentTypeCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.RISK_RATING_CODE)).withSelfRel());

        return resources;
    }
}
