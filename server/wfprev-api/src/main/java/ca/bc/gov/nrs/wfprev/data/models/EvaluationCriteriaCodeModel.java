package ca.bc.gov.nrs.wfprev.data.models;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "evaluationCriteriaCode")
@Relation(collectionRelation = "evaluationCriteriaCode")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationCriteriaCodeModel extends CommonModel<EvaluationCriteriaCodeModel> {
    private UUID  evaluationCriteriaGuid;
    private String projectTypeCode;
    private String criteriaLabel;
    private String evalCriteriaSectCode;
    private BigDecimal weightedRank;
}
