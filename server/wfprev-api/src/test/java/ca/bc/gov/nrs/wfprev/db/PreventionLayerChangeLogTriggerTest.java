package ca.bc.gov.nrs.wfprev.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * WFPREV-1307.
 *
 * Exercises wfprev.prevention_layer_change_log table, triggers, and backfill script.
 * Validates that inserts, updates, and deletes across project, project_boundary,
 * project_plan_fiscal, activity, and activity_boundary update prevention_layer_change_log,
 * and that the backfill correctly populates rows using the latest update_date.
 */
@Testcontainers
class PreventionLayerChangeLogTriggerTest {

    private static final Path TABLE_SQL = Path.of(
            "../../db/scripts/01_05_00/01/ddl/WFPREV.ddl.prevention_layer_change_log.sql");
    private static final Path TRIGGER_SQL = Path.of(
            "../../db/scripts/01_05_00/01/ddl/WFPREV.ddl.prevention_layer_change_log_trigger.sql");
    private static final Path BACKFILL_SQL = Path.of(
            "../../db/scripts/01_05_00/01/dml/WFPREV.dml.prevention_layer_change_log_backfill.sql");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:17-3.6").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("wfprev")
            .withUsername("wfprev")
            .withPassword("password");

    private static Connection connection;

    @BeforeAll
    static void setUp() throws Exception {
        connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());

        try (Statement st = connection.createStatement()) {
            st.execute("""
                    CREATE SCHEMA IF NOT EXISTS wfprev;

                    CREATE TABLE wfprev.project (
                      project_guid UUID PRIMARY KEY,
                      project_name VARCHAR(300),
                      create_date  DATE,
                      update_date  DATE
                    );

                    CREATE TABLE wfprev.project_boundary (
                      project_boundary_guid UUID PRIMARY KEY,
                      project_guid          UUID NOT NULL REFERENCES wfprev.project(project_guid),
                      create_date           DATE,
                      update_date           DATE
                    );

                    CREATE TABLE wfprev.project_plan_fiscal (
                      project_plan_fiscal_guid UUID PRIMARY KEY,
                      project_guid             UUID NOT NULL REFERENCES wfprev.project(project_guid),
                      create_date              DATE,
                      update_date              DATE
                    );

                    CREATE TABLE wfprev.activity (
                      activity_guid            UUID PRIMARY KEY,
                      project_plan_fiscal_guid UUID NOT NULL REFERENCES wfprev.project_plan_fiscal(project_plan_fiscal_guid),
                      create_date              DATE,
                      update_date              DATE
                    );

                    CREATE TABLE wfprev.activity_boundary (
                      activity_boundary_guid UUID PRIMARY KEY,
                      activity_guid          UUID NOT NULL REFERENCES wfprev.activity(activity_guid),
                      create_date            DATE,
                      update_date            DATE
                    );
                    """);

            st.execute(Files.readString(TABLE_SQL));
            st.execute(Files.readString(TRIGGER_SQL));
        }
    }

    @BeforeEach
    void reset() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                    DELETE FROM wfprev.activity_boundary;
                    DELETE FROM wfprev.activity;
                    DELETE FROM wfprev.project_plan_fiscal;
                    DELETE FROM wfprev.project_boundary;
                    DELETE FROM wfprev.project;
                    DELETE FROM wfprev.prevention_layer_change_log;
                    """);
        }
    }

    @Test
    void project_insertUpdateDelete_updatesChangeLog() throws Exception {
        UUID projectGuid = UUID.randomUUID();

        // Insert
        Timestamp beforeInsert = getDbCurrentTimestamp();
        execute("INSERT INTO wfprev.project (project_guid, project_name) VALUES ('" + projectGuid + "', 'Initial Name')");
        Timestamp afterInsert = getDbCurrentTimestamp();

        Timestamp tsInsert = getLastUpdated(projectGuid);
        assertNotNull(tsInsert, "Project insert must record a change log row");
        assertTrue(!tsInsert.before(beforeInsert) && !tsInsert.after(afterInsert),
                String.format("last_updated (%s) must be between %s and %s", tsInsert, beforeInsert, afterInsert));

        // Update
        Timestamp beforeUpdate = getDbCurrentTimestamp();
        execute("UPDATE wfprev.project SET project_name = 'Updated Name' WHERE project_guid = '" + projectGuid + "'");
        Timestamp afterUpdate = getDbCurrentTimestamp();

        Timestamp tsUpdate = getLastUpdated(projectGuid);
        assertNotNull(tsUpdate, "Project update must update change log");
        assertTrue(!tsUpdate.before(beforeUpdate) && !tsUpdate.after(afterUpdate),
                String.format("last_updated (%s) must be between %s and %s", tsUpdate, beforeUpdate, afterUpdate));

        // Delete (row must survive project hard delete)
        Timestamp beforeDelete = getDbCurrentTimestamp();
        execute("DELETE FROM wfprev.project WHERE project_guid = '" + projectGuid + "'");
        Timestamp afterDelete = getDbCurrentTimestamp();

        Timestamp tsDelete = getLastUpdated(projectGuid);
        assertNotNull(tsDelete, "Change log row must survive project hard delete");
        assertTrue(!tsDelete.before(beforeDelete) && !tsDelete.after(afterDelete),
                String.format("last_updated (%s) must be between %s and %s", tsDelete, beforeDelete, afterDelete));
    }

    @Test
    void defaultLastUpdated_isUtcTimestamp() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        Timestamp beforeInsert = getDbCurrentTimestamp();
        execute("INSERT INTO wfprev.prevention_layer_change_log (project_guid) VALUES ('" + projectGuid + "')");
        Timestamp afterInsert = getDbCurrentTimestamp();

        Timestamp tsInsert = getLastUpdated(projectGuid);
        assertNotNull(tsInsert, "Direct insert without last_updated must populate default timestamp");
        assertTrue(!tsInsert.before(beforeInsert) && !tsInsert.after(afterInsert),
                String.format("default last_updated (%s) must be between %s and %s", tsInsert, beforeInsert, afterInsert));
    }

    @Test
    void projectBoundary_insertUpdateDelete_updatesChangeLog() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        execute("INSERT INTO wfprev.project (project_guid, project_name) VALUES ('" + projectGuid + "', 'Boundary Test')");

        UUID boundaryGuid = UUID.randomUUID();
        Timestamp beforeInsert = getDbCurrentTimestamp();
        execute("INSERT INTO wfprev.project_boundary (project_boundary_guid, project_guid) VALUES ('" + boundaryGuid + "', '" + projectGuid + "')");
        Timestamp afterInsert = getDbCurrentTimestamp();

        Timestamp tsInsert = getLastUpdated(projectGuid);
        assertNotNull(tsInsert, "Boundary insert must update change log");
        assertTrue(!tsInsert.before(beforeInsert) && !tsInsert.after(afterInsert),
                String.format("last_updated (%s) must be between %s and %s", tsInsert, beforeInsert, afterInsert));

        Timestamp beforeUpdate = getDbCurrentTimestamp();
        execute("UPDATE wfprev.project_boundary SET update_date = CURRENT_DATE WHERE project_boundary_guid = '" + boundaryGuid + "'");
        Timestamp afterUpdate = getDbCurrentTimestamp();

        Timestamp tsUpdate = getLastUpdated(projectGuid);
        assertNotNull(tsUpdate, "Boundary update must update change log");
        assertTrue(!tsUpdate.before(beforeUpdate) && !tsUpdate.after(afterUpdate),
                String.format("last_updated (%s) must be between %s and %s", tsUpdate, beforeUpdate, afterUpdate));

        Timestamp beforeDelete = getDbCurrentTimestamp();
        execute("DELETE FROM wfprev.project_boundary WHERE project_boundary_guid = '" + boundaryGuid + "'");
        Timestamp afterDelete = getDbCurrentTimestamp();

        Timestamp tsDelete = getLastUpdated(projectGuid);
        assertNotNull(tsDelete, "Boundary delete must update change log");
        assertTrue(!tsDelete.before(beforeDelete) && !tsDelete.after(afterDelete),
                String.format("last_updated (%s) must be between %s and %s", tsDelete, beforeDelete, afterDelete));
    }

    @Test
    void projectPlanFiscal_insertUpdateDelete_updatesChangeLog() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        execute("INSERT INTO wfprev.project (project_guid, project_name) VALUES ('" + projectGuid + "', 'Fiscal Test')");

        UUID fiscalGuid = UUID.randomUUID();
        Timestamp beforeInsert = getDbCurrentTimestamp();
        execute("INSERT INTO wfprev.project_plan_fiscal (project_plan_fiscal_guid, project_guid) VALUES ('" + fiscalGuid + "', '" + projectGuid + "')");
        Timestamp afterInsert = getDbCurrentTimestamp();

        Timestamp tsInsert = getLastUpdated(projectGuid);
        assertNotNull(tsInsert, "Fiscal insert must update change log");
        assertTrue(!tsInsert.before(beforeInsert) && !tsInsert.after(afterInsert),
                String.format("last_updated (%s) must be between %s and %s", tsInsert, beforeInsert, afterInsert));

        Timestamp beforeUpdate = getDbCurrentTimestamp();
        execute("UPDATE wfprev.project_plan_fiscal SET update_date = CURRENT_DATE WHERE project_plan_fiscal_guid = '" + fiscalGuid + "'");
        Timestamp afterUpdate = getDbCurrentTimestamp();

        Timestamp tsUpdate = getLastUpdated(projectGuid);
        assertNotNull(tsUpdate, "Fiscal update must update change log");
        assertTrue(!tsUpdate.before(beforeUpdate) && !tsUpdate.after(afterUpdate),
                String.format("last_updated (%s) must be between %s and %s", tsUpdate, beforeUpdate, afterUpdate));

        Timestamp beforeDelete = getDbCurrentTimestamp();
        execute("DELETE FROM wfprev.project_plan_fiscal WHERE project_plan_fiscal_guid = '" + fiscalGuid + "'");
        Timestamp afterDelete = getDbCurrentTimestamp();

        Timestamp tsDelete = getLastUpdated(projectGuid);
        assertNotNull(tsDelete, "Fiscal delete must update change log");
        assertTrue(!tsDelete.before(beforeDelete) && !tsDelete.after(afterDelete),
                String.format("last_updated (%s) must be between %s and %s", tsDelete, beforeDelete, afterDelete));
    }

    @Test
    void activity_insertUpdateDelete_updatesChangeLog() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        execute("INSERT INTO wfprev.project (project_guid, project_name) VALUES ('" + projectGuid + "', 'Activity Test')");

        UUID fiscalGuid = UUID.randomUUID();
        execute("INSERT INTO wfprev.project_plan_fiscal (project_plan_fiscal_guid, project_guid) VALUES ('" + fiscalGuid + "', '" + projectGuid + "')");

        UUID activityGuid = UUID.randomUUID();
        Timestamp beforeInsert = getDbCurrentTimestamp();
        execute("INSERT INTO wfprev.activity (activity_guid, project_plan_fiscal_guid) VALUES ('" + activityGuid + "', '" + fiscalGuid + "')");
        Timestamp afterInsert = getDbCurrentTimestamp();

        Timestamp tsInsert = getLastUpdated(projectGuid);
        assertNotNull(tsInsert, "Activity insert must update change log");
        assertTrue(!tsInsert.before(beforeInsert) && !tsInsert.after(afterInsert),
                String.format("last_updated (%s) must be between %s and %s", tsInsert, beforeInsert, afterInsert));

        Timestamp beforeUpdate = getDbCurrentTimestamp();
        execute("UPDATE wfprev.activity SET update_date = CURRENT_DATE WHERE activity_guid = '" + activityGuid + "'");
        Timestamp afterUpdate = getDbCurrentTimestamp();

        Timestamp tsUpdate = getLastUpdated(projectGuid);
        assertNotNull(tsUpdate, "Activity update must update change log");
        assertTrue(!tsUpdate.before(beforeUpdate) && !tsUpdate.after(afterUpdate),
                String.format("last_updated (%s) must be between %s and %s", tsUpdate, beforeUpdate, afterUpdate));

        Timestamp beforeDelete = getDbCurrentTimestamp();
        execute("DELETE FROM wfprev.activity WHERE activity_guid = '" + activityGuid + "'");
        Timestamp afterDelete = getDbCurrentTimestamp();

        Timestamp tsDelete = getLastUpdated(projectGuid);
        assertNotNull(tsDelete, "Activity delete must update change log");
        assertTrue(!tsDelete.before(beforeDelete) && !tsDelete.after(afterDelete),
                String.format("last_updated (%s) must be between %s and %s", tsDelete, beforeDelete, afterDelete));
    }

    @Test
    void activityBoundary_insertUpdateDelete_updatesChangeLog() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        execute("INSERT INTO wfprev.project (project_guid, project_name) VALUES ('" + projectGuid + "', 'Act Boundary Test')");

        UUID fiscalGuid = UUID.randomUUID();
        execute("INSERT INTO wfprev.project_plan_fiscal (project_plan_fiscal_guid, project_guid) VALUES ('" + fiscalGuid + "', '" + projectGuid + "')");

        UUID activityGuid = UUID.randomUUID();
        execute("INSERT INTO wfprev.activity (activity_guid, project_plan_fiscal_guid) VALUES ('" + activityGuid + "', '" + fiscalGuid + "')");

        UUID actBoundaryGuid = UUID.randomUUID();
        Timestamp beforeInsert = getDbCurrentTimestamp();
        execute("INSERT INTO wfprev.activity_boundary (activity_boundary_guid, activity_guid) VALUES ('" + actBoundaryGuid + "', '" + activityGuid + "')");
        Timestamp afterInsert = getDbCurrentTimestamp();

        Timestamp tsInsert = getLastUpdated(projectGuid);
        assertNotNull(tsInsert, "Activity boundary insert must update change log");
        assertTrue(!tsInsert.before(beforeInsert) && !tsInsert.after(afterInsert),
                String.format("last_updated (%s) must be between %s and %s", tsInsert, beforeInsert, afterInsert));

        Timestamp beforeUpdate = getDbCurrentTimestamp();
        execute("UPDATE wfprev.activity_boundary SET update_date = CURRENT_DATE WHERE activity_boundary_guid = '" + actBoundaryGuid + "'");
        Timestamp afterUpdate = getDbCurrentTimestamp();

        Timestamp tsUpdate = getLastUpdated(projectGuid);
        assertNotNull(tsUpdate, "Activity boundary update must update change log");
        assertTrue(!tsUpdate.before(beforeUpdate) && !tsUpdate.after(afterUpdate),
                String.format("last_updated (%s) must be between %s and %s", tsUpdate, beforeUpdate, afterUpdate));

        Timestamp beforeDelete = getDbCurrentTimestamp();
        execute("DELETE FROM wfprev.activity_boundary WHERE activity_boundary_guid = '" + actBoundaryGuid + "'");
        Timestamp afterDelete = getDbCurrentTimestamp();

        Timestamp tsDelete = getLastUpdated(projectGuid);
        assertNotNull(tsDelete, "Activity boundary delete must update change log");
        assertTrue(!tsDelete.before(beforeDelete) && !tsDelete.after(afterDelete),
                String.format("last_updated (%s) must be between %s and %s", tsDelete, beforeDelete, afterDelete));
    }

    @Test
    void backfill_seedsRowsForExistingProjects_withLatestUpdateDate() throws Exception {
        UUID project1 = UUID.randomUUID();
        UUID pb1 = UUID.randomUUID();
        UUID ppf1 = UUID.randomUUID();
        UUID act1 = UUID.randomUUID();
        UUID ab1 = UUID.randomUUID();

        // Project 1: activity boundary has the latest update_date (2025-05-15)
        execute(String.format("INSERT INTO wfprev.project (project_guid, project_name, create_date, update_date) VALUES ('%s', 'P1', '2025-01-01', '2025-01-10')", project1));
        execute(String.format("INSERT INTO wfprev.project_boundary (project_boundary_guid, project_guid, create_date, update_date) VALUES ('%s', '%s', '2025-02-01', '2025-02-10')", pb1, project1));
        execute(String.format("INSERT INTO wfprev.project_plan_fiscal (project_plan_fiscal_guid, project_guid, create_date, update_date) VALUES ('%s', '%s', '2025-03-01', '2025-03-10')", ppf1, project1));
        execute(String.format("INSERT INTO wfprev.activity (activity_guid, project_plan_fiscal_guid, create_date, update_date) VALUES ('%s', '%s', '2025-04-01', '2025-04-10')", act1, ppf1));
        execute(String.format("INSERT INTO wfprev.activity_boundary (activity_boundary_guid, activity_guid, create_date, update_date) VALUES ('%s', '%s', '2025-05-01', '2025-05-15')", ab1, act1));

        // Project 2: has no child records, so its own update_date is used (2025-06-20)
        UUID project2 = UUID.randomUUID();
        execute(String.format("INSERT INTO wfprev.project (project_guid, project_name, create_date, update_date) VALUES ('%s', 'P2', '2025-06-01', '2025-06-20')", project2));

        // Delete change log rows for project1 and project2 so we verify backfill creates them
        execute(String.format("DELETE FROM wfprev.prevention_layer_change_log WHERE project_guid IN ('%s', '%s')", project1, project2));

        // Run the backfill script
        execute(Files.readString(BACKFILL_SQL));

        Timestamp p1Timestamp = getLastUpdated(project1);
        assertNotNull(p1Timestamp);
        assertEquals(Timestamp.valueOf("2025-05-15 00:00:00"), p1Timestamp,
                "Project 1 last_updated must match the latest child update_date (activity_boundary)");

        Timestamp p2Timestamp = getLastUpdated(project2);
        assertNotNull(p2Timestamp);
        assertEquals(Timestamp.valueOf("2025-06-20 00:00:00"), p2Timestamp,
                "Project 2 last_updated must match the project's own update_date when it has no children");
    }

    @Test
    void backfill_doesNotOverwriteExistingChangeLogRows() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        execute(String.format("INSERT INTO wfprev.project (project_guid, project_name, create_date, update_date) VALUES ('%s', 'Existing Project', '2025-01-01', '2025-01-10')", projectGuid));

        // Manually set an existing custom timestamp in the change log
        Timestamp existingTimestamp = Timestamp.valueOf("2024-01-01 12:00:00");
        execute(String.format("INSERT INTO wfprev.prevention_layer_change_log (project_guid, last_updated) VALUES ('%s', '%s') ON CONFLICT (project_guid) DO UPDATE SET last_updated = EXCLUDED.last_updated",
                projectGuid, existingTimestamp));

        // Re-run backfill script
        execute(Files.readString(BACKFILL_SQL));

        Timestamp currentTimestamp = getLastUpdated(projectGuid);
        assertEquals(existingTimestamp, currentTimestamp,
                "Existing change log row must NOT be overwritten by backfill (DO NOTHING on conflict)");
    }

    private void execute(String sql) throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        }
    }

    private Timestamp getLastUpdated(UUID projectGuid) throws Exception {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT last_updated FROM wfprev.prevention_layer_change_log WHERE project_guid = '" + projectGuid + "'")) {
            if (rs.next()) {
                return rs.getTimestamp("last_updated");
            }
            return null;
        }
    }

    private Timestamp getDbCurrentTimestamp() throws Exception {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')")) {
            rs.next();
            return rs.getTimestamp(1);
        }
    }
}
