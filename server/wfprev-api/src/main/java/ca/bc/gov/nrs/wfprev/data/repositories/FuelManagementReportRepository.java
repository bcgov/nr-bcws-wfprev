package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.FuelManagementReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FuelManagementReportRepository extends JpaRepository<FuelManagementReportEntity, UUID> {
    List<FuelManagementReportEntity> findByProjectGuidIn(List<UUID> projectGuids);

    List<FuelManagementReportEntity> findByProjectPlanFiscalGuidIn(List<UUID> projectGuids);
}
