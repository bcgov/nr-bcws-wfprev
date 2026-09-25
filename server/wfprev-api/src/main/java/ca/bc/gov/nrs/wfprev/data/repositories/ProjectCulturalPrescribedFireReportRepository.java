package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectCulturalPrescribedFireReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
@RepositoryRestResource(exported = false)
public interface ProjectCulturalPrescribedFireReportRepository extends JpaRepository<ProjectCulturalPrescribedFireReportEntity, UUID> {
    List<ProjectCulturalPrescribedFireReportEntity> findByProjectGuidIn(Collection<UUID> projectGuids);

    List<ProjectCulturalPrescribedFireReportEntity> findByProjectPlanFiscalGuidIn(Collection<UUID> fiscalGuids);
}