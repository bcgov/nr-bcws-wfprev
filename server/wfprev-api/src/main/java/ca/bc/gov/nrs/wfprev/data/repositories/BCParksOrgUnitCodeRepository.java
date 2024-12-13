package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.BCParksOrgUnitEntity;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(exported = false)
public interface BCParksOrgUnitCodeRepository extends CommonRepository<BCParksOrgUnitEntity, Integer>{
    List<BCParksOrgUnitEntity> findByBcParksOrgUnitTypeCode(String bcParksOrgUnitTypeCode);

}
