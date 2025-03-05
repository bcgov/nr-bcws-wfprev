package ca.bc.gov.nrs.wfprev.data.models;

import java.math.BigDecimal;
import java.util.Date;

import ca.bc.gov.nrs.wfprev.common.validators.ActivityDates;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.server.core.Relation;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@ActivityDates
@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "activity")
@Relation(collectionRelation = "activities")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityModel extends CommonModel<ActivityModel> {

    private String activityGuid;
    private String projectPlanFiscalGuid;
    private ActivityStatusCodeModel activityStatusCode;
    private String silvicultureBaseGuid;
    private String silvicultureTechniqueGuid;
    private String silvicultureMethodGuid;
    private RiskRatingCodeModel riskRatingCode;
    private ContractPhaseCodeModel contractPhaseCode;
    private String activityFundingSourceGuid;
    @NotNull(message = "Activity activityName must not be null")
    private String activityName;
    @NotNull(message = "Activity activityDescription must not be null")
    private String activityDescription;
    @NotNull(message = "Activity activityStartDate must not be null")
    private Date activityStartDate;
    @NotNull(message = "Activity activityEndDate must not be null")
    private Date activityEndDate;
    private BigDecimal plannedSpendAmount;
    private BigDecimal plannedTreatmentAreaHa;
    private BigDecimal reportedSpendAmount;
    private BigDecimal completedAreaHa;
    @NotNull(message = "Activity isResultsReportableInd must not be null")
    private Boolean isResultsReportableInd;
    @NotNull(message = "Activity outstandingObligationsInd must not be null")
    private Boolean outstandingObligationsInd;
    private String activityComment;
    @NotNull(message = "Activity isSpatialAddedInd must not be null")
    private Boolean isSpatialAddedInd;
}