package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.SilvicultureMethodCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.SilvicultureMethodCodeModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SilvicultureMethodCodeResourceAssembler extends RepresentationModelAssemblerSupport<SilvicultureMethodCodeEntity, SilvicultureMethodCodeModel> {

    public SilvicultureMethodCodeResourceAssembler() {
        super(CodesController.class, SilvicultureMethodCodeModel.class);
    }

    public SilvicultureMethodCodeEntity toEntity(SilvicultureMethodCodeModel model) {
        if (model == null) {
            return null;
        }
        SilvicultureMethodCodeEntity entity = new SilvicultureMethodCodeEntity();

        entity.setSilvicultureMethodGuid(UUID.fromString(model.getSilvicultureMethodGuid()));
        entity.setSilvicultureMethodCode(model.getSilvicultureMethodCode());
        entity.setSilvicultureTechniqueGuid(UUID.fromString(model.getSilvicultureTechniqueGuid()));
        entity.setDescription(model.getDescription());
        entity.setRevisionCount(model.getRevisionCount());
        entity.setCreateUser(model.getCreateUser());
        entity.setCreateDate(model.getCreateDate());
        entity.setUpdateUser(model.getUpdateUser());
        entity.setUpdateDate(model.getUpdateDate());

        return entity;
    }

    @Override
    public SilvicultureMethodCodeModel toModel(SilvicultureMethodCodeEntity entity) {
        SilvicultureMethodCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.SILVICULTURE_METHOD_CODE, String.valueOf(entity.getSilvicultureMethodGuid())))
                .withSelfRel());

        model.setSilvicultureMethodGuid(String.valueOf(entity.getSilvicultureMethodGuid()));
        model.setSilvicultureMethodCode(entity.getSilvicultureMethodCode());
        model.setSilvicultureTechniqueGuid(String.valueOf(entity.getSilvicultureTechniqueGuid()));
        model.setDescription(entity.getDescription());
        model.setSystemStartTimestamp(entity.getSystemStartTimestamp());
        model.setSystemEndTimestamp(entity.getSystemEndTimestamp());
        model.setRevisionCount(entity.getRevisionCount());
        model.setCreateUser(entity.getCreateUser());
        model.setCreateDate(entity.getCreateDate());
        model.setUpdateUser(entity.getUpdateUser());
        model.setUpdateDate(entity.getUpdateDate());

        return model;
    }

    @Override
    public CollectionModel<SilvicultureMethodCodeModel> toCollectionModel(Iterable<? extends SilvicultureMethodCodeEntity> entities) {
        CollectionModel<SilvicultureMethodCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.SILVICULTURE_METHOD_CODE)).withSelfRel());

        return resources;
    }
}
