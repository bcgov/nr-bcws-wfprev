package ca.bc.gov.nrs.wfprev.data.assemblers;

import java.util.UUID;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.controllers.ProjectFiscalController;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectPlanFiscalPerfEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForecastAmountsModel;
import ca.bc.gov.nrs.wfprev.data.models.PerformanceUpdateModel;
import ca.bc.gov.nrs.wfprev.services.ForecastAmountCalculatorService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PerformanceUpdateResourceAssembler extends RepresentationModelAssemblerSupport<ProjectPlanFiscalPerfEntity, PerformanceUpdateModel> {
    
    private final ForecastAmountCalculatorService forecastAmountCalculator;

    public PerformanceUpdateResourceAssembler(ForecastAmountCalculatorService forecastAmountCalculator) {
        super(ProjectFiscalController.class, PerformanceUpdateModel.class);
        this.forecastAmountCalculator = forecastAmountCalculator;
    }
    
    @Override
    public PerformanceUpdateModel toModel(ProjectPlanFiscalPerfEntity entity) {
        PerformanceUpdateModel model = new PerformanceUpdateModel();

        model.setSubmittedTimestamp(entity.getSubmittedTimestamp());
        model.setReportingPeriod(entity.getReportingPeriodCode());
        model.setProgressStatusCode(entity.getProgressStatusCode());
        model.setUpdateGeneralStatus(entity.getPlanFiscalStatusCode());

        model.setGeneralUpdateComment(entity.getGeneralUpdateComment());
        model.setSubmittedBy(entity.getSubmittedByName());

        model.setForecastAmount(forecastAmountCalculator.defaultZero(entity.getForecastAmount()));
        model.setForecastAdjustmentAmount(forecastAmountCalculator.defaultZero(entity.getForecastAdjustmentAmount()));
        model.setPreviousForecastAmount(forecastAmountCalculator.defaultZero(entity.getPreviousForecastAmount()));
        model.setForecastAdjustmentRationale(entity.getForecastAdjustmentRationale());

        model.setBudgetHighRiskAmount(forecastAmountCalculator.defaultZero(entity.getBudgetHighRiskAmount()));
        model.setBudgetHighRiskRationale(entity.getBudgetHighRiskRationale());

        model.setBudgetMediumRiskAmount(forecastAmountCalculator.defaultZero(entity.getBudgetMediumRiskAmount()));
        model.setBudgetMediumRiskRationale(entity.getBudgetMediumRiskRationale());

        model.setBudgetLowRiskAmount(forecastAmountCalculator.defaultZero(entity.getBudgetLowRiskAmount()));
        model.setBudgetLowRiskRationale(entity.getBudgetLowRiskRationale());

        model.setBudgetCompletedAmount(forecastAmountCalculator.defaultZero(entity.getBudgetCompletedAmount()));
        model.setBudgetCompletedDescription(entity.getBudgetCompletedDescription());

        model.setTotalAmount(model.getBudgetHighRiskAmount()
        .add(model.getBudgetMediumRiskAmount())
        .add(model.getBudgetLowRiskAmount())
        .add(model.getBudgetCompletedAmount()));

        return model;
    }

    public ProjectPlanFiscalPerfEntity toEntity(PerformanceUpdateModel resource, ProjectFiscalEntity projectFiscalEntity) {
        ProjectPlanFiscalPerfEntity entity = new ProjectPlanFiscalPerfEntity();
        
        entity.setProjectPlanFiscalPerfGuid(UUID.randomUUID());
        entity.setProjectFiscal(projectFiscalEntity);
        entity.setReportingPeriodCode(resource.getReportingPeriod());
        entity.setProgressStatusCode(resource.getProgressStatusCode());
        entity.setPlanFiscalStatusCode(projectFiscalEntity.getPlanFiscalStatusCode().getPlanFiscalStatusCode());
        entity.setSubmittedByName(resource.getSubmittedBy());
        entity.setSubmittedByUserid(resource.getSubmittedByUserid());
        entity.setSubmittedByGuid(resource.getSubmittedByGuid());

        ForecastAmountsModel amounts = forecastAmountCalculator.calculate(
                projectFiscalEntity.getFiscalForecastAmount(),
                resource.getForecastAdjustmentAmount()
        );
        
        entity.setPreviousForecastAmount(forecastAmountCalculator.defaultZero(projectFiscalEntity.getFiscalForecastAmount()));
        entity.setForecastAmount(forecastAmountCalculator.defaultZero(amounts.forecastAmount()));
        entity.setForecastAdjustmentAmount(forecastAmountCalculator.defaultZero(amounts.forecastAdjustmentAmount()));
        entity.setForecastAdjustmentRationale(resource.getForecastAdjustmentRationale());
        entity.setGeneralUpdateComment(resource.getGeneralUpdateComment());
        entity.setBudgetHighRiskAmount(forecastAmountCalculator.defaultZero(resource.getBudgetHighRiskAmount()));
        entity.setBudgetHighRiskRationale(resource.getBudgetHighRiskRationale());
        entity.setBudgetMediumRiskAmount(forecastAmountCalculator.defaultZero(resource.getBudgetMediumRiskAmount()));
        entity.setBudgetMediumRiskRationale(resource.getBudgetMediumRiskRationale());
        entity.setBudgetLowRiskAmount(forecastAmountCalculator.defaultZero(resource.getBudgetLowRiskAmount()));
        entity.setBudgetLowRiskRationale(resource.getBudgetLowRiskRationale());
        entity.setBudgetCompletedAmount(forecastAmountCalculator.defaultZero(resource.getBudgetCompletedAmount()));
        entity.setBudgetCompletedDescription(resource.getBudgetCompletedDescription());
        entity.setRevisionCount(resource.getRevisionCount());
        entity.setCreateUser(resource.getCreateUser());
        entity.setCreateDate(resource.getCreateDate());
        entity.setUpdateUser(resource.getUpdateUser());
        entity.setUpdateDate(resource.getUpdateDate());

        return entity;
    }
}
