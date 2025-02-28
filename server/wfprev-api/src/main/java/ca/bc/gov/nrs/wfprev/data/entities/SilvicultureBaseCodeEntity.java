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
@Table(name = "silviculture_base")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SilvicultureBaseCodeEntity implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "silviculture_base_guid", nullable = false, updatable = false)
    private UUID silvicultureBaseGuid;

    @Column(name = "silviculture_base_code", nullable = false)
    private String silvicultureBaseCode;

    @Formula("(SELECT sbc.description FROM silviculture_base_code sbc WHERE sbc.silviculture_base_code = silviculture_base_code)")
    private String description;

    @Column(name = "project_type_code")
    private String projectTypeCode;

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
