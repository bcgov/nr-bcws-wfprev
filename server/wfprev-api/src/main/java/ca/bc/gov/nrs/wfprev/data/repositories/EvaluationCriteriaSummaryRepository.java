package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSummaryEntity;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface EvaluationCriteriaSummaryRepository extends CommonRepository<EvaluationCriteriaSummaryEntity, UUID> {
    List<EvaluationCriteriaSummaryEntity> findAllByProjectGuid(UUID projectGuid);
}

