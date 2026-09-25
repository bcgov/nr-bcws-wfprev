package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.ReportExportJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Every status change is a conditional update that returns the number of rows changed. Several API
 * tasks run jobs and the sweep at the same time, so a writer that gets 0 back has lost the race and
 * must stop rather than overwrite the row.
 */
@Repository
@RepositoryRestResource(exported = false)
public interface ReportExportJobRepository extends JpaRepository<ReportExportJobEntity, UUID> {

    Optional<ReportExportJobEntity> findByReportExportJobGuidAndOwnerUserId(UUID reportExportJobGuid, String ownerUserId);

    List<ReportExportJobEntity> findByOwnerUserIdAndDismissedIndFalseAndRequestTimestampAfterOrderByRequestTimestampDesc(
            String ownerUserId, LocalDateTime since);

    /** Started jobs that are still preparing. */
    List<ReportExportJobEntity> findByStatusCodeAndStartedTimestampBefore(String statusCode, LocalDateTime cutoff);

    /** Jobs that no worker has claimed. */
    List<ReportExportJobEntity> findByStatusCodeAndStartedTimestampIsNullAndRequestTimestampBefore(
            String statusCode, LocalDateTime cutoff);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ReportExportJobEntity j SET j.startedTimestamp = :now, "
            + "j.revisionCount = j.revisionCount + 1, j.updateUser = :user, j.updateDate = :updateDate "
            + "WHERE j.reportExportJobGuid = :guid AND j.statusCode = 'PREPARING' AND j.startedTimestamp IS NULL")
    int claim(@Param("guid") UUID guid, @Param("now") LocalDateTime now,
              @Param("user") String user, @Param("updateDate") Date updateDate);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ReportExportJobEntity j SET j.statusCode = 'READY', j.s3ObjectKey = :key, j.completedTimestamp = :now, "
            + "j.revisionCount = j.revisionCount + 1, j.updateUser = :user, j.updateDate = :updateDate "
            + "WHERE j.reportExportJobGuid = :guid AND j.statusCode = 'PREPARING'")
    int markReady(@Param("guid") UUID guid, @Param("key") String key, @Param("now") LocalDateTime now,
                  @Param("user") String user, @Param("updateDate") Date updateDate);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ReportExportJobEntity j SET j.statusCode = 'NO_FILES', j.completedTimestamp = :now, "
            + "j.revisionCount = j.revisionCount + 1, j.updateUser = :user, j.updateDate = :updateDate "
            + "WHERE j.reportExportJobGuid = :guid AND j.statusCode = 'PREPARING'")
    int markNoFiles(@Param("guid") UUID guid, @Param("now") LocalDateTime now,
                    @Param("user") String user, @Param("updateDate") Date updateDate);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ReportExportJobEntity j SET j.statusCode = 'FAILED', j.errorCode = :errorCode, "
            + "j.errorMessage = :errorMessage, j.retryableInd = :retryable, j.completedTimestamp = :now, "
            + "j.revisionCount = j.revisionCount + 1, j.updateUser = :user, j.updateDate = :updateDate "
            + "WHERE j.reportExportJobGuid = :guid AND j.statusCode = 'PREPARING'")
    int markFailed(@Param("guid") UUID guid, @Param("errorCode") String errorCode,
                   @Param("errorMessage") String errorMessage, @Param("retryable") Boolean retryable,
                   @Param("now") LocalDateTime now, @Param("user") String user, @Param("updateDate") Date updateDate);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ReportExportJobEntity j SET j.downloadedTimestamp = :now, "
            + "j.revisionCount = j.revisionCount + 1, j.updateUser = :user, j.updateDate = :updateDate "
            + "WHERE j.reportExportJobGuid = :guid AND j.downloadedTimestamp IS NULL")
    int markDownloaded(@Param("guid") UUID guid, @Param("now") LocalDateTime now,
                       @Param("user") String user, @Param("updateDate") Date updateDate);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ReportExportJobEntity j SET j.dismissedInd = true, "
            + "j.revisionCount = j.revisionCount + 1, j.updateUser = :user, j.updateDate = :updateDate "
            + "WHERE j.reportExportJobGuid = :guid AND j.dismissedInd = false")
    int dismiss(@Param("guid") UUID guid, @Param("user") String user, @Param("updateDate") Date updateDate);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ReportExportJobEntity j SET j.dismissedInd = true, "
            + "j.revisionCount = j.revisionCount + 1, j.updateUser = :user, j.updateDate = :updateDate "
            + "WHERE j.ownerUserId = :owner AND j.statusCode <> 'PREPARING' AND j.dismissedInd = false")
    int dismissFinished(@Param("owner") String owner, @Param("user") String user, @Param("updateDate") Date updateDate);
}
