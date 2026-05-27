package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSelectedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.UUID;

@Repository
@RepositoryRestResource(exported = false)
public interface EvaluationCriteriaSelectedRepository extends JpaRepository<EvaluationCriteriaSelectedEntity, UUID> {
    void deleteByEvaluationCriteriaSectionSummaryGuid(UUID evaluationCriteriaSectionSummaryGuid);
}
