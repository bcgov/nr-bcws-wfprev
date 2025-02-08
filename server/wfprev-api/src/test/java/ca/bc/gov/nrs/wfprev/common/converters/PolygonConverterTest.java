package ca.bc.gov.nrs.wfprev.common.converters;

import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpolygon;

import static org.junit.jupiter.api.Assertions.*;

class PolygonConverterTest {

    private final PolygonConverter converter = new PolygonConverter();

    @Test
    void testConvertToDatabaseColumn_WithValidPolygon() throws Exception {
        PGpolygon polygon = new PGpolygon("((-123.3656,48.4284),(-123.3657,48.4285),(-123.3658,48.4284),(-123.3656,48.4284))");
        String result = converter.convertToDatabaseColumn(polygon);
        assertEquals("((-123.3656,48.4284),(-123.3657,48.4285),(-123.3658,48.4284),(-123.3656,48.4284))", result);
    }

    @Test
    void testConvertToDatabaseColumn_WithNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void testConvertToEntityAttribute_WithValidString() {
        String dbData = "((-123.3656,48.4284),(-123.3657,48.4285),(-123.3658,48.4284),(-123.3656,48.4284))";
        PGpolygon result = converter.convertToEntityAttribute(dbData);
        assertNotNull(result);
        assertEquals(dbData, result.getValue());
    }

    @Test
    void testConvertToEntityAttribute_WithNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void testConvertToEntityAttribute_WithInvalidString() {
        String invalidDbData = "INVALID POLYGON STRING";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToEntityAttribute(invalidDbData);
        });
        assertTrue(exception.getMessage().contains("Error converting database value to polygon"));
    }

    @Test
    void testCreatePolygon_WithValidPoints() {
        double[][] points = {
                {-123.3656, 48.4284},
                {-123.3657, 48.4285},
                {-123.3658, 48.4284},
                {-123.3656, 48.4284}
        };

        PGpolygon polygon = PolygonConverter.createPolygon(points);
        assertNotNull(polygon);
        assertEquals("((-123.3656,48.4284),(-123.3657,48.4285),(-123.3658,48.4284),(-123.3656,48.4284))", polygon.getValue());
    }

    @Test
    void testCreatePolygon_WithInvalidPoints() {
        double[][] points = {}; // Empty array should fail
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            PolygonConverter.createPolygon(points);
        });
        assertTrue(exception.getMessage().contains("Error creating polygon from points"));
    }
}
