package ca.bc.gov.nrs.wfprev.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
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
@Table(name = "project_plan_fiscal", schema = "wfprev")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectFiscalEntity implements Serializable {

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "project_plan_fiscal_guid", nullable = false, updatable = false)
    private UUID projectPlanFiscalGuid;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "project_guid", referencedColumnName = "project_guid")
    private ProjectEntity project;

    @NotNull
    @Column(name = "activity_category_code", length = 10)
    private String activityCategoryCode;

    @NotNull
    @Column(name = "fiscal_year", precision = 4)
    private BigDecimal fiscalYear;

    @Column(name = "ancillary_funding_source_guid", columnDefinition = "uuid")
    private UUID ancillaryFundingSourceGuid;

    @NotNull
    @Column(name = "project_plan_status_code", length = 10)
    private String projectPlanStatusCode;

    @NotNull
    @Column(name = "plan_fiscal_status_code", length = 10)
    private String planFiscalStatusCode;

    @NotNull
    @Column(name = "proposal_type_code", length = 10)
    private String proposalTypeCode;

    @Column(name = "endorsement_code", length = 10)
    private String endorsementCode;

    @NotNull
    @Column(name = "project_fiscal_name", length = 300)
    private String projectFiscalName;

    @Column(name = "project_fiscal_description", length = 4000)
    private String projectFiscalDescription;

    @Column(name = "business_area_comment", length = 4000)
    private String businessAreaComment;

    @Column(name = "estimated_clwrr_alloc_amount", precision = 15, scale = 2)
    private BigDecimal estimatedClwrrAllocAmount;

    @Column(name = "total_cost_estimate_amount", precision = 15, scale = 2, nullable = false, columnDefinition = "Decimal(15,2) DEFAULT 0")
    private BigDecimal totalCostEstimateAmount;

    @Column(name = "cfs_project_code", length = 25)
    private String cfsProjectCode;

    @Column(name = "fiscal_forecast_amount", precision = 15, scale = 2)
    private BigDecimal fiscalForecastAmount;

    @Column(name = "fiscal_ancillary_fund_amount", precision = 15, scale = 2)
    private BigDecimal fiscalAncillaryFundAmount;

    @Column(name = "fiscal_planned_project_size_ha", precision = 15, scale = 4, nullable = false, columnDefinition = "Decimal(15,4) DEFAULT 0")
    private BigDecimal fiscalPlannedProjectSizeHa;

    @Column(name = "fiscal_planned_cost_per_ha_amt", precision = 15, scale = 2, nullable = false, columnDefinition = "Decimal(15,2) DEFAULT 0")
    private BigDecimal fiscalPlannedCostPerHaAmt;

    @Column(name = "fiscal_reported_spend_amount", precision = 15, scale = 2, nullable = false, columnDefinition = "Decimal(15,2) DEFAULT 0")
    private BigDecimal fiscalReportedSpendAmount;

    @Column(name = "fiscal_actual_amount", precision = 15, scale = 2, nullable = false, columnDefinition = "Decimal(15,2) DEFAULT 0")
    private BigDecimal fiscalActualAmount;

    @Column(name = "fiscal_completed_size_ha", precision = 15, scale = 4)
    private BigDecimal fiscalCompletedSizeHa;

    @Column(name = "fiscal_actual_cost_per_ha_amt", precision = 15, scale = 2, nullable = false, columnDefinition = "Decimal(15,2) DEFAULT 0")
    private BigDecimal fiscalActualCostPerHaAmt;

    @NotNull
    @Column(name = "first_nations_deliv_part_ind", nullable = false)
    private Boolean firstNationsDelivPartInd;

    @NotNull
    @Column(name = "first_nations_engagement_ind", nullable = false)
    private Boolean firstNationsEngagementInd;

    @Column(name = "first_nations_partner", length = 4000)
    private String firstNationsPartner;

    @Column(name = "other_partner", length = 4000)
    private String otherPartner;

    @Column(name = "results_number", length = 50)
    private String resultsNumber;

    @Column(name = "results_opening_id", length = 25)
    private String resultsOpeningId;

    @Column(name = "results_contact_email", length = 100)
    private String resultsContactEmail;

    @Column(name = "submitted_by_name", length = 100)
    private String submittedByName;

    @Column(name = "submitted_by_user_guid", length = 36)
    private String submittedByUserGuid;

    @Column(name = "submitted_by_user_userid", length = 100)
    private String submittedByUserUserid;

    @Column(name = "submission_timestamp")
    private Date submissionTimestamp;

    @Column(name = "endorsement_eval_timestamp")
    private Date endorsementEvalTimestamp;

    @Column(name = "endorser_name", length = 100)
    private String endorserName;

    @Column(name = "endorser_user_guid", length = 100)
    private String endorserUserGuid;

    @Column(name = "endorser_user_userid", length = 100)
    private String endorserUserUserid;

    @Column(name = "endorsement_timestamp")
    private Date endorsementTimestamp;

    @Column(name = "endorsement_comment", length = 4000)
    private String endorsementComment;

    @NotNull
    @Column(name = "is_approved_ind", nullable = false)
    private Boolean isApprovedInd;

    @Column(name = "approver_name", length = 100)
    private String approverName;

    @Column(name = "approver_user_guid", length = 100)
    private String approverUserGuid;

    @Column(name = "approver_user_userid", length = 100)
    private String approverUserUserid;

    @Column(name = "approved_timestamp")
    private Date approvedTimestamp;

    @Column(name = "accomplishments_comment", length = 4000)
    private String accomplishmentsComment;

    @NotNull
    @Column(name = "is_delayed_ind", nullable = false)
    private Boolean isDelayedInd;

    @Column(name = "delay_rationale", length = 4000)
    private String delayRationale;

    @Column(name = "abandoned_rationale", length = 4000)
    private String abandonedRationale;

    @Column(name = "last_progress_update_timestamp")
    private Date lastProgressUpdateTimestamp;

    @NotNull
    @Version
    @Column(name = "revision_count", columnDefinition="Decimal(10) default '0'")
    private Integer revisionCount = 0;

    @CreatedBy
    @NotNull
    @Column(name = "create_user", length = 64)
    private String createUser;

    @CreatedDate
    @NotNull
    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @LastModifiedBy
    @NotNull
    @Column(name = "update_user", length = 64)
    private String updateUser;

    @LastModifiedDate
    @NotNull
    @Column(name = "update_date", nullable = false)
    private Date updateDate;
}