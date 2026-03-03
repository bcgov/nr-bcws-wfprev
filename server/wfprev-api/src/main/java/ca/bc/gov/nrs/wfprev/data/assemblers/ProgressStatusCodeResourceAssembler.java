package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.ProgressStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProgressStatusCodeModel;

@Component
public class ProgressStatusCodeResourceAssembler
        extends RepresentationModelAssemblerSupport<ProgressStatusCodeEntity, ProgressStatusCodeModel> {

    public ProgressStatusCodeResourceAssembler() {
        super(CodesController.class, ProgressStatusCodeModel.class);
    }

    @Override
    public ProgressStatusCodeModel toModel(ProgressStatusCodeEntity entity) {
        return ProgressStatusCodeModel.builder()
                .progressStatusCode(entity.getProgressStatusCode())
                .description(entity.getDescription())
                .displayOrder(entity.getDisplayOrder())
                .effectiveDate(entity.getEffectiveDate())
                .expiryDate(entity.getExpiryDate())
                .revisionCount(entity.getRevisionCount())
                .createDate(entity.getCreateDate())
                .updateUser(entity.getUpdateUser())
                .updateDate(entity.getUpdateDate())
                .build();
    }

    @Override
    public CollectionModel<ProgressStatusCodeModel> toCollectionModel(
            Iterable<? extends ProgressStatusCodeEntity> entities) {

        CollectionModel<ProgressStatusCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.PROGRESS_STATUS_CODE)).withSelfRel());

        return resources;
    }

}
