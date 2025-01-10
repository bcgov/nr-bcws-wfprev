package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.ObjectiveTypeCodeEntity;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.GeneralScopeCodeEntity;

@RepositoryRestResource(exported = false)
public interface ObjectiveTypeCodeRepository extends CommonRepository<ObjectiveTypeCodeEntity, String> {

}
