package ca.bc.gov.nrs.wfprev.data.models;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.springframework.hateoas.server.core.Relation;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
    private String activityName;
    private String activityDescription;
    private Date activityStartDate;
    private Date activityEndDate;
    private BigDecimal plannedSpendAmount;
    private BigDecimal plannedTreatmentAreaHa;
    private BigDecimal reportedSpendAmount;
    private BigDecimal completedAreaHa;
    private Boolean isResultsReportableInd;
    private Boolean outstandingObligationsInd;
    private String activityComment;
    private Boolean isSpatialAddedInd;
    private Integer revisionCount;
    private String createUser;
    private Date createDate;
    private String updateUser;
    private Date updateDate;
}