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

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "eval_criteria_selected")
@Relation(collectionRelation = "eval_criteria_selected")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationCriteriaSelectedModel extends CommonModel<EvaluationCriteriaSelectedModel> {
    private String evaluationCriteriaSelectedGuid;
    private String evaluationCriteriaGuid;
    private String evaluationCriteriaSectionSummaryGuid;
    private Boolean isEvaluationCriteriaSelectedInd;
}