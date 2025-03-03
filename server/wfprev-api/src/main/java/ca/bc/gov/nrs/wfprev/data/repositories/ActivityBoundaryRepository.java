package ca.bc.gov.nrs.wfprev.data.repositories;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface ActivityBoundaryRepository extends CommonRepository<ActivityBoundaryEntity, String> {

    List<ActivityBoundaryEntity> findByActivityGuid(@NotNull UUID activityGuid);

    Optional<ActivityBoundaryEntity> findByActivityBoundaryGuid(@NotNull UUID id);

    void deleteByActivityBoundaryGuid(@NotNull UUID id);
}
