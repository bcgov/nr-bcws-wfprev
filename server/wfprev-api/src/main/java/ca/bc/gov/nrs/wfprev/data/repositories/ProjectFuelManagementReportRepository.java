package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectFuelManagementReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
@RepositoryRestResource(exported = false)
public interface ProjectFuelManagementReportRepository extends JpaRepository<ProjectFuelManagementReportEntity, UUID> {
    List<ProjectFuelManagementReportEntity> findByProjectGuid(UUID projectGuid);

    List<ProjectFuelManagementReportEntity> findByProjectGuidAndProjectPlanFiscalGuidIn(UUID projectGuid, Collection<UUID> fiscalGuids);
}