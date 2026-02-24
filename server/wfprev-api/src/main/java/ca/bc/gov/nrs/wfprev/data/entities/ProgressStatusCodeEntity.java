package ca.bc.gov.nrs.wfprev.data.entities;

import java.sql.Date;

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
@Table(name = "progress_status_code", schema = "wfprev")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgressStatusCodeEntity {

    @Id
    @Column(name = "progress_status_code", length = 10)
    @NotNull
    private String progressStatusCode;

    @NotNull
    @Column(name = "description", length = 200)
    private String description;

    @NotNull
    @Column(name = "display_order")
    private Integer displayOrder;

    @NotNull
    @Column(name = "effective_date")
    private Date effectiveDate;

    @NotNull
    @Column(name = "expiry_date")
    private Date expiryDate;

    @NotNull
    @Column(name = "revision_count")
    private Integer revisionCount;

    @NotNull
    @Column(name = "create_user")
    private String createUser;

    @NotNull
    @Column(name = "create_date")
    private Date createDate;

    @NotNull
    @Column(name = "update_user")
    private String updateUser;

    @NotNull
    @Column(name = "update_date")
    private Date updateDate;
}
