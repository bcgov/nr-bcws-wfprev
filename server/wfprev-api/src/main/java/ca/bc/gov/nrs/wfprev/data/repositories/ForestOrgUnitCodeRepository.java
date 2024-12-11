package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.ForestOrgUnitCodeEntity;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(exported = false)
public interface ForestOrgUnitCodeRepository extends CommonRepository<ForestOrgUnitCodeEntity, String> {

    List<ForestOrgUnitCodeEntity> findByForestOrgUnitTypeCode(String forestOrgUnitTypeCode);
}
