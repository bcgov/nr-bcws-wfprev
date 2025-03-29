package ca.bc.gov.nrs.wfprev.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.MultiPolygon;
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
@Table(name = "activity_boundary")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityBoundaryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "activity_boundary_guid", nullable = false)
    private UUID activityBoundaryGuid;

    @Column(name = "activity_guid", nullable = false)
    @NotNull
    private UUID activityGuid;

    @NotNull
    @Column(name="system_start_timestamp", nullable = false)
    private Date systemStartTimestamp;

    @NotNull
    @Column(name="system_end_timestamp", nullable = false)
    private Date systemEndTimestamp;

    @Column(name="mapping_label", length = 250)
    private String mappingLabel;

    @NotNull
    @Column(name="collection_date", nullable = false)
    private Date collectionDate;

    @Column(name="collection_method", length = 4000)
    private String collectionMethod;

    @Column(name="collector_name", length = 100)
    private String collectorName;

    @NotNull
    @Column(name = "boundary_size_ha", precision = 19, scale = 4, nullable = false)
    private BigDecimal boundarySizeHa;

    @Column(name="boundary_comment", length = 2000)
    private String boundaryComment;

    @NotNull
    @Column(name = "geometry", columnDefinition = "geometry(Multipolygon, 4326)")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private MultiPolygon geometry;

    @Column(name = "revision_count", columnDefinition="Decimal(10) default '0'", nullable = false)
    @NotNull
    @Version
    private Integer revisionCount;

    @CreatedBy
    @NotNull
    @Column(name="create_user", length = 64, nullable = false)
    private String createUser;

    @CreatedDate
    @NotNull
    @Column(name="create_date", nullable = false)
    private Date createDate;

    @LastModifiedBy
    @NotNull
    @Column(name="update_user", length = 64, nullable = false)
    private String updateUser;

    @LastModifiedDate
    @NotNull
    @Column(name="update_date", nullable = false)
    private Date updateDate;
}
