package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.EndorsementCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.EndorsementCodeModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class EndorsementCodeResourceAssembler extends RepresentationModelAssemblerSupport<EndorsementCodeEntity, EndorsementCodeModel> {

    public EndorsementCodeResourceAssembler() {
        super(CodesController.class, EndorsementCodeModel.class);
    }

    public EndorsementCodeEntity toEntity(EndorsementCodeModel model) {
        if(model == null) {
            return null;
        }
        EndorsementCodeEntity entity = new EndorsementCodeEntity();

        entity.setEndorsementCode(model.getEndorsementCode());
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
    public EndorsementCodeModel toModel(EndorsementCodeEntity entity) {
        EndorsementCodeModel model = instantiateModel(entity);

        model.setEndorsementCode(entity.getEndorsementCode());
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

}
