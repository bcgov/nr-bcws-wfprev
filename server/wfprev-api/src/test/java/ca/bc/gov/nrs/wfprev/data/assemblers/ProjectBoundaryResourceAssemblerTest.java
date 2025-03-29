package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.postgresql.geometric.PGpolygon;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProjectBoundaryResourceAssemblerTest {

    ProjectBoundaryResourceAssembler assembler = new ProjectBoundaryResourceAssembler();

    MultiPolygon multiPolygon;

    @BeforeEach
    void setUp(){
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates = {
                new Coordinate(-123.3656, 48.4284),
                new Coordinate(-123.3657, 48.4285),
                new Coordinate(-123.3658, 48.4284),
                new Coordinate(-123.3656, 48.4284) // Closing the polygon
        };

        LinearRing shell = geometryFactory.createLinearRing(coordinates);
        Polygon polygon = geometryFactory.createPolygon(shell);

        multiPolygon = geometryFactory.createMultiPolygon(new Polygon[]{polygon});
    }

    @Test
    void testToModel_MapsEntityToModel() throws SQLException {

        GeometryFactory geometryFactory = new GeometryFactory();
        Point locationPoint = geometryFactory.createPoint(new Coordinate(-123.3656, 48.4284));

        // Arrange
        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();
        entity.setProjectBoundaryGuid(UUID.randomUUID());
        entity.setProjectGuid(UUID.randomUUID());
        entity.setSystemStartTimestamp(convertToDate(LocalDate.of(2025, 4, 1)));
        entity.setSystemEndTimestamp(convertToDate(LocalDate.of(2025, 9, 30)));
        entity.setMappingLabel("Test mapping label");
        entity.setCollectionDate(convertToDate(LocalDate.of(2025, 4, 1)));
        entity.setCollectionMethod("Test collection method");
        entity.setCollectorName("test_user");
        entity.setBoundarySizeHa(BigDecimal.valueOf(100.0000));
        entity.setBoundaryComment("Initial test project boundary creation");
        entity.setLocationGeometry(locationPoint);
        entity.setBoundaryGeometry(multiPolygon);

        // Act
        ProjectBoundaryModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(entity.getProjectBoundaryGuid().toString(), model.getProjectBoundaryGuid());
        assertEquals(entity.getProjectGuid().toString(), model.getProjectGuid());
        assertEquals(entity.getSystemStartTimestamp(), model.getSystemStartTimestamp());
        assertEquals(entity.getSystemEndTimestamp(), model.getSystemEndTimestamp());
        assertEquals(entity.getMappingLabel(), model.getMappingLabel());
        assertEquals(entity.getCollectionDate(), model.getCollectionDate());
        assertEquals(entity.getCollectionMethod(), model.getCollectionMethod());
        assertEquals(entity.getCollectorName(), model.getCollectorName());
        assertEquals(entity.getBoundarySizeHa(), model.getBoundarySizeHa());
        assertEquals(entity.getBoundaryComment(), model.getBoundaryComment());
        assertEquals(entity.getLocationGeometry(), model.getLocationGeometry());
        assertEquals(entity.getBoundaryGeometry(), model.getBoundaryGeometry());
    }

    @Test
    void testToEntity_MapsModelToEntity() throws SQLException {

        GeometryFactory geometryFactory = new GeometryFactory();
        Point locationPoint = geometryFactory.createPoint(new Coordinate(-123.3656, 48.4284));

        // Arrange
        ProjectBoundaryModel model = new ProjectBoundaryModel();
        model.setProjectBoundaryGuid(UUID.randomUUID().toString());
        model.setProjectGuid(UUID.randomUUID().toString());
        model.setSystemStartTimestamp(convertToDate(LocalDate.of(2025, 4, 1)));
        model.setSystemEndTimestamp(convertToDate(LocalDate.of(2025, 9, 30)));
        model.setMappingLabel("Test mapping label");
        model.setCollectionDate(convertToDate(LocalDate.of(2025, 4, 1)));
        model.setCollectionMethod("Test collection method");
        model.setCollectorName("test_user");
        model.setBoundarySizeHa(BigDecimal.valueOf(100.0000));
        model.setBoundaryComment("Initial test project boundary creation");
        model.setLocationGeometry(locationPoint);
        model.setBoundaryGeometry(multiPolygon);

        // Act
        ProjectBoundaryEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(model.getProjectBoundaryGuid(), entity.getProjectBoundaryGuid().toString());
        assertEquals(model.getProjectGuid(), entity.getProjectGuid().toString());
        assertEquals(model.getSystemStartTimestamp(), entity.getSystemStartTimestamp());
        assertEquals(model.getSystemEndTimestamp(), entity.getSystemEndTimestamp());
        assertEquals(model.getMappingLabel(), entity.getMappingLabel());
        assertEquals(model.getCollectionDate(), entity.getCollectionDate());
        assertEquals(model.getCollectionMethod(), entity.getCollectionMethod());
        assertEquals(model.getCollectorName(), entity.getCollectorName());
        assertEquals(model.getBoundarySizeHa(), entity.getBoundarySizeHa());
        assertEquals(model.getBoundaryComment(), entity.getBoundaryComment());
        assertEquals(model.getLocationGeometry(), entity.getLocationGeometry());
        assertEquals(model.getBoundaryGeometry(), entity.getBoundaryGeometry());
    }

    @Test
    void testUpdateEntity() {
        // Arrange
        ProjectBoundaryModel model = new ProjectBoundaryModel();
        model.setMappingLabel("Updated Label");
        model.setCollectionMethod("Updated Method");
        model.setBoundarySizeHa(BigDecimal.valueOf(200.0000));

        ProjectBoundaryEntity existingEntity = new ProjectBoundaryEntity();
        existingEntity.setProjectBoundaryGuid(UUID.randomUUID());
        existingEntity.setProjectGuid(UUID.randomUUID());
        existingEntity.setMappingLabel("Old Label");
        existingEntity.setCollectionMethod("Old Method");
        existingEntity.setBoundarySizeHa(BigDecimal.valueOf(100.0000));

        // Act
        ProjectBoundaryEntity updatedEntity = assembler.updateEntity(model, existingEntity);

        // Assert
        assertEquals("Updated Label", updatedEntity.getMappingLabel());
        assertEquals("Updated Method", updatedEntity.getCollectionMethod());
        assertEquals(BigDecimal.valueOf(200.0000), updatedEntity.getBoundarySizeHa());
    }

    private Date convertToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
