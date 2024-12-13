package ca.bc.gov.nrs.wfprev.data.entities;

import ca.bc.gov.nrs.wfprev.common.validators.Latitude;
import ca.bc.gov.nrs.wfprev.common.validators.Longitude;
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
@Table(name = "project")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectEntity implements Serializable {

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "project_guid")
    private UUID projectGuid;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "project_type_code")
    private ProjectTypeCodeEntity projectTypeCode;

    @Column(name = "project_number", columnDefinition = "Decimal(10)", insertable = false, updatable = true)
    private Integer projectNumber;

    @NotNull
    @Column(name = "site_unit_name", length = 250)
    private String siteUnitName;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "forest_area_code")
    private ForestAreaCodeEntity forestAreaCode;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "general_scope_code")
    private GeneralScopeCodeEntity generalScopeCode;

    @Column(name = "program_area_guid", columnDefinition = "uuid")
    @NotNull
    private UUID programAreaGuid;

    @Column(name = "forest_region_org_unit_id", columnDefinition = "Decimal(10)")
    @NotNull
    private Integer forestRegionOrgUnitId;

    @Column(name = "forest_district_org_unit_id", columnDefinition = "Decimal(10)")
    private Integer forestDistrictOrgUnitId;

    @Column(name = "fire_centre_org_unit_id", columnDefinition = "Decimal(10)")
    private Integer fireCentreOrgUnitId;

    @Column(name = "bc_parks_region_org_unit_id", columnDefinition = "Decimal(10)")
    private Integer bcParksRegionOrgUnitId;

    @Column(name = "bc_parks_section_org_unit_id", columnDefinition = "Decimal(10)")
    private Integer bcParksSectionOrgUnitId;

    @NotNull
    @Column(name = "project_name", length = 300)
    private String projectName;

    @Column(name = "project_lead", length = 300)
    private String projectLead;

    @Column(name = "project_lead_email_address", length = 100)
    private String projectLeadEmailAddress;

    @Column(name = "project_description", length = 4000)
    private String projectDescription;

    @Column(name = "closest_community_name", length = 250)
    private String closestCommunityName;

    @Column(name = "total_funding_request_amount", precision = 15, scale = 2)
    private BigDecimal totalFundingRequestAmount;

    @Column(name = "total_allocated_amount", precision = 15, scale = 2)
    private BigDecimal totalAllocatedAmount;

    @Column(name = "total_planned_project_size_ha", columnDefinition = "Decimal(15, 4) default '0'")
    private BigDecimal totalPlannedProjectSizeHa;

    @Column(name = "total_planned_cost_per_hectare", columnDefinition = "Decimal(15, 2) default '0'")
    private BigDecimal totalPlannedCostPerHectare;

    @NotNull
    @Column(name = "total_actual_amount", columnDefinition = "Decimal(15, 2) default '0'")
    private BigDecimal totalActualAmount;

    @Column(name = "total_actual_project_size_ha", columnDefinition = "Decimal(15, 4) default '0'")
    private BigDecimal totalActualProjectSizeHa;

    @Column(name = "total_actual_cost_per_hectare_amount", columnDefinition = "Decimal(15, 2) default '0'")
    private BigDecimal totalActualCostPerHectareAmount;

    @NotNull
    @Column(name = "is_multi_fiscal_year_proj_ind")
    @Builder.Default
    private Boolean isMultiFiscalYearProj = false;

    @Column(name = "latitude", precision = 9, scale = 6)
    @Latitude
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    @Longitude
    private BigDecimal longitude;

    @Column(name = "last_progress_update_timestamp")
    private Date lastProgressUpdateTimestamp;

    @Column(name = "revision_count", columnDefinition = "Decimal(10) default '0'")
    @NotNull
    @Version
    private Integer revisionCount;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "project_status_code")
    private ProjectStatusCodeEntity projectStatusCode;

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
}