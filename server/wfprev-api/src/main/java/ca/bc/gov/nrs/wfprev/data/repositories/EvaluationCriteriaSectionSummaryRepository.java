package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvaluationCriteriaSectionSummaryRepository extends JpaRepository<EvaluationCriteriaSectionSummaryEntity, UUID> {
    List<EvaluationCriteriaSectionSummaryEntity> findAllByEvaluationCriteriaSummaryGuid(UUID evaluationCriteriaSummaryGuid);
    void deleteByEvaluationCriteriaSummaryGuid(UUID evaluationCriteriaSummaryGuid);
}
