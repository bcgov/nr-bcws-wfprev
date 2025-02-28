package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.SilvicultureBaseCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.SilvicultureBaseCodeModel;

import java.util.UUID;

@Component
public class SilvicultureBaseCodeResourceAssembler extends RepresentationModelAssemblerSupport<SilvicultureBaseCodeEntity, SilvicultureBaseCodeModel> {

    public SilvicultureBaseCodeResourceAssembler() {
        super(CodesController.class, SilvicultureBaseCodeModel.class);
    }

    public SilvicultureBaseCodeEntity toEntity(SilvicultureBaseCodeModel model) {
        if(model == null) {
            return null;
        }
        SilvicultureBaseCodeEntity entity = new SilvicultureBaseCodeEntity();

        entity.setSilvicultureBaseCode(model.getSilvicultureBaseCode());
        entity.setSilvicultureBaseGuid(UUID.fromString(model.getSilvicultureBaseGuid()));
        entity.setProjectTypeCode(entity.getProjectTypeCode());
        entity.setDescription(model.getDescription());
        entity.setRevisionCount(model.getRevisionCount());
        entity.setCreateUser(model.getCreateUser());
        entity.setCreateDate(model.getCreateDate());
        entity.setUpdateUser(model.getUpdateUser());
        entity.setUpdateDate(model.getUpdateDate());

        return entity;
    }

    @Override
    public SilvicultureBaseCodeModel toModel(SilvicultureBaseCodeEntity entity) {
        SilvicultureBaseCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.SILVICULTURE_BASE_CODE, String.valueOf(entity.getSilvicultureBaseGuid())))
                .withSelfRel());

        model.setSilvicultureBaseCode(entity.getSilvicultureBaseCode());
        model.setSilvicultureBaseGuid(String.valueOf(entity.getSilvicultureBaseGuid()));
        model.setProjectTypeCode(entity.getProjectTypeCode());
        model.setDescription(entity.getDescription());
        model.setRevisionCount(entity.getRevisionCount());
        model.setCreateUser(entity.getCreateUser());
        model.setCreateDate(entity.getCreateDate());
        model.setUpdateUser(entity.getUpdateUser());
        model.setUpdateDate(entity.getUpdateDate());

        return model;
    }

    @Override
    public CollectionModel<SilvicultureBaseCodeModel> toCollectionModel(Iterable<? extends SilvicultureBaseCodeEntity> entities)
    {
        CollectionModel<SilvicultureBaseCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.SILVICULTURE_BASE_CODE)).withSelfRel());

        return resources;
    }
}
