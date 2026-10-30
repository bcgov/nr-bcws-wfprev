package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface ProjectActivityRepository extends JpaRepository<ProjectActivityEntity, UUID>, JpaSpecificationExecutor<ProjectActivityEntity> {
    List<ProjectActivityEntity> findByLastUpdatedGreaterThanEqualOrderByLastUpdatedAsc(@Param("since") Date since);
}
