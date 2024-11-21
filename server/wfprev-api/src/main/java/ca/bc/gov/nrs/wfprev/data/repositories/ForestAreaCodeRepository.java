package ca.bc.gov.nrs.wfprev.data.repositories;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.model.ForestAreaCodeEntity;

@RepositoryRestResource(exported = false)
public interface ForestAreaCodeRepository extends CommonRepository<ForestAreaCodeEntity, String> {
  
}
