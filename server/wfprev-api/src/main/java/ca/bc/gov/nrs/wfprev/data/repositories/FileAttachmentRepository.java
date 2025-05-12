package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.FileAttachmentEntity;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface FileAttachmentRepository extends CommonRepository<FileAttachmentEntity, UUID> {
    List<FileAttachmentEntity> findAllBySourceObjectUniqueId(String sourceObjectUniqueId);

    List<FileAttachmentEntity> findAllBySourceObjectUniqueIdIn(List<String> boundaryGuids);
}
