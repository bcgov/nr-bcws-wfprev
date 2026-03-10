package ca.bc.gov.nrs.wfprev.data.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;

@RepositoryRestResource(exported = false)
public interface ProjectTypeCodeRepository extends CommonRepository<ProjectTypeCodeEntity, String> {
    List<ProjectTypeCodeEntity> findAllByOrderByDisplayOrderAsc();
}