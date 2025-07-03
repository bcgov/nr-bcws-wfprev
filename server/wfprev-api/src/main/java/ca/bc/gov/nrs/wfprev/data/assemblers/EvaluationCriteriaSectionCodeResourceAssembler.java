package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSectionCodeModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class EvaluationCriteriaSectionCodeResourceAssembler extends RepresentationModelAssemblerSupport<EvaluationCriteriaSectionCodeEntity, EvaluationCriteriaSectionCodeModel> {

    public EvaluationCriteriaSectionCodeResourceAssembler() {
        super(CodesController.class, EvaluationCriteriaSectionCodeModel.class);
    }

    public EvaluationCriteriaSectionCodeEntity toEntity(EvaluationCriteriaSectionCodeModel model) {
        if(model == null) {
            return null;
        }
        EvaluationCriteriaSectionCodeEntity entity = new EvaluationCriteriaSectionCodeEntity();

        entity.setEvaluationCriteriaSectionCode(model.getEvaluationCriteriaSectionCode());
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
    public EvaluationCriteriaSectionCodeModel toModel(EvaluationCriteriaSectionCodeEntity entity) {
        EvaluationCriteriaSectionCodeModel model = instantiateModel(entity);

        model.setEvaluationCriteriaSectionCode(entity.getEvaluationCriteriaSectionCode());
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
