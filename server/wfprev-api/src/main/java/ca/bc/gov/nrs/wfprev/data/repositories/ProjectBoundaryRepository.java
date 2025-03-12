package ca.bc.gov.nrs.wfprev.data.repositories;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface ProjectBoundaryRepository extends CommonRepository<ProjectBoundaryEntity, String> {

    List<ProjectBoundaryEntity> findByProjectGuid(@NotNull UUID projectGuid);

    Optional<ProjectBoundaryEntity> findByProjectBoundaryGuid(@NotNull UUID id);

    void deleteByProjectBoundaryGuid(@NotNull UUID id);
}
