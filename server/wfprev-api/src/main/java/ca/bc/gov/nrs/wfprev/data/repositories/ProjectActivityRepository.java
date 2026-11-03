package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface ProjectActivityRepository extends JpaRepository<ProjectActivityEntity, UUID>, JpaSpecificationExecutor<ProjectActivityEntity> {
    @Query("SELECT e FROM ProjectActivityEntity e "
            + "WHERE e.lastUpdated > :since "
            + "OR (e.lastUpdated = :since AND (cast(:afterProjectGuid as uuid) IS NULL OR e.projectGuid > :afterProjectGuid)) "
            + "ORDER BY e.lastUpdated ASC, e.projectGuid ASC "
            + "LIMIT :limit")
    List<ProjectActivityEntity> findByLastUpdatedAndProjectGuidAfter(
            @Param("since") Instant since,
            @Param("afterProjectGuid") UUID afterProjectGuid,
            @Param("limit") int limit);
}
