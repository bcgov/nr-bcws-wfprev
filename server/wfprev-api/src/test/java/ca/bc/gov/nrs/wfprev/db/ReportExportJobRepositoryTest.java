package ca.bc.gov.nrs.wfprev.db;

import ca.bc.gov.nrs.wfprev.data.entities.ReportExportJobEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ReportExportJobRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The conditional updates are what keep several API tasks, and the sweep, from overwriting each
 * other. This runs them against the table created by the shipped DDL script.
 */
@Testcontainers
@DataJpaTest(properties = {"spring.liquibase.enabled=false", "spring.jpa.hibernate.ddl-auto=none"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ReportExportJobRepositoryTest {

    private static final Path DDL = Path.of("../../db/scripts/01_04_00/02/ddl/WFPREV.ddl.report_export_job.sql");
    private static final String USER = "WFPREV_REPORT_JOBS";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 24, 21, 14, 3);

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("wfprev")
            .withUsername("wfprev")
            .withPassword("password");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @MockBean(name = "springSecurityAuditorAware")
    private AuditorAware<String> auditorAware;

    @Autowired
    private ReportExportJobRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeAll
    static void createTable() throws Exception {
        try (Connection connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement st = connection.createStatement()) {
            st.execute("CREATE SCHEMA IF NOT EXISTS wfprev");
            st.execute(Files.readString(DDL));
        }
    }

    @BeforeEach
    void clean() {
        jdbc.execute("DELETE FROM wfprev.report_export_job");
    }

    private ReportExportJobEntity save(String owner, String status, LocalDateTime requested) {
        Date today = new Date();
        return repository.save(ReportExportJobEntity.builder()
                .reportExportJobGuid(UUID.randomUUID())
                .exportGroupGuid(UUID.randomUUID())
                .ownerUserId(owner)
                .reportTypeCode("RESULTS_XLSX")
                .requestJson("{\"reportType\":\"RESULTS_XLSX\",\"projectFilter\":{\"fiscalYears\":[\"2026\"]}}")
                .description("FY 2026/27")
                .fileName("ReMi_RESULTS.xlsx")
                .statusCode(status)
                .requestTimestamp(requested)
                .createUser(owner).createDate(today).updateUser(owner).updateDate(today)
                .build());
    }

    private ReportExportJobEntity reload(UUID guid) {
        return repository.findById(guid).orElseThrow();
    }

    @Test
    void requestJson_isStoredAsJsonb() {
        ReportExportJobEntity job = save("owner", "PREPARING", NOW);

        String type = jdbc.queryForObject("SELECT jsonb_typeof(request_json -> 'projectFilter') FROM wfprev.report_export_job "
                + "WHERE report_export_job_guid = ?", String.class, job.getReportExportJobGuid());
        assertEquals("object", type);
        assertTrue(reload(job.getReportExportJobGuid()).getRequestJson().contains("\"fiscalYears\""));
    }

    @Test
    void claim_succeedsOnce() {
        UUID guid = save("owner", "PREPARING", NOW).getReportExportJobGuid();

        assertEquals(1, repository.claim(guid, NOW, USER, new Date()));
        assertEquals(0, repository.claim(guid, NOW.plusSeconds(1), USER, new Date()), "a second worker must not run it");
        assertEquals(NOW, reload(guid).getStartedTimestamp());
    }

    @Test
    void claim_ofAFinishedJob_fails() {
        UUID guid = save("owner", "FAILED", NOW).getReportExportJobGuid();

        assertEquals(0, repository.claim(guid, NOW, USER, new Date()));
    }

    @Test
    void markReady_thenALateFailure_keepsReady() {
        UUID guid = save("owner", "PREPARING", NOW).getReportExportJobGuid();

        assertEquals(1, repository.markReady(guid, "jobs/" + guid + "/ReMi_RESULTS.xlsx", NOW, USER, new Date()));
        assertEquals(0, repository.markFailed(guid, "TEMPORARY", "Something went wrong. Try again.", true, NOW, USER, new Date()));

        ReportExportJobEntity job = reload(guid);
        assertEquals("READY", job.getStatusCode());
        assertEquals("jobs/" + guid + "/ReMi_RESULTS.xlsx", job.getS3ObjectKey());
        assertEquals(NOW, job.getCompletedTimestamp());
        assertNull(job.getErrorCode());
    }

    @Test
    void markFailed_thenALateReady_keepsFailed() {
        // The sweep failed the job; the worker it gave up on finishes afterwards.
        UUID guid = save("owner", "PREPARING", NOW).getReportExportJobGuid();

        assertEquals(1, repository.markFailed(guid, "TEMPORARY", "Something went wrong. Try again.", true, NOW, USER, new Date()));
        assertEquals(0, repository.markReady(guid, "jobs/x", NOW, USER, new Date()));
        assertEquals(0, repository.markNoFiles(guid, NOW, USER, new Date()));

        ReportExportJobEntity job = reload(guid);
        assertEquals("FAILED", job.getStatusCode());
        assertEquals("TEMPORARY", job.getErrorCode());
        assertEquals(Boolean.TRUE, job.getRetryableInd());
    }

    @Test
    void statusChanges_bumpTheRevisionCount() {
        UUID guid = save("owner", "PREPARING", NOW).getReportExportJobGuid();

        repository.claim(guid, NOW, USER, new Date());
        repository.markNoFiles(guid, NOW, USER, new Date());

        ReportExportJobEntity job = reload(guid);
        assertEquals(2, job.getRevisionCount());
        assertEquals(USER, job.getUpdateUser());
        assertEquals("NO_FILES", job.getStatusCode());
    }

    @Test
    void markDownloaded_keepsTheFirstTime() {
        UUID guid = save("owner", "READY", NOW).getReportExportJobGuid();

        assertEquals(1, repository.markDownloaded(guid, NOW, "owner", new Date()));
        assertEquals(0, repository.markDownloaded(guid, NOW.plusMinutes(5), "owner", new Date()));
        assertEquals(NOW, reload(guid).getDownloadedTimestamp());
    }

    @Test
    void list_returnsTheOwnersUndismissedRecentJobsNewestFirst() {
        ReportExportJobEntity older = save("owner", "READY", NOW.minusHours(3));
        ReportExportJobEntity newer = save("owner", "PREPARING", NOW.minusHours(1));
        save("owner", "READY", NOW.minusHours(49));
        save("someone-else", "READY", NOW.minusHours(1));
        ReportExportJobEntity dismissed = save("owner", "FAILED", NOW.minusHours(2));
        repository.dismiss(dismissed.getReportExportJobGuid(), "owner", new Date());

        List<ReportExportJobEntity> jobs = repository
                .findByOwnerUserIdAndDismissedIndFalseAndRequestTimestampAfterOrderByRequestTimestampDesc("owner", NOW.minusHours(48));

        assertEquals(List.of(newer.getReportExportJobGuid(), older.getReportExportJobGuid()),
                jobs.stream().map(ReportExportJobEntity::getReportExportJobGuid).toList());
    }

    @Test
    void dismissFinished_leavesJobsStillPreparing() {
        ReportExportJobEntity preparing = save("owner", "PREPARING", NOW);
        ReportExportJobEntity ready = save("owner", "READY", NOW);
        ReportExportJobEntity failed = save("owner", "FAILED", NOW);
        ReportExportJobEntity otherOwner = save("someone-else", "READY", NOW);

        assertEquals(2, repository.dismissFinished("owner", "owner", new Date()));

        assertEquals(false, reload(preparing.getReportExportJobGuid()).getDismissedInd());
        assertEquals(true, reload(ready.getReportExportJobGuid()).getDismissedInd());
        assertEquals(true, reload(failed.getReportExportJobGuid()).getDismissedInd());
        assertEquals(false, reload(otherOwner.getReportExportJobGuid()).getDismissedInd());
    }

    @Test
    void sweepQueries_findStartedAndUnstartedJobsSeparately() {
        ReportExportJobEntity startedLongAgo = save("owner", "PREPARING", NOW.minusMinutes(30));
        repository.claim(startedLongAgo.getReportExportJobGuid(), NOW.minusMinutes(29), USER, new Date());
        ReportExportJobEntity startedRecently = save("owner", "PREPARING", NOW.minusMinutes(2));
        repository.claim(startedRecently.getReportExportJobGuid(), NOW.minusMinutes(1), USER, new Date());
        ReportExportJobEntity unstarted = save("owner", "PREPARING", NOW.minusMinutes(15));
        save("owner", "READY", NOW.minusMinutes(40));

        List<ReportExportJobEntity> started = repository.findByStatusCodeAndStartedTimestampBefore("PREPARING", NOW.minusMinutes(10));
        List<ReportExportJobEntity> neverStarted = repository
                .findByStatusCodeAndStartedTimestampIsNullAndRequestTimestampBefore("PREPARING", NOW.minusMinutes(10));

        assertEquals(List.of(startedLongAgo.getReportExportJobGuid()),
                started.stream().map(ReportExportJobEntity::getReportExportJobGuid).toList());
        assertEquals(List.of(unstarted.getReportExportJobGuid()),
                neverStarted.stream().map(ReportExportJobEntity::getReportExportJobGuid).toList());
        assertNotNull(reload(startedLongAgo.getReportExportJobGuid()).getStartedTimestamp());
    }
}
