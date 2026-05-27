package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.FiscalCloseoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;

@Repository
@RepositoryRestResource(exported = false)
public interface FiscalCloseoutRepository extends JpaRepository<FiscalCloseoutEntity, UUID> {
    List<FiscalCloseoutEntity> findAllByProjectFiscal_ProjectPlanFiscalGuid(UUID projectPlanFiscalGuid, Sort sort);
    void deleteByProjectFiscal_ProjectPlanFiscalGuid(UUID projectPlanFiscalGuid);
}
