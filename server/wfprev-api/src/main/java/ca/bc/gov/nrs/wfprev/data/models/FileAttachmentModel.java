package ca.bc.gov.nrs.wfprev.data.models;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "fileAttachment")
@Relation(collectionRelation = "fileAttachment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileAttachmentModel extends CommonModel<FileAttachmentModel> {
    private String fileAttachmentGuid;
    @NotNull(message = "FileAttachment sourceObjectNameCode must not be null")
    private SourceObjectNameCodeModel sourceObjectNameCode;
    @NotNull(message = "FileAttachment sourceObjectUniqueId must not be null")
    private String sourceObjectUniqueId;
    @NotNull(message = "FileAttachment documentPath must not be null")
    private String documentPath;
    private String fileIdentifier;
    private Integer wildfireYear;
    @NotNull(message = "FileAttachment attachmentContentTypeCode must not be null")
    private AttachmentContentTypeCodeModel attachmentContentTypeCode;
    private String attachmentDescription;
    @NotNull(message = "FileAttachment attachmentReadOnlyInd must not be null")
    private Boolean attachmentReadOnlyInd;
    private String uploadedByUserType;
    private String uploadedByUserId;
    private String uploadedByUserGuid;
    private Date uploadedByTimestamp;
}
