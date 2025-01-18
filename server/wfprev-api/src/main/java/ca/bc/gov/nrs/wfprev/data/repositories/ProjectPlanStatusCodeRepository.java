package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectPlanStatusCodeEntity;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;

@RepositoryRestResource(exported = false)
public interface ProjectPlanStatusCodeRepository extends CommonRepository<ProjectPlanStatusCodeEntity, String> {

}
