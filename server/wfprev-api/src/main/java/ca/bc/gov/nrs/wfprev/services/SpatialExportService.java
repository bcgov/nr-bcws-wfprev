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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            return ts.toInstant();
        } catch (Exception e) {
            log.warn("Could not fetch database clock, falling back to system clock: {}", e.getMessage());
            return Instant.now();
        }
    }

    @Transactional(readOnly = true)
    public SpatialExportModel getSpatialExport(Date since) {
        if (since == null) {
            throw new IllegalArgumentException("'since' parameter is required");
        }

        // 1. Database clock at the start of the request
        Instant databaseClock = fetchDatabaseClock();
        Date timestamp = Date.from(databaseClock);

        // 2. Query changed activities
        List<ProjectActivityEntity> entities = projectActivityRepository.findByLastUpdatedGreaterThanEqualOrderByLastUpdatedAsc(since);

        // 3. Convert and limit response size to 10MB
        List<ProjectActivityModel> models = new ArrayList<>();
        // Overhead of JSON envelope {"projectActivities":[],"since":"2026-10-30T18:23:19.000+00:00"} is approx 100 bytes
        long totalBytes = 100L;
        Date nextSince = timestamp;
        boolean truncated = false;

        for (ProjectActivityEntity entity : entities) {
            ProjectActivityModel model = projectActivityResourceAssembler.toModel(entity);
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
            }
        }

        if (truncated && !models.isEmpty()) {
            nextSince = models.get(models.size() - 1).getLastUpdated();
        }

        return SpatialExportModel.builder()
                .projectActivities(models)
                .since(nextSince)
                .build();
    }
}
