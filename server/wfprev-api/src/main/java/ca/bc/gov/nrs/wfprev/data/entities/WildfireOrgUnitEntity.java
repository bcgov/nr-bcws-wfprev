package ca.bc.gov.nrs.wfprev.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
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
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "wildfire_org_unit")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WildfireOrgUnitEntity implements Serializable {

    @Id
    @Column(name = "org_unit_identifier")
    private Integer orgUnitIdentifier;

    @NotNull
    @Column(name = "effective_date")
    private Date effectiveDate;

    @NotNull
    @Column(name = "expiry_date")
    private Date expiryDate;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "wildfire_org_unit_type_code")
    private WildfireOrgUnitTypeCodeEntity wildfireOrgUnitTypeCode;

    @Column(name = "parent_org_unit_identifier")
    private Integer parentOrgUnitIdentifier;

    @Column(name = "org_unit_name", length = 120)
    private String orgUnitName;

    @Column(name = "integer_alias")
    private Integer integerAlias;

    @Column(name = "character_alias", length = 10)
    private String characterAlias;

    @NotNull
    @Version
    @Column(name = "revision_count", columnDefinition = "numeric(10)")
    private Integer revisionCount;

    @NotNull
    @CreatedBy
    @Column(name = "create_user", length = 64)
    private String createUser;

    @NotNull
    @CreatedDate
    @Column(name = "create_date")
    private Date createDate;

    @NotNull
    @LastModifiedBy
    @Column(name = "update_user", length = 64)
    private String updateUser;

    @NotNull
    @LastModifiedDate
    @Column(name = "update_date")
    private Date updateDate;
}
