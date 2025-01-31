package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class ActivityResourceAssemblerTest {

    ActivityResourceAssembler assembler = new ActivityResourceAssembler();

    @Test
    void testToModel_MapsEntityToModel() {
        // Arrange
        ActivityEntity entity = new ActivityEntity();
        entity.setActivityGuid(UUID.randomUUID());
        entity.setActivityName("Test Activity");
        entity.setActivityDescription("Description");
        entity.setActivityStartDate(new Date());
        entity.setActivityEndDate(new Date());
        entity.setPlannedSpendAmount(BigDecimal.valueOf(1000));
        entity.setCompletedAreaHa(BigDecimal.valueOf(50));

        // Act
        ActivityModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getActivityGuid().toString(), model.getActivityGuid());
        assertEquals(entity.getActivityName(), model.getActivityName());
        assertEquals(entity.getActivityDescription(), model.getActivityDescription());
        assertEquals(entity.getPlannedSpendAmount(), model.getPlannedSpendAmount());
        assertEquals(entity.getCompletedAreaHa(), model.getCompletedAreaHa());
    }

    @Test
    void testToEntity_MapsModelToEntity() {
        // Arrange
        ActivityModel model = new ActivityModel();
        model.setActivityGuid(UUID.randomUUID().toString());
        model.setActivityName("Test Activity");
        model.setActivityDescription("Description");
        model.setActivityStartDate(new Date());
        model.setActivityEndDate(new Date());
        model.setPlannedSpendAmount(BigDecimal.valueOf(1000));
        model.setCompletedAreaHa(BigDecimal.valueOf(50));

        // Act
        ActivityEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getActivityGuid(), entity.getActivityGuid().toString());
        assertEquals(model.getActivityName(), entity.getActivityName());
        assertEquals(model.getActivityDescription(), entity.getActivityDescription());
        assertEquals(model.getPlannedSpendAmount(), entity.getPlannedSpendAmount());
        assertEquals(model.getCompletedAreaHa(), entity.getCompletedAreaHa());
    }

    @Test
    void testUpdateEntity() {
        // Arrange
        ActivityModel model = new ActivityModel();
        model.setActivityName("Updated Activity");
        model.setPlannedSpendAmount(BigDecimal.valueOf(2000));

        ActivityEntity existingEntity = new ActivityEntity();
        existingEntity.setActivityGuid(UUID.randomUUID());
        existingEntity.setActivityName("Old Activity");
        existingEntity.setPlannedSpendAmount(BigDecimal.valueOf(1000));

        // Act
        ActivityEntity updatedEntity = assembler.updateEntity(model, existingEntity);

        // Assert
        assertEquals("Updated Activity", updatedEntity.getActivityName());
        assertEquals(BigDecimal.valueOf(2000), updatedEntity.getPlannedSpendAmount());
    }
}
