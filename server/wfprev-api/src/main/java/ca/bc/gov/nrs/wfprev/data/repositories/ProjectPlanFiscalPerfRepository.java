package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectPlanFiscalPerfEntity;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface ProjectPlanFiscalPerfRepository extends JpaRepository<ProjectPlanFiscalPerfEntity, UUID> {
    void deleteByProjectFiscal_ProjectPlanFiscalGuid(UUID projectPlanFiscalGuid);
    List<ProjectPlanFiscalPerfEntity> findAllByProjectFiscal_ProjectPlanFiscalGuid(UUID projectPlanFiscalGuid);
    List<ProjectPlanFiscalPerfEntity> findAllByProjectFiscal_ProjectPlanFiscalGuid(
        UUID projectPlanFiscalGuid,
        Sort sort);
}
