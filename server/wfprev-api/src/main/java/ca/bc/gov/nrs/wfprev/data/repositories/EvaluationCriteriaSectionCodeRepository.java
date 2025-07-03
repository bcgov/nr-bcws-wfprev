package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionCodeEntity;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface EvaluationCriteriaSectionCodeRepository extends CommonRepository<EvaluationCriteriaSectionCodeEntity, String> {}
