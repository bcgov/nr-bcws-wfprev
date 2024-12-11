package ca.bc.gov.nrs.wfprev.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "forest_org_unit")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForestOrgUnitCodeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "org_unit_identifier")
    @NotNull
    private String orgUnitIdentifier;

    @NotNull
    @Column(name = "effective_date")
    private Date effectiveDate;

    @NotNull
    @Column(name = "expiry_date")
    private Date expiryDate;

    @NotNull
    @Column(name = "forest_org_unit_type_code")
    private String forestOrgUnitTypeCode;

    @Column(name = "parent_org_unit_identifier")
    private String parentOrgUnitIdentifier;

    @NotNull
    @Column(name = "org_unit_name")
    private String orgUnitName;

    @Column(name = "integer_alias")
    private Integer integerAlias;

    @Column(name = "character_alias")
    private String characterAlias;

    @Column(name = "revision_count", columnDefinition="Decimal(10) default '0'")
    @NotNull
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

}
