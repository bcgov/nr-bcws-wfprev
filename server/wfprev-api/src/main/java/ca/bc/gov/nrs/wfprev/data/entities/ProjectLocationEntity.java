package ca.bc.gov.nrs.wfprev.data.entities;

import ca.bc.gov.nrs.wfprev.common.validators.Latitude;
import ca.bc.gov.nrs.wfprev.common.validators.Longitude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "project")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Immutable
public class ProjectLocationEntity implements Serializable {

    @Id
    @Column(name = "project_guid", nullable = false, updatable = false, insertable = false)
    private UUID projectGuid;

    @Column(name = "latitude", precision = 9, scale = 6, updatable = false, insertable = false)
    @Latitude
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6, updatable = false, insertable = false)
    @Longitude
    private BigDecimal longitude;

}