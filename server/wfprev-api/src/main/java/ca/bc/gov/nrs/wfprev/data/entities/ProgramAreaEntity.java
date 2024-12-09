package ca.bc.gov.nrs.wfprev.data.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "program_area")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgramAreaEntity implements Serializable {
  @Id
  @UuidGenerator
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "program_area_guid", columnDefinition = "uuid")
  private UUID programAreaGuid;

  @NotNull
	@Column(name="program_area_name", length = 100)
	private String programAreaName;

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
