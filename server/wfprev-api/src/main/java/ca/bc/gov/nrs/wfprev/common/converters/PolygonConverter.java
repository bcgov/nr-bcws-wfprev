package ca.bc.gov.nrs.wfprev.common.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.geometric.PGpolygon;

@Converter
public class PolygonConverter implements AttributeConverter<PGpolygon, String> {

    @Override
    public String convertToDatabaseColumn(PGpolygon polygon) {
        if (polygon == null) return null;
        return polygon.getValue();
    }

    @Override
    public PGpolygon convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return new PGpolygon(dbData);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting database value to polygon: " + dbData, e);
        }
    }

    /**
     * Utility method to create a PGpolygon from an array of points
     */
    public static PGpolygon createPolygon(double[][] points) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < points.length; i++) {
                if (i > 0) sb.append(",");
                sb.append("(").append(points[i][0]).append(",").append(points[i][1]).append(")");
            }
            sb.append(")");
            return new PGpolygon(sb.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error creating polygon from points", e);
        }
    }
}