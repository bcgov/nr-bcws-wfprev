package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.ForestRegionCodeEntity;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(exported = false)
public interface ForestRegionCodeRepository  extends CommonRepository<ForestRegionCodeEntity, String> {

    List<ForestRegionCodeEntity> findByForestOrgUnitTypeCode(String forestOrgUnitTypeCode);
}
