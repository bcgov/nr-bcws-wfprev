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
@Table(name = "activity_progress")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityProgressEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "activity_progress_guid", updatable = false, nullable = false)
    private UUID activityProgressGuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_guid", nullable = false)
    private ActivityEntity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_plan_fiscal_perf_guid", nullable = false)
    private ProjectPlanFiscalPerfEntity projectPlanFiscalPerf;

    @NotNull
    @Column(name = "entered_timestamp", nullable = false)
    private Date enteredTimestamp;

    @NotNull
    @Column(name = "activity_status_code", length = 10, nullable = false)
    private String activityStatusCode;

    @Column(name = "contract_phase_code", length = 10)
    private String contractPhaseCode;

    @NotNull
    @Column(name = "risk_rating_code", length = 10, nullable = false)
    private String riskRatingCode;

    @Column(name = "planned_spend_amount", precision = 15, scale = 2)
    private BigDecimal plannedSpendAmount;

    @Column(name = "reported_spend_amount", precision = 15, scale = 2)
    private BigDecimal reportedSpendAmount;

    @Column(name = "completed_area_ha", precision = 15, scale = 4)
    private BigDecimal completedAreaHa;

    @Column(name = "progress_comment", length = 4000)
    private String progressComment;

    @Column(name = "commenter_userid", length = 64)
    private String commenterUserid;

    @Column(name = "commenter_guid", length = 36)
    private String commenterGuid;

    @NotNull
    @Column(name = "system_generated_ind", nullable = false)
    private Boolean systemGeneratedInd;

    @NotNull
    @Column(name = "outstanding_obligations_ind", nullable = false)
    private Boolean outstandingObligationsInd;

    @Column(name = "activity_comment", length = 4000)
    private String activityComment;

    @NotNull
    @Column(name = "revision_count", columnDefinition = "numeric(10) default '0'", nullable = false)
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
