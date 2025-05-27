package ca.bc.gov.nrs.wfprev.data.models;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "projectFiscal")
@Relation(collectionRelation = "projectFiscals")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectFiscalModel extends CommonModel<ProjectFiscalModel> {
    private String projectPlanFiscalGuid;
    private String projectGuid;
    private String activityCategoryCode;
    private Long fiscalYear;
    private String ancillaryFundingProvider;
    private String projectPlanStatusCode;
    private String planFiscalStatusCode;
    private String proposalTypeCode;
    private String endorsementCode;
    private String projectFiscalName;
    private String projectFiscalDescription;
    private String businessAreaComment;
    private BigDecimal estimatedClwrrAllocAmount;
    private BigDecimal totalCostEstimateAmount;
    private String cfsProjectCode;
    private BigDecimal fiscalForecastAmount;
    private BigDecimal fiscalAncillaryFundAmount;
    private BigDecimal fiscalPlannedProjectSizeHa;
    private BigDecimal fiscalPlannedCostPerHaAmt;
    private BigDecimal fiscalReportedSpendAmount;
    private BigDecimal fiscalActualAmount;
    private BigDecimal fiscalCompletedSizeHa;
    private BigDecimal fiscalActualCostPerHaAmt;
    private Boolean firstNationsDelivPartInd;
    private Boolean firstNationsEngagementInd;
    private String firstNationsPartner;
    private String otherPartner;
    private String resultsNumber;
    private String resultsOpeningId;
    private String resultsContactEmail;
    private String submittedByName;
    private String submittedByUserGuid;
    private String submittedByUserUserid;
    private Date submissionTimestamp;
    private Date endorsementEvalTimestamp;
    private String endorserName;
    private String endorserUserGuid;
    private String endorserUserUserid;
    private Date endorsementTimestamp;
    private String endorsementComment;
    private Boolean isApprovedInd;
    private String approverName;
    private String approverUserGuid;
    private String approverUserUserid;
    private Date approvedTimestamp;
    private String accomplishmentsComment;
    private Boolean isDelayedInd;
    private String delayRationale;
    private String abandonedRationale;
    private Date lastProgressUpdateTimestamp;
}