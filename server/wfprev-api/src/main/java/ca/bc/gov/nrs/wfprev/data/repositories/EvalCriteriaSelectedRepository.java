package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.EvalCriteriaSelectedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EvalCriteriaSelectedRepository extends JpaRepository<EvalCriteriaSelectedEntity, UUID> {
    void deleteByEvalCriteriaSectSumm_EvalCriteriaSectSummGuid(UUID evalCriteriaSectSummGuid);
}
