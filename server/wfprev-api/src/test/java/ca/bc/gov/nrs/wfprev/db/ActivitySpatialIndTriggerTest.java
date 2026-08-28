package ca.bc.gov.nrs.wfprev.db;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * WFPREV-1223.
 *
 * {@code activity.is_spatial_added_ind} is maintained by a database trigger
 * rather than by
 * application code, which means Mockito cannot reach it. This exercises the
 * real trigger SQL -
 * the same file changeset 01_03_00_09 ships - against a real PostgreSQL
 * instance.
 *
 * The script is executed in one shot, exactly as Liquibase does with
 * {@code "splitStatements": false}, so a change that breaks dollar-quoting
 * fails here too.
 *
 * The schema is deliberately minimal: only the two tables and columns the
 * trigger touches.
 * This covers the trigger's behaviour, not the application of the wider
 * migration history.
 */
@Testcontainers
class ActivitySpatialIndTriggerTest {

    private static final Path TRIGGER_SQL = Path.of(
            "../../db/scripts/01_03_00/09/ddl/app_wf1_prev.ddl.activity_spatial_ind_trigger_wfprev-1223.sql");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("wfprev")
            .withUsername("wfprev")
            .withPassword("password");

    private static Connection connection;

    @BeforeAll
    static void applySchemaAndTrigger() throws Exception {
        connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());

        try (Statement st = connection.createStatement()) {
            st.execute("""
                    CREATE SCHEMA IF NOT EXISTS wfprev;

                    CREATE TABLE wfprev.activity (
                      activity_guid        UUID PRIMARY KEY,
                      is_spatial_added_ind BOOLEAN NOT NULL DEFAULT FALSE,
                      revision_count       INTEGER NOT NULL DEFAULT 0
                    );

                    CREATE TABLE wfprev.activity_boundary (
                      activity_boundary_guid UUID PRIMARY KEY,
                      activity_guid          UUID NOT NULL REFERENCES wfprev.activity(activity_guid)
                    );
                    """);

            // the shipped migration file, executed as a single script
            st.execute(Files.readString(TRIGGER_SQL));
        }
    }

    @BeforeEach
    void reset() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute("DELETE FROM wfprev.activity_boundary; DELETE FROM wfprev.activity;");
        }
    }

    @Test
    void deletingTheOnlyBoundaryClearsTheFlag() throws Exception {
        UUID activity = givenActivity();
        UUID boundary = givenBoundary(activity);

        assertTrue(flagOf(activity), "inserting a boundary should set the flag");

        execute("DELETE FROM wfprev.activity_boundary WHERE activity_boundary_guid = '" + boundary + "'");

        assertFalse(flagOf(activity), "removing the last boundary must clear the flag");
    }

    @Test
    void deletingOneOfTwoBoundariesLeavesTheFlagSet() throws Exception {
        UUID activity = givenActivity();
        UUID first = givenBoundary(activity);
        givenBoundary(activity);

        execute("DELETE FROM wfprev.activity_boundary WHERE activity_boundary_guid = '" + first + "'");

        assertTrue(flagOf(activity), "a remaining boundary must keep the flag set");
    }

    @Test
    void insertingABoundarySetsTheFlag() throws Exception {
        UUID activity = givenActivity();
        assertFalse(flagOf(activity));

        givenBoundary(activity);

        assertTrue(flagOf(activity));
    }

    @Test
    void theFlagIsNotRewrittenWhenNothingChanges() throws Exception {
        UUID activity = givenActivity();
        givenBoundary(activity);

        // xmin is the transaction that last wrote the row: if the guard works, a second
        // boundary insert leaves the activity row physically untouched.
        String before = xminOf(activity);
        givenBoundary(activity);
        String after = xminOf(activity);

        assertEquals(before, after,
                "the flag was already true, so the activity row should not have been rewritten");
    }

    @Test
    void movingABoundaryBetweenActivitiesCorrectsBothSides() throws Exception {
        UUID from = givenActivity();
        UUID to = givenActivity();
        UUID boundary = givenBoundary(from);

        assertTrue(flagOf(from));
        assertFalse(flagOf(to));

        execute("UPDATE wfprev.activity_boundary SET activity_guid = '" + to + "' "
                + "WHERE activity_boundary_guid = '" + boundary + "'");

        assertFalse(flagOf(from), "the activity that lost the boundary must be cleared");
        assertTrue(flagOf(to), "the activity that gained it must be set");
    }

    @Test
    void revisionCountIsNotTouched() throws Exception {
        UUID activity = givenActivity();
        UUID boundary = givenBoundary(activity);

        // revision_count is Hibernate's @Version. If the trigger bumped it, every
        // spatial
        // upload would hand a 409 to anyone holding the activity open in the UI.
        assertEquals(0, revisionCountOf(activity), "insert must not bump the version");

        execute("DELETE FROM wfprev.activity_boundary WHERE activity_boundary_guid = '" + boundary + "'");

        assertEquals(0, revisionCountOf(activity), "delete must not bump the version");
    }

    @Test
    void theTriggerFiresForWritesThatBypassTheApplication() throws Exception {
        // The whole point of moving this into the database: correctness does not depend
        // on
        // which code path made the change.
        UUID activity = givenActivity();
        execute("UPDATE wfprev.activity SET is_spatial_added_ind = TRUE WHERE activity_guid = '" + activity + "'");
        assertTrue(flagOf(activity), "sanity: direct write took effect");

        givenBoundary(activity);
        execute("DELETE FROM wfprev.activity_boundary WHERE activity_guid = '" + activity + "'");

        assertFalse(flagOf(activity), "a raw SQL delete must still correct the flag");
    }

    // --- helpers ---

    private UUID givenActivity() throws Exception {
        UUID guid = UUID.randomUUID();
        execute("INSERT INTO wfprev.activity (activity_guid) VALUES ('" + guid + "')");
        return guid;
    }

    private UUID givenBoundary(UUID activityGuid) throws Exception {
        UUID guid = UUID.randomUUID();
        execute("INSERT INTO wfprev.activity_boundary (activity_boundary_guid, activity_guid) "
                + "VALUES ('" + guid + "', '" + activityGuid + "')");
        return guid;
    }

    private void execute(String sql) throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        }
    }

    private boolean flagOf(UUID activityGuid) throws Exception {
        // read as a boolean, not a String: the PostgreSQL driver renders booleans as
        // "t"/"f",
        // which Boolean.parseBoolean silently turns into false
        String sql = "SELECT is_spatial_added_ind FROM wfprev.activity WHERE activity_guid = '" + activityGuid + "'";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            assertTrue(rs.next(), "expected a row for: " + sql);
            return rs.getBoolean(1);
        }
    }

    private int revisionCountOf(UUID activityGuid) throws Exception {
        return Integer.parseInt(
                scalar("SELECT revision_count FROM wfprev.activity WHERE activity_guid = '" + activityGuid + "'"));
    }

    private String xminOf(UUID activityGuid) throws Exception {
        return scalar("SELECT xmin::text FROM wfprev.activity WHERE activity_guid = '" + activityGuid + "'");
    }

    private String scalar(String sql) throws Exception {
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            assertTrue(rs.next(), "expected a row for: " + sql);
            return rs.getString(1);
        }
    }
}
