package ca.bc.gov.nrs.wfprev.data.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ca.bc.gov.nrs.wfprev.common.converters.BooleanConverter;
import ca.bc.gov.nrs.wfprev.common.validators.Latitude;
import ca.bc.gov.nrs.wfprev.common.validators.Longitude;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PostRemove;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A really simple example resource model. Doesn't match the example model but enough for a demo
 */

@Entity
@Table(name = "EXAMPLE_TABLE")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExampleEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @UuidGenerator
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "example_guid")
  private String exampleGuid;

  //@NotFound(action = NotFoundAction.IGNORE)
	//@RestResource(exported = false)
  //@ManyToOne(fetch = FetchType.LAZY, optional = true, cascade = CascadeType.REFRESH)
	//@JoinColumn(name="example_code")
  @Column(name="example_code")
  private String exampleCode;

  @Column(name = "example_text")
  private String exampleText;

  @Column(name = "example_var", length = 25)
  private String exampleVar;

  @Column(name = "example_num", precision = 10, scale = 3)
  private BigDecimal exampleNum;

  @Column(name = "example_ind")
  @Convert(converter = BooleanConverter.class)
  @NotNull
  private Boolean exampleInd;

  @LastModifiedDate
	@Column(name="last_updated_timestamp")
	private Long lastUpdatedTimestamp;
  
  // alternative way to set column definitions
  @Column(name = "revision_count", columnDefinition="Decimal(10) default '0'")
  @NotNull
  @Version // Version enables optomistic locks. This will detect conflicts.
  private Integer revisionCount;

  @CreatedBy
  @NotNull
	@Column(name="create_user", length = 64)
	private String createdBy;

  @CreatedDate
  @NotNull
	@Column(name="create_date")
	private Date createDate;

  @LastModifiedBy
  @NotNull
	@Column(name="update_user", length = 64)
	private String updatedBy;

  @LastModifiedDate
  @NotNull
	@Column(name="update_date")
	private Date updateDate;

  // Example for spatial columns
  //@Latitude
  //@NotNull
  //private Double latitude;

  //@Longitude
  //@NotNull
  //private Double longitude;

  //@Column(columnDefinition = "GEOMETRY(Point,4326)", nullable = false)
  //private Point geometry;


  /*** Events that must trigger BEFORE pushing changes to the Database with this entity *******/
  @PrePersist
  public void prePersist() {
    // do stuff you need to do before you write to the DB (POST)
  }

  @PreUpdate
  public void preUpdate() {
    // as above, but on Update events only
  }

  @PreRemove
  public void preDelete() {
    // Before delete is executed
  }

  /*** Events that must trigger AFTER pushing changes to the Database with this entity *******/
  @PostPersist
  public void postPersist() {
    // do stuff you need to do after you write to the DB (POST)
  }

  @PostUpdate
  public void postUpdate() {
    // as above, but on Update events only
  }

  @PostRemove
  public void postDelete() {
    // After delete is executed
  }
}
