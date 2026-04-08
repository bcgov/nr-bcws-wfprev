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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "eval_criteria_summary")
@JsonIgnoreProperties(ignoreUnknown = false)
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EvaluationCriteriaSummaryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "eval_criteria_summary_guid", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    @Setter
    private UUID evaluationCriteriaSummaryGuid;

    @NotNull
    @Column(name = "project_guid", nullable = false)
    @Setter
    private UUID projectGuid;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "wui_risk_class_code",
        referencedColumnName = "wui_risk_class_code"
    )
    private WUIRiskClassRankEntity wuiRiskClassCode;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "local_wui_risk_class_code",
        referencedColumnName = "wui_risk_class_code"
    )
    private WUIRiskClassRankEntity localWuiRiskClassCode;

    @Column(name = "wui_risk_class_comment", length = 4000)
    @Setter
    private String wuiRiskClassComment;

    @Column(name = "local_wui_risk_class_rationale", length = 4000)
    @Setter
    private String localWuiRiskClassRationale;

    @NotNull
    @Column(name = "outside_wui_ind", nullable = false)
    @Setter
    private Boolean isOutsideWuiInd;

    @NotNull
    @Column(name = "total_filter_score", columnDefinition = "numeric(5) default '0'", nullable = false)
    @Setter
    private Integer totalFilterScore;

    @OneToMany(mappedBy = "evaluationCriteriaSummary", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<EvaluationCriteriaSectionSummaryEntity> evaluationCriteriaSectionSummaries = new ArrayList<>();

    @NotNull
    @Column(name = "revision_count", columnDefinition = "numeric(10) default '0'", nullable = false)
    @Version
    @Setter
    private Integer revisionCount;

    @CreatedBy
    @NotNull
    @Column(name = "create_user", length = 64, nullable = false)
    @Setter
    private String createUser;

    @CreatedDate
    @NotNull
    @Column(name = "create_date", nullable = false)
    @Setter
    private Date createDate;

    @LastModifiedBy
    @NotNull
    @Column(name = "update_user", length = 64, nullable = false)
    @Setter
    private String updateUser;

    @LastModifiedDate
    @NotNull
    @Column(name = "update_date", nullable = false)
    @Setter
    private Date updateDate;
    
    @NotNull
    @Column(name = "last_updated_timestamp", nullable = false)
    @Setter
    private Date lastUpdatedTimestamp;

    public void addEvaluationCriteriaSectionSummaries(EvaluationCriteriaSectionSummaryEntity item) {
        evaluationCriteriaSectionSummaries.add(item);
        item.setEvaluationCriteriaSummary(this);
    }

    public void removeEvaluationCriteriaSectionSummaries(EvaluationCriteriaSectionSummaryEntity item) {
        evaluationCriteriaSectionSummaries.remove(item);
        item.setEvaluationCriteriaSummary(null);
    }
}
