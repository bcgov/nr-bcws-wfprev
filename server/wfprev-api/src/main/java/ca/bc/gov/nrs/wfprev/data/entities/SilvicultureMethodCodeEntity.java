package ca.bc.gov.nrs.wfprev.data.entities;

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
import org.hibernate.annotations.Formula;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "silviculture_method")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SilvicultureMethodCodeEntity implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "silviculture_method_guid", nullable = false, updatable = false)
    private UUID silvicultureMethodGuid;

    @Column(name = "silviculture_technique_guid", nullable = false)
    private UUID silvicultureTechniqueGuid;

    @Column(name = "silviculture_method_code", nullable = false)
    private String silvicultureMethodCode;

    @Formula("(SELECT smc.description FROM silviculture_method_code smc WHERE smc.silviculture_method_code = silviculture_method_code)")
    private String description;

    @Column(name = "system_start_timestamp")
    private Date systemStartTimestamp;

    @Column(name = "system_end_timestamp")
    private Date systemEndTimestamp;

    @Column(name = "revision_count")
    private Integer revisionCount;

    @Column(name = "create_user")
    private String createUser;

    @Column(name = "create_date")
    private Date createDate;

    @Column(name = "update_user")
    private String updateUser;

    @Column(name = "update_date")
    private Date updateDate;

}
