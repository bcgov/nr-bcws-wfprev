package ca.bc.gov.nrs.wfprev.data.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_objective")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FuelManagementObjectiveCodeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "project_objective_guid", nullable = false)
    @NotNull
    private String fuelManagementObjectiveGuid;

    @Column(name = "project_type_code", length = 10)
    private String fuelManagementObjectiveTypeCode;

    @Column(name = "objective_label", length = 300)
    private String objectiveLabel;

    @Column(name = "objective_filter_level_code", length = 10)
    private String objectiveFilterLevelCode;

    @Column(name = "weighted_rank")
    private BigDecimal weightedRank;

    @LastModifiedBy
    @NotNull
    @Column(name="update_user", length = 64)
    private String updateUser;

    @LastModifiedDate
    @NotNull
    @Column(name="update_date")
    private Date updateDate;
}
