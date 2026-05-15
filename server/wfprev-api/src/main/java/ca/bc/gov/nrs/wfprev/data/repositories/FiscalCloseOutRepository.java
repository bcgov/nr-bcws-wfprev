package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.FiscalCloseOutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FiscalCloseOutRepository extends JpaRepository<FiscalCloseOutEntity, UUID> {
    Optional<FiscalCloseOutEntity> findByProjectFiscal_ProjectPlanFiscalGuid(UUID projectPlanFiscalGuid);
    void deleteByProjectFiscal_ProjectPlanFiscalGuid(UUID projectPlanFiscalGuid);
}
