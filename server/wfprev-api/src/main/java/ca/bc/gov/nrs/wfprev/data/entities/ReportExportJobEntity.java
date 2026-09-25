package ca.bc.gov.nrs.wfprev.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * One file produced by a report download request. Status changes after the insert are made only
 * through the conditional updates in {@code ReportExportJobRepository}, never by saving this entity.
 * Timestamps are UTC.
 */
@Entity
@Table(name = "report_export_job", schema = "wfprev")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportExportJobEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "report_export_job_guid", updatable = false, nullable = false)
    private UUID reportExportJobGuid;

    @NotNull
    @Column(name = "export_group_guid", nullable = false)
    private UUID exportGroupGuid;

    @Column(name = "retried_from_job_guid")
    private UUID retriedFromJobGuid;

    @NotNull
    @Column(name = "owner_user_id", length = 64, nullable = false)
    private String ownerUserId;

    @NotNull
    @Column(name = "report_type_code", length = 32, nullable = false)
    private String reportTypeCode;

    @NotNull
    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "request_json", nullable = false, columnDefinition = "jsonb")
    private String requestJson;

    @NotNull
    @Column(name = "description", length = 4000, nullable = false)
    private String description;

    @NotNull
    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @NotNull
    @Column(name = "status_code", length = 16, nullable = false)
    private String statusCode;

    @Column(name = "s3_object_key", length = 1024)
    private String s3ObjectKey;

    @Column(name = "error_code", length = 16)
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "retryable_ind")
    private Boolean retryableInd;

    @NotNull
    @Column(name = "request_timestamp", nullable = false)
    private LocalDateTime requestTimestamp;

    @Column(name = "started_timestamp")
    private LocalDateTime startedTimestamp;

    @Column(name = "completed_timestamp")
    private LocalDateTime completedTimestamp;

    @Column(name = "downloaded_timestamp")
    private LocalDateTime downloadedTimestamp;

    @NotNull
    @Builder.Default
    @Column(name = "dismissed_ind", nullable = false)
    private Boolean dismissedInd = false;

    @NotNull
    @Version
    @Builder.Default
    @Column(name = "revision_count", nullable = false)
    private Integer revisionCount = 0;

    @NotNull
    @Column(name = "create_user", length = 64, nullable = false)
    private String createUser;

    @NotNull
    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @NotNull
    @Column(name = "update_user", length = 64, nullable = false)
    private String updateUser;

    @NotNull
    @Column(name = "update_date", nullable = false)
    private Date updateDate;
}
