package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectActivityResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectActivityEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectActivityModel;
import ca.bc.gov.nrs.wfprev.data.models.SpatialExportModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectActivityRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpatialExportService {

    public static final long MAX_RESPONSE_BYTES = 10L * 1024 * 1024; // 10 MB

    @PersistenceContext
    private final EntityManager entityManager;

    private final ProjectActivityRepository projectActivityRepository;
    private final ProjectActivityResourceAssembler projectActivityResourceAssembler;
    private final ObjectMapper objectMapper;

    /**
     * Reads the current database clock in UTC at the start of the request.
     */
    public Instant fetchDatabaseClock() {
        try {
            Query query = entityManager.createNativeQuery("SELECT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')");
            Timestamp ts = (Timestamp) query.getSingleResult();
            return ts.toLocalDateTime().toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            log.error("Could not fetch database clock: {}", e.getMessage(), e);
            throw new RuntimeException("Could not fetch database clock", e);
        }
    }

    @Transactional(readOnly = true)
    public SpatialExportModel getSpatialExport(Instant since, UUID afterProjectGuid) {
        if (since == null) {
            throw new IllegalArgumentException("'since' parameter is required");
        }

        // 1. Database clock at the start of the request
        Instant databaseClock = fetchDatabaseClock();

        // 2. Query changed activities
        List<ProjectActivityEntity> entities = projectActivityRepository.findByLastUpdatedAndProjectGuidAfter(since,
                afterProjectGuid);

        // 3. Aggregate entities by project and limit response size to 10MB
        List<ProjectActivityModel> models = new ArrayList<>();
        // Overhead of JSON envelope
        // {"projectActivities":[],"since":"2026-10-30T18:23:19.000000Z"} is approx 100
        // bytes
        long totalBytes = 100L;
        Instant nextSince = databaseClock;
        UUID nextAfterProjectGuid = null;
        boolean truncated = false;

        List<ProjectActivityModel> aggregatedModels = projectActivityResourceAssembler.toModels(entities);

        for (ProjectActivityModel model : aggregatedModels) {
            try {
                byte[] modelBytes = objectMapper.writeValueAsBytes(model);
                long additionalBytes = modelBytes.length + (models.isEmpty() ? 0 : 1); // 1 byte for comma separator
                if (totalBytes + additionalBytes > MAX_RESPONSE_BYTES && !models.isEmpty()) {
                    truncated = true;
                    break;
                }
                models.add(model);
                totalBytes += additionalBytes;
            } catch (JsonProcessingException e) {
                log.error("Error serializing ProjectActivityModel: {}", e.getMessage(), e);
                throw new RuntimeException("Error serializing ProjectActivityModel", e);
            }
        }

        if (truncated && !models.isEmpty()) {
            ProjectActivityModel lastModel = models.get(models.size() - 1);
            nextSince = lastModel.getLastUpdated();
            nextAfterProjectGuid = lastModel.getProjectGuid();
        }

        return SpatialExportModel.builder()
                .projectActivities(models)
                .since(nextSince)
                .afterProjectGuid(nextAfterProjectGuid)
                .build();
    }
}
