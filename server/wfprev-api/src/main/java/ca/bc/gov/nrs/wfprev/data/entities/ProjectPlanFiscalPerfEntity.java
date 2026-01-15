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
    @Column(name = "progress_comment", length = 4000, nullable = false)
    private String progressComment;

    @NotNull
    @Column(name = "is_delayed_ind", nullable = false)
    private Boolean isDelayedInd;

    @Column(name = "delay_rationale", length = 4000)
    private String delayRationale;

    @NotNull
    @Column(name = "revision_count", columnDefinition = "numeric(10) default '0'", nullable = false)
    private Integer revisionCount;
}
