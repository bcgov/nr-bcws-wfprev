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
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "proposal_type_code", schema = "wfprev")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProposalTypeCodeEntity implements Serializable {

    @Id
    @Column(name = "proposal_type_code")
    @NotNull
    private String proposalTypeCode;

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

    @Version
    @Column(name = "revision_count", precision = 10, nullable = false)
    private Integer revisionCount;

    @CreatedBy
    @Column(name = "create_user", length = 64, nullable = false)
    private String createUser;

    @CreatedDate
    @Column(name = "create_date", nullable = false, columnDefinition = "DATE DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @LastModifiedBy
    @Column(name = "update_user", length = 64, nullable = false)
    private String updateUser;

    @LastModifiedDate
    @Column(name = "update_date", nullable = false, columnDefinition = "DATE DEFAULT CURRENT_TIMESTAMP")
    private Date updateDate;
}