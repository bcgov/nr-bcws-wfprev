package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaCodeModel;

@Component
public class EvaluationCriteriaCodeResourceAssembler extends RepresentationModelAssemblerSupport<EvaluationCriteriaCodeEntity, EvaluationCriteriaCodeModel> {

    public EvaluationCriteriaCodeResourceAssembler() {
        super(CodesController.class, EvaluationCriteriaCodeModel.class);
    }

    public EvaluationCriteriaCodeEntity toEntity(EvaluationCriteriaCodeModel model) {
        if (model == null) {
            return null;
        }

        EvaluationCriteriaCodeEntity entity = new EvaluationCriteriaCodeEntity();
        entity.setEvaluationCriteriaGuid(model.getEvaluationCriteriaGuid());
        entity.setProjectTypeCode(model.getProjectTypeCode());
        entity.setCriteriaLabel(model.getCriteriaLabel());
        entity.setEvalCriteriaSectCode(model.getEvalCriteriaSectCode());
        entity.setWeightedRank(model.getWeightedRank());
        return entity;
    }

    @Override
    public EvaluationCriteriaCodeModel toModel(EvaluationCriteriaCodeEntity entity) {
        EvaluationCriteriaCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.EVALUATION_CRITERIA_CODE, entity.getEvaluationCriteriaGuid()))
                .withSelfRel());

        model.setEvaluationCriteriaGuid(entity.getEvaluationCriteriaGuid());
        model.setProjectTypeCode(entity.getProjectTypeCode());
        model.setCriteriaLabel(entity.getCriteriaLabel());
        model.setEvalCriteriaSectCode(entity.getEvalCriteriaSectCode());
        model.setWeightedRank(entity.getWeightedRank());

        return model;
    }

    @Override
    public CollectionModel<EvaluationCriteriaCodeModel> toCollectionModel(Iterable<? extends EvaluationCriteriaCodeEntity> entities) {
        CollectionModel<EvaluationCriteriaCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.EVALUATION_CRITERIA_CODE)).withSelfRel());

        return resources;
    }
}
