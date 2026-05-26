package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.FiscalCloseoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FiscalCloseoutRepository extends JpaRepository<FiscalCloseoutEntity, UUID> {
    Optional<FiscalCloseoutEntity> findByProjectFiscal_ProjectPlanFiscalGuid(UUID projectPlanFiscalGuid);
    void deleteByProjectFiscal_ProjectPlanFiscalGuid(UUID projectPlanFiscalGuid);
}
