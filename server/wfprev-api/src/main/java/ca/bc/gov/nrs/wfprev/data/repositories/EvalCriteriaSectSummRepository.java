package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.EvalCriteriaSectSummEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvalCriteriaSectSummRepository extends JpaRepository<EvalCriteriaSectSummEntity, UUID> {
    void deleteByEvalCriteriaSummary_EvalCriteriaSummaryGuid(UUID evalCriteriaSummaryGuid);
    List<EvalCriteriaSectSummEntity> findAllByEvalCriteriaSummary_EvalCriteriaSummaryGuid(UUID evalCriteriaSummaryGuid);
}
