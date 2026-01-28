package ca.bc.gov.nrs.wfprev.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "project_plan_fiscal_perf")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectPlanFiscalPerfEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "project_plan_fiscal_perf_guid", updatable = false, nullable = false)
    private UUID projectPlanFiscalPerfGuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_plan_fiscal_guid", nullable = false)
    private ProjectFiscalEntity projectFiscal;

    @NotNull
    @Column(name = "submitted_timestamp", nullable = false)
    private Date submittedTimestamp;

    @Column(name = "reporting_period_code", length = 10)
    private String reportingPeriodCode;

    @Column(name = "progress_status_code", length = 10)
    private String progressStatusCode;

    @NotNull
    @Column(name = "plan_fiscal_status_code", length = 10, nullable = false)
    private String planFiscalStatusCode;

    @NotNull
    @Column(name = "submitted_by_name", length = 100, nullable = false)
    private String submittedByName;

    @Column(name = "submitted_by_userid", length = 64)
    private String submittedByUserid;

    @Column(name = "submitted_by_guid", length = 36)
    private String submittedByGuid;

    @Column(name = "previous_forecast_amount", precision = 15, scale = 2)
    private BigDecimal previousForecastAmount;

    @NotNull
    @Column(name = "forecast_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal forecastAmount;

    @NotNull
    @Column(name = "forecast_adjustment_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal forecastAdjustmentAmount;

    @NotNull
    @Column(name = "forecast_adjustment_rationale", length = 4000, nullable = false)
    private String forecastAdjustmentRationale;

    @NotNull
    @Column(name = "general_update_comment", length = 4000, nullable = false)
    private String generalUpdateComment;

    @Column(name = "budget_high_risk_amount", precision = 15, scale = 2)
    private BigDecimal budgetHighRiskAmount;

    @Column(name = "budget_high_risk_rationale", length = 4000)
    private String budgetHighRiskRationale;

    @Column(name = "budget_medium_risk_amount", precision = 15, scale = 2)
    private BigDecimal budgetMediumRiskAmount;

    @Column(name = "budget_medium_risk_rationale", length = 4000)
    private String budgetMediumRiskRationale;

    @Column(name = "budget_low_risk_amount", precision = 15, scale = 2)
    private BigDecimal budgetLowRiskAmount;

    @Column(name = "budget_low_risk_rationale", length = 4000)
    private String budgetLowRiskRationale;

    @NotNull
    @Column(name = "budget_completed_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal budgetCompletedAmount;

    @Column(name = "budget_completed_description", length = 4000)
    private String budgetCompletedDescription;

    @NotNull
    @Column(name = "revision_count", nullable = false)
    private Integer revisionCount;

    @CreatedBy
    @NotNull
    @Column(name = "create_user", length = 64, nullable = false)
    private String createUser;

    @CreatedDate
    @NotNull
    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @LastModifiedBy
    @NotNull
    @Column(name = "update_user", length = 64, nullable = false)
    private String updateUser;

    @LastModifiedDate
    @NotNull
    @Column(name = "update_date", nullable = false)
    private Date updateDate;
}
