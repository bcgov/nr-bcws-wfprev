package ca.bc.gov.nrs.wfprev.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "file_attachment")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileAttachmentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "file_attachment_guid", updatable = false, nullable = false)
    private UUID fileAttachmentGuid;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_object_name_code")
    private SourceObjectNameCodeEntity sourceObjectNameCode;

    @NotNull
    @Column(name = "source_object_unique_id", length = 50, nullable = false)
    private String sourceObjectUniqueId;

    @NotNull
    @Column(name = "document_path", length = 2000, nullable = false)
    private String documentPath;

    @Column(name = "file_identifier", length = 256)
    private String fileIdentifier;

    @Column(name = "wildfire_year", precision = 4)
    private Integer wildfireYear;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attachment_content_type_code")
    private AttachmentContentTypeCodeEntity attachmentContentTypeCode;

    @Column(name = "attachment_description", length = 150)
    private String attachmentDescription;

    @NotNull
    @Column(name = "attachment_read_only_ind", nullable = false)
    private Boolean attachmentReadOnlyInd;

    @Column(name = "uploaded_by_user_type", length = 3)
    private String uploadedByUserType;

    @Column(name = "uploaded_by_userid", length = 100)
    private String uploadedByUserId;

    @Column(name = "uploaded_by_user_guid", length = 32)
    private String uploadedByUserGuid;

    @Column(name = "uploaded_by_timestamp")
    private Date uploadedByTimestamp;

    @NotNull
    @Column(name = "revision_count", columnDefinition = "numeric(10) default '0'")
    private Integer revisionCount;

    @CreatedBy
    @NotNull
    @Column(name = "create_user", length = 64, nullable = false)
    private String createUser;

    @CreatedDate
    @NotNull
    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @LastModifiedBy
    @NotNull
    @Column(name = "update_user", length = 64, nullable = false)
    private String updateUser;

    @LastModifiedDate
    @NotNull
    @Column(name = "update_date", nullable = false)
    private Date updateDate;


}
