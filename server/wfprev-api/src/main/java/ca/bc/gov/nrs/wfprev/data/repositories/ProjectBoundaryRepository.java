package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.model.ProjectBoundaryEntity;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;

@RepositoryRestResource(exported = false)
public interface ProjectBoundaryRepository extends CommonRepository<ProjectBoundaryEntity, String> {
  
}
