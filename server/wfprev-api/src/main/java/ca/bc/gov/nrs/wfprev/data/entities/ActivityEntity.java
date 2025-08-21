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
import jakarta.persistence.Version;
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
@Table(name = "activity")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "activity_guid")
    private UUID activityGuid;

    @Column(name = "project_plan_fiscal_guid", columnDefinition = "uuid")
    private UUID projectPlanFiscalGuid;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "activity_status_code")
    private ActivityStatusCodeEntity activityStatusCode;

    @Column(name = "silviculture_base_guid", columnDefinition = "uuid")
    private UUID silvicultureBaseGuid;

    @Column(name = "silviculture_technique_guid", columnDefinition = "uuid")
    private UUID silvicultureTechniqueGuid;

    @Column(name = "silviculture_method_guid", columnDefinition = "uuid")
    private UUID silvicultureMethodGuid;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "risk_rating_code")
    private RiskRatingCodeEntity riskRatingCode;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "contract_phase_code")
    private ContractPhaseCodeEntity contractPhaseCode;

    @Column(name = "activity_funding_source_guid", columnDefinition = "uuid")
    private UUID activityFundingSourceGuid;

    @NotNull
    @Column(name = "activity_name", length = 4000, nullable = false)
    private String activityName;

    @NotNull
    @Column(name = "activity_description", length = 4000, nullable = false)
    private String activityDescription;

    @NotNull
    @Column(name = "activity_start_date", nullable = false)
    private Date activityStartDate;

    @NotNull
    @Column(name = "activity_end_date", nullable = false)
    private Date activityEndDate;

    @Column(name = "planned_spend_amount", precision = 15, scale = 2)
    private BigDecimal plannedSpendAmount;

    @Column(name = "planned_treatment_area_ha", precision = 15, scale = 4)
    private BigDecimal plannedTreatmentAreaHa;

    @Column(name = "reported_spend_amount", precision = 15, scale = 2)
    private BigDecimal reportedSpendAmount;

    @Column(name = "completed_area_ha", precision = 15, scale = 4)
    private BigDecimal completedAreaHa;

    @NotNull
    @Column(name = "is_results_reportable_ind", nullable = false)
    private Boolean isResultsReportableInd;

    @NotNull
    @Column(name = "outstanding_obligations_ind", nullable = false)
    private Boolean outstandingObligationsInd;

    @Column(name = "activity_comment", length = 4000)
    private String activityComment;

    @NotNull
    @Column(name = "is_spatial_added_ind", nullable = false)
    private Boolean isSpatialAddedInd;

    @NotNull
    @Column(name = "revision_count", columnDefinition = "Decimal(10) default '0'")
    @Version
    private Integer revisionCount;

    @CreatedBy
    @NotNull
    @Column(name = "create_user", length = 64)
    private String createUser;

    @CreatedDate
    @NotNull
    @Column(name = "create_date")
    private Date createDate;

    @LastModifiedBy
    @NotNull
    @Column(name = "update_user", length = 64)
    private String updateUser;

    @LastModifiedDate
    @NotNull
    @Column(name = "update_date")
    private Date updateDate;

    @NotNull
    @Column(name = "last_updated_timestamp", nullable = false)
    private Date lastUpdatedTimestamp;
}
