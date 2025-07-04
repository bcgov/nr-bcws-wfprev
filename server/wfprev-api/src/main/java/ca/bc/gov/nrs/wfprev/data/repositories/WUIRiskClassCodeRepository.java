package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.WUIRiskClassRankEntity;

import java.util.Optional;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface WUIRiskClassCodeRepository extends CommonRepository<WUIRiskClassRankEntity, String> {
     Optional<WUIRiskClassRankEntity> findByWuiRiskClassCode(String wuiRiskClassCode);
}
