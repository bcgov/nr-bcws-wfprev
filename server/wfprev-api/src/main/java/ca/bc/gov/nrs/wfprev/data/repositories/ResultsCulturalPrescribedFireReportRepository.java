package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.ResultsCulturalPrescribedFireReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
@RepositoryRestResource(exported = false)
public interface ResultsCulturalPrescribedFireReportRepository extends JpaRepository<ResultsCulturalPrescribedFireReportEntity, UUID> {
    List<ResultsCulturalPrescribedFireReportEntity> findByProjectGuidIn(Collection<UUID> projectGuids);

    List<ResultsCulturalPrescribedFireReportEntity> findByProjectPlanFiscalGuidIn(Collection<UUID> fiscalGuids);
}
