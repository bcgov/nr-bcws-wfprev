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
@Table(name = "reporting_period_code", schema = "wfprev")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportingPeriodCodeEntity implements Serializable {
  
  @Id
  @Column(name = "reporting_period_code", length = 10)
  @NotNull
  private String reportingPeriodCode;

  @NotNull
  @Column(name = "description", length = 200)
  private String description;

  @Column(name = "display_order")
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
