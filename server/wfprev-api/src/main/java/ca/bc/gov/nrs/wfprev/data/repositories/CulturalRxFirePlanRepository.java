package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.CulturalRxFirePlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CulturalRxFirePlanRepository extends JpaRepository<CulturalRxFirePlanEntity, UUID> {
    void deleteByProjectFiscal_ProjectPlanFiscalGuid(UUID projectPlanFiscalGuid);
}
