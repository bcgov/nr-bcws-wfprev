package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ca.bc.gov.nrs.wfprev.data.model.ProjectEntity;

@RepositoryRestResource(exported = false)
public interface ProjectRepository extends CommonRepository<ProjectEntity, String> {
  
}
