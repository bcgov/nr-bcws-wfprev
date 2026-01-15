package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSelectedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EvaluationCriteriaSelectedRepository extends JpaRepository<EvaluationCriteriaSelectedEntity, UUID> {
    void deleteByEvaluationCriteriaSectionSummaryGuid(UUID evaluationCriteriaSectionSummaryGuid);
}
