package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.EvalCriteriaSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvalCriteriaSummaryRepository extends JpaRepository<EvalCriteriaSummaryEntity, UUID> {
    void deleteByProject_ProjectGuid(UUID projectGuid);
    List<EvalCriteriaSummaryEntity> findAllByProject_ProjectGuid(UUID projectGuid);
}
