package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.CulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FuelManagementReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CulturalPrescribedFireReportRepository extends JpaRepository<CulturalPrescribedFireReportEntity, UUID> {
    List<CulturalPrescribedFireReportEntity> findByProjectGuidIn(List<UUID> projectGuids);

    List<CulturalPrescribedFireReportEntity> findByProjectPlanFiscalGuidIn(List<UUID> projectGuids);
}
