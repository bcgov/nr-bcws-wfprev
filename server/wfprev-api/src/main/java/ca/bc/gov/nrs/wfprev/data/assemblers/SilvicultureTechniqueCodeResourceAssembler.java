package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.SilvicultureTechniqueCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.SilvicultureTechniqueCodeModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SilvicultureTechniqueCodeResourceAssembler extends RepresentationModelAssemblerSupport<SilvicultureTechniqueCodeEntity, SilvicultureTechniqueCodeModel> {

    public SilvicultureTechniqueCodeResourceAssembler() {
        super(CodesController.class, SilvicultureTechniqueCodeModel.class);
    }

    public SilvicultureTechniqueCodeEntity toEntity(SilvicultureTechniqueCodeModel model) {
        if (model == null) {
            return null;
        }
        SilvicultureTechniqueCodeEntity entity = new SilvicultureTechniqueCodeEntity();

        entity.setSilvicultureTechniqueGuid(UUID.fromString(model.getSilvicultureTechniqueGuid()));
        entity.setSilvicultureBaseGuid(UUID.fromString(model.getSilvicultureBaseGuid()));
        entity.setSilvicultureTechniqueCode(model.getSilvicultureTechniqueCode());
        entity.setDescription(model.getDescription());
        entity.setRevisionCount(model.getRevisionCount());
        entity.setCreateUser(model.getCreateUser());
        entity.setCreateDate(model.getCreateDate());
        entity.setUpdateUser(model.getUpdateUser());
        entity.setUpdateDate(model.getUpdateDate());

        return entity;
    }

    @Override
    public SilvicultureTechniqueCodeModel toModel(SilvicultureTechniqueCodeEntity entity) {
        SilvicultureTechniqueCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.SILVICULTURE_TECHNIQUE_CODE, String.valueOf(entity.getSilvicultureTechniqueGuid())))
                .withSelfRel());

        model.setSilvicultureTechniqueGuid(String.valueOf(entity.getSilvicultureTechniqueGuid()));
        model.setSilvicultureTechniqueCode(entity.getSilvicultureTechniqueCode());
        model.setSilvicultureTechniqueGuid(String.valueOf(entity.getSilvicultureTechniqueGuid()));
        model.setDescription(entity.getDescription());
        model.setRevisionCount(entity.getRevisionCount());
        model.setCreateUser(entity.getCreateUser());
        model.setCreateDate(entity.getCreateDate());
        model.setUpdateUser(entity.getUpdateUser());
        model.setUpdateDate(entity.getUpdateDate());

        return model;
    }

    @Override
    public CollectionModel<SilvicultureTechniqueCodeModel> toCollectionModel(Iterable<? extends SilvicultureTechniqueCodeEntity> entities) {
        CollectionModel<SilvicultureTechniqueCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.SILVICULTURE_TECHNIQUE_CODE)).withSelfRel());

        return resources;
    }
}
