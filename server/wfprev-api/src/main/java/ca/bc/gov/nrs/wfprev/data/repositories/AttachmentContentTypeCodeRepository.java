package ca.bc.gov.nrs.wfprev.data.repositories;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.AttachmentContentTypeCodeEntity;

@RepositoryRestResource(exported = false)
public interface AttachmentContentTypeCodeRepository extends CommonRepository<AttachmentContentTypeCodeEntity, String> {

}
