package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.model.GeneralScopeCodeEntity;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;

@RepositoryRestResource(exported = false)
public interface GeneralScopeCodeRepository extends CommonRepository<GeneralScopeCodeEntity, String> {
  
}
