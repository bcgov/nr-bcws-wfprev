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
import java.util.Date;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "cultural_rx_fire_plan")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CulturalRxFirePlanEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cultural_rx_fire_plan_guid", updatable = false, nullable = false)
    private UUID culturalRxFirePlanGuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_plan_fiscal_guid", nullable = false)
    private ProjectFiscalEntity projectFiscal;

    @Column(name = "burn_impl_season_code", length = 10)
    private String burnImplSeasonCode;

    @NotNull
    @Column(name = "developed_with_first_natio_ind", nullable = false)
    private Boolean developedWithFirstNatioInd;

    @NotNull
    @Column(name = "first_nations_partnership_ind", nullable = false)
    private Boolean firstNationsPartnershipInd;

    @NotNull
    @Column(name = "maximizes_funding_integr_ind", nullable = false)
    private Boolean maximizesFundingIntegrInd;

    @NotNull
    @Column(name = "capacity_development_opp_ind", nullable = false)
    private Boolean capacityDevelopmentOppInd;

    @NotNull
    @Column(name = "revision_count", columnDefinition = "numeric(10) default '0'")
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
