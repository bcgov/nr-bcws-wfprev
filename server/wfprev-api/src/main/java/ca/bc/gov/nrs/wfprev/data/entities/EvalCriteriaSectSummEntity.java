package ca.bc.gov.nrs.wfprev.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
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
@Table(name = "eval_criteria_sect_summ")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvalCriteriaSectSummEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "eval_criteria_sect_summ_guid", updatable = false, nullable = false)
    private UUID evalCriteriaSectSummGuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eval_criteria_summary_guid", nullable = false)
    private EvalCriteriaSummaryEntity evalCriteriaSummary;

    @NotNull
    @Column(name = "eval_criteria_sect_code", length = 10, nullable = false)
    private String evalCriteriaSectCode;

    @NotNull
    @Column(name = "filter_section_score", precision = 6, scale = 2, nullable = false)
    private BigDecimal filterSectionScore;

    @Column(name = "filter_section_comment", length = 4000)
    private String filterSectionComment;

    @NotNull
    @Column(name = "revision_count", columnDefinition = "numeric(10) default '0'")
    private Integer revisionCount;

    @CreatedBy
    @NotNull
    @Column(name = "create_user", length = 64, nullable = false)
    private String createUser;

    @CreatedDate
    @NotNull
    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @LastModifiedBy
    @NotNull
    @Column(name = "update_user", length = 64, nullable = false)
    private String updateUser;

    @LastModifiedDate
    @NotNull
    @Column(name = "update_date", nullable = false)
    private Date updateDate;

}
