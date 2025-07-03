package ca.bc.gov.nrs.wfprev.data.entities;

import java.io.Serializable;
import java.math.BigDecimal;
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
@Table(name = "wui_risk_class_rank")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WUIRiskClassCodeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "wui_risk_class_rank_guid")
    @NotNull
    private String wuiRiskClassRankGuid;

    @NotNull
    @Column(name = "wui_risk_class_code", length = 200)
    private String wuiRiskClassCode;

    @Column(name = "weighted_rank")
    private BigDecimal weightedRank;

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
