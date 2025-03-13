package ca.bc.gov.nrs.wfprev.data.entities;

import ca.bc.gov.nrs.wfprev.common.serializers.PGPolygonDeserializer;
import ca.bc.gov.nrs.wfprev.common.serializers.PGPolygonSerializer;
import ca.bc.gov.nrs.wfprev.common.types.PostgresPolygonType;
import ca.bc.gov.nrs.wfprev.common.validators.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UuidGenerator;
import org.postgresql.geometric.PGpolygon;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "project_boundary")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectBoundaryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "project_boundary_guid")
    private UUID projectBoundaryGuid;

    @Column(name = "project_guid")
    @NotNull
    private UUID projectGuid;

    @NotNull
    @Column(name = "system_start_timestamp")
    private Date systemStartTimestamp;

    @NotNull
    @Column(name = "system_end_timestamp")
    private Date systemEndTimestamp;

    @Column(name = "mapping_label", length = 250)
    private String mappingLabel;

    @NotNull
    @Column(name = "collection_date")
    private Date collectionDate;

    @Column(name = "collection_method", length = 4000)
    private String collectionMethod;

    @Column(name = "collector_name", length = 100)
    private String collectorName;

    @NotNull
    @Column(name = "boundary_size_ha", precision = 19, scale = 4)
    private BigDecimal boundarySizeHa;

    @Column(name = "boundary_comment", length = 2000)
    private String boundaryComment;

    @NotNull
    @Column(name = "location_geometry", columnDefinition = "polygon")
    @JdbcType(PostgresPolygonType.class)
    @JsonDeserialize(using = PGPolygonDeserializer.class)
    @JsonSerialize(using = PGPolygonSerializer.class)
    @Geometry
    public PGpolygon locationGeometry;

    @NotNull
    @Column(name = "boundary_geometry", columnDefinition = "polygon")
    @JdbcType(PostgresPolygonType.class)
    @JsonDeserialize(using = PGPolygonDeserializer.class)
    @JsonSerialize(using = PGPolygonSerializer.class)
    @Geometry
    public PGpolygon boundaryGeometry;

    @Column(name = "revision_count", columnDefinition = "Decimal(10) default '0'")
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
