package ca.bc.gov.nrs.wfprev.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
public class EvaluationCriteriaSectionSummaryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "eval_criteria_sect_summ_guid", updatable = false, nullable = false)
    private UUID evaluationCriteriaSectionSummaryGuid;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "eval_criteria_sect_code")
    private EvaluationCriteriaSectionCodeEntity evaluationCriteriaSectionCode;

    @NotNull
    @Column(name = "eval_criteria_summary_guid", nullable = false)
    private UUID evaluationCriteriaSummaryGuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eval_criteria_summary_guid", nullable = false, insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EvaluationCriteriaSummaryEntity evaluationCriteriaSummary;

    @NotNull
    @Column(name = "filter_section_score", columnDefinition = "numeric(5) default '0'", nullable = false)
    private BigDecimal filterSectionScore;


    @Column(name = "filter_section_comment", length = 4000)
    private String filterSectionComment;

    @OneToMany(mappedBy = "evaluationCriteriaSectionSummary", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<EvaluationCriteriaSelectedEntity> evaluationCriteriaSelected = new ArrayList<>();

    @NotNull
    @Column(name = "revision_count", columnDefinition = "numeric(10) default '0'", nullable = false)
    @Version
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
