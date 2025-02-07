package ca.bc.gov.nrs.wfprev.data.repositories;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.SourceObjectNameCodeEntity;

@RepositoryRestResource(exported = false)
public interface SourceObjectNameCodeRepository extends CommonRepository<SourceObjectNameCodeEntity, String> {

}
