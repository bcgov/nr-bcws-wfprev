package ca.bc.gov.nrs.wfprev.data.models;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "eval_criteria_summary")
@Relation(collectionRelation = "eval_criteria_summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationCriteriaSectionSummaryModel extends CommonModel<EvaluationCriteriaSectionSummaryModel> {
    private String evaluationCriteriaSectionSummaryGuid;
    private EvaluationCriteriaSectionCodeModel evaluationCriteriaSectionCode;
    private String evaluationCriteriaSummaryGuid;
    private Integer filterSectionScore;
    private String filterSectionComment;
    private List<EvaluationCriteriaSelectedModel> evaluationCriteriaSelected;
}