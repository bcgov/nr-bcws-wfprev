package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.SilvicultureBaseCodeEntity;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface SilvicultureBaseCodeRepository extends CommonRepository<SilvicultureBaseCodeEntity, UUID> {

}
