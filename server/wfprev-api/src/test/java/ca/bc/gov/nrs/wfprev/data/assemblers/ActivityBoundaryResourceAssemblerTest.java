package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpolygon;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class ActivityBoundaryResourceAssemblerTest {

    ActivityBoundaryResourceAssembler assembler = new ActivityBoundaryResourceAssembler();

    @Test
    void testToModel_MapsEntityToModel() throws SQLException {
        // Arrange
        ActivityBoundaryEntity entity = new ActivityBoundaryEntity();
        entity.setActivityBoundaryGuid(UUID.randomUUID());
        entity.setActivityGuid(UUID.randomUUID());
        entity.setSystemStartTimestamp(convertToDate(LocalDate.of(2025, 4, 1)));
        entity.setSystemEndTimestamp(convertToDate(LocalDate.of(2025, 9, 30)));
        entity.setMappingLabel("Test mapping label");
        entity.setCollectionDate(convertToDate(LocalDate.of(2025, 4, 1)));
        entity.setCollectionMethod("Test collection method");
        entity.setCollectorName("test_user");
        entity.setBoundarySizeHa(BigDecimal.valueOf(100.0000));
        entity.setBoundaryComment("Initial test activity boundary creation");
        entity.setGeometry(new PGpolygon("((-123.3656,48.4284),(-123.3657,48.4285),(-123.3658,48.4284),(-123.3656,48.4284))"));

        // Act
        ActivityBoundaryModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getActivityBoundaryGuid().toString(), model.getActivityBoundaryGuid());
        assertEquals(entity.getActivityGuid().toString(), model.getActivityGuid());
        assertEquals(entity.getSystemStartTimestamp(), model.getSystemStartTimestamp());
        assertEquals(entity.getSystemEndTimestamp(), model.getSystemEndTimestamp());
        assertEquals(entity.getMappingLabel(), model.getMappingLabel());
        assertEquals(entity.getCollectionDate(), model.getCollectionDate());
        assertEquals(entity.getCollectionMethod(), model.getCollectionMethod());
        assertEquals(entity.getCollectorName(), model.getCollectorName());
        assertEquals(entity.getBoundarySizeHa(), model.getBoundarySizeHa());
        assertEquals(entity.getBoundaryComment(), model.getBoundaryComment());
        assertEquals(entity.getGeometry(), model.getGeometry());
    }

    @Test
    void testToEntity_MapsModelToEntity() throws SQLException {
        // Arrange
        ActivityBoundaryModel model = new ActivityBoundaryModel();
        model.setActivityBoundaryGuid(UUID.randomUUID().toString());
        model.setActivityGuid(UUID.randomUUID().toString());
        model.setSystemStartTimestamp(convertToDate(LocalDate.of(2025, 4, 1)));
        model.setSystemEndTimestamp(convertToDate(LocalDate.of(2025, 9, 30)));
        model.setMappingLabel("Test mapping label");
        model.setCollectionDate(convertToDate(LocalDate.of(2025, 4, 1)));
        model.setCollectionMethod("Test collection method");
        model.setCollectorName("test_user");
        model.setBoundarySizeHa(BigDecimal.valueOf(100.0000));
        model.setBoundaryComment("Initial test activity boundary creation");
        model.setGeometry(new PGpolygon("((-123.3656,48.4284),(-123.3657,48.4285),(-123.3658,48.4284),(-123.3656,48.4284))"));

        // Act
        ActivityBoundaryEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getActivityBoundaryGuid(), entity.getActivityBoundaryGuid().toString());
        assertEquals(model.getActivityGuid(), entity.getActivityGuid().toString());
        assertEquals(model.getSystemStartTimestamp(), entity.getSystemStartTimestamp());
        assertEquals(model.getSystemEndTimestamp(), entity.getSystemEndTimestamp());
        assertEquals(model.getMappingLabel(), entity.getMappingLabel());
        assertEquals(model.getCollectionDate(), entity.getCollectionDate());
        assertEquals(model.getCollectionMethod(), entity.getCollectionMethod());
        assertEquals(model.getCollectorName(), entity.getCollectorName());
        assertEquals(model.getBoundarySizeHa(), entity.getBoundarySizeHa());
        assertEquals(model.getBoundaryComment(), entity.getBoundaryComment());
        assertEquals(model.getGeometry(), entity.getGeometry());
    }

    @Test
    void testUpdateEntity() {
        // Arrange
        ActivityBoundaryModel model = new ActivityBoundaryModel();
        model.setMappingLabel("Updated Label");
        model.setCollectionMethod("Updated Method");
        model.setBoundarySizeHa(BigDecimal.valueOf(200.0000));

        ActivityBoundaryEntity existingEntity = new ActivityBoundaryEntity();
        existingEntity.setActivityBoundaryGuid(UUID.randomUUID());
        existingEntity.setActivityGuid(UUID.randomUUID());
        existingEntity.setMappingLabel("Old Label");
        existingEntity.setCollectionMethod("Old Method");
        existingEntity.setBoundarySizeHa(BigDecimal.valueOf(100.0000));

        // Act
        ActivityBoundaryEntity updatedEntity = assembler.updateEntity(model, existingEntity);

        // Assert
        assertEquals("Updated Label", updatedEntity.getMappingLabel());
        assertEquals("Updated Method", updatedEntity.getCollectionMethod());
        assertEquals(BigDecimal.valueOf(200.0000), updatedEntity.getBoundarySizeHa());
    }

    private Date convertToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
