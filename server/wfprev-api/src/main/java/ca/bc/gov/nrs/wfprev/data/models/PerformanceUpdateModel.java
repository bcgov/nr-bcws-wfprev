package ca.bc.gov.nrs.wfprev.data.models;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "performanceUpdate")
@Relation(collectionRelation = "performanceUpdate")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceUpdateModel extends CommonModel<PerformanceUpdateModel>{
    
    @NotNull
    private Date submittedTimestamp;
    private String reportingPeriod;
    private String progressStatusCode;
    private String updateGeneralStatus;
    
    private String submittedByUserid;
    private String submittedByGuid;

    @NotNull
    private String generalUpdateComment;

    @NotNull
    private String submittedBy;

    @NotNull
    private BigDecimal forecastAmount;

    @NotNull
    private BigDecimal forecastAdjustmentAmount;
    private BigDecimal previousForecastAmount;

    @NotNull
    private String forecastAdjustmentRationale;


    private BigDecimal budgetHighRiskAmount;
    private String budgetHighRiskRationale;
    
    private BigDecimal budgetMediumRiskAmount;
    private String budgetMediumRiskRationale;

    private BigDecimal budgetLowRiskAmount;
    private String budgetLowRiskRationale;
    
    private BigDecimal budgetCompletedAmount;
    private String budgetCompletedDescription;

    private BigDecimal totalAmount;
}

