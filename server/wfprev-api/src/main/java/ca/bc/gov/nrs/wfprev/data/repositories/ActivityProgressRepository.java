package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.ActivityProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActivityProgressRepository extends JpaRepository<ActivityProgressEntity, UUID> {
    void deleteByActivity_ActivityGuid(UUID activityGuid);
    void deleteByProjectPlanFiscalPerf_ProjectPlanFiscalPerfGuid(UUID projectPlanFiscalPerfGuid);
}
