package ca.bc.gov.nrs.wfprev.data.repositories;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.model.ExampleCodeEntity;

@RepositoryRestResource(exported = false)
public interface ExampleCodeRepository extends CommonRepository<ExampleCodeEntity, String> {
  
}
