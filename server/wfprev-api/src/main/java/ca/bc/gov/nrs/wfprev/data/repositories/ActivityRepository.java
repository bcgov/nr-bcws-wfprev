package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface ActivityRepository extends CommonRepository<ActivityEntity, UUID> {
    List<ActivityEntity> findByProjectPlanFiscalGuid(UUID projectPlanFiscalGuid);
    void deleteByProjectPlanFiscalGuid(UUID projectPlanFiscalGuid);
}
