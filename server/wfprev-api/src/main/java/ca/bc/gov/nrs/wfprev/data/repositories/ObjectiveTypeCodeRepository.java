package ca.bc.gov.nrs.wfprev.data.repositories;

import java.util.List;

import ca.bc.gov.nrs.wfprev.data.entities.ObjectiveTypeCodeEntity;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;

@RepositoryRestResource(exported = false)
public interface ObjectiveTypeCodeRepository extends CommonRepository<ObjectiveTypeCodeEntity, String> {

    List<ObjectiveTypeCodeEntity> findAllByOrderByDisplayOrderAsc();

}
