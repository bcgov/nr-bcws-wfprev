package ca.bc.gov.nrs.wfprev.data.entities;

import java.io.Serializable;

import java.util.Date;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "objective_type_code")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ObjectiveTypeCodeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "objective_type_code")
    @NotNull
    private String objectiveTypeCode;

    @NotNull
    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "display_order", length = 3)
    private Integer displayOrder;

    @NotNull
    @Column(name = "effective_date")
    private Date effectiveDate;

    @NotNull
    @Column(name = "expiry_date")
    private Date expiryDate;

    @Column(name = "revision_count", columnDefinition="Decimal(10) default '0'")
    @NotNull
    @Version
    private Integer revisionCount;

    @CreatedBy
    @NotNull
    @Column(name="create_user", length = 64)
    private String createUser;

    @CreatedDate
    @NotNull
    @Column(name="create_date")
    private Date createDate;

    @LastModifiedBy
    @NotNull
    @Column(name="update_user", length = 64)
    private String updateUser;

    @LastModifiedDate
    @NotNull
    @Column(name="update_date")
    private Date updateDate;
}
