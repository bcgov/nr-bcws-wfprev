package ca.bc.gov.nrs.wfprev.services.spatial;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Writes polygon Shapefiles (.shp, .shx, .dbf, .prj, .cpg) following the ESRI Shapefile Technical
 * Description (1998) and dBASE III.
 * <p>
 * Written by hand rather than with GeoTools, which relies on SPI and reflection that the API's native
 * image would need registering, and adds about 20 MB of dependencies. The format only needs byte buffers.
 */
public final class ShapefileWriter {

    /** ESRI WKT for EPSG:3005, NAD83 / BC Albers. */
    public static final String BC_ALBERS_PRJ = "PROJCS[\"NAD_1983_BC_Environment_Albers\","
            + "GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\","
            + "SPHEROID[\"GRS_1980\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],"
            + "UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Albers\"],"
            + "PARAMETER[\"False_Easting\",1000000.0],PARAMETER[\"False_Northing\",0.0],"
            + "PARAMETER[\"Central_Meridian\",-126.0],PARAMETER[\"Standard_Parallel_1\",50.0],"
            + "PARAMETER[\"Standard_Parallel_2\",58.5],PARAMETER[\"Latitude_Of_Origin\",45.0],"
            + "UNIT[\"Meter\",1.0]]";

    private static final int FILE_CODE = 9994;
    private static final int VERSION = 1000;
    private static final int SHAPE_NULL = 0;
    private static final int SHAPE_POLYGON = 5;
    private static final int HEADER_BYTES = 100;

    private ShapefileWriter() {
    }

    /** A dBASE field: {@code C} (text) or {@code N} (number). Names are at most 10 ASCII characters. */
    public record Field(String name, char type, int length, int decimals) {
        public Field {
            if (name.isEmpty() || name.length() > 10 || !StandardCharsets.US_ASCII.newEncoder().canEncode(name)) {
                throw new IllegalArgumentException("dBASE field names are 1-10 ASCII characters: " + name);
            }
            if (type != 'C' && type != 'N') {
                throw new IllegalArgumentException("Unsupported dBASE field type: " + type);
            }
            if (length < 1 || length > 254) {
                throw new IllegalArgumentException("dBASE field length must be 1-254: " + length);
            }
        }

        public static Field text(String name, int length) {
            return new Field(name, 'C', length, 0);
        }

        public static Field number(String name, int length, int decimals) {
            return new Field(name, 'N', length, decimals);
        }
    }

    /** One feature. A null or empty geometry is written as a null shape. Values line up with the fields. */
    public record Feature(MultiPolygon geometry, List<?> values) {
    }

    /** The files of one Shapefile, keyed by the extension they must be saved with. */
    public record Parts(byte[] shp, byte[] shx, byte[] dbf, byte[] prj, byte[] cpg) {

        public List<Part> asList() {
            return List.of(new Part(".shp", shp), new Part(".shx", shx), new Part(".dbf", dbf),
                    new Part(".prj", prj), new Part(".cpg", cpg));
        }
    }

    public record Part(String extension, byte[] content) {
    }

    public static Parts write(List<Field> fields, List<Feature> features, String prjWkt) {
        List<byte[]> records = new ArrayList<>(features.size());
        Envelope bounds = new Envelope();
        for (Feature feature : features) {
            records.add(shapeContent(feature.geometry()));
            if (feature.geometry() != null && !feature.geometry().isEmpty()) {
                bounds.expandToInclude(feature.geometry().getEnvelopeInternal());
            }
        }

        int shpBytes = HEADER_BYTES;
        for (byte[] content : records) {
            shpBytes += 8 + content.length;
        }
        int shxBytes = HEADER_BYTES + 8 * records.size();

        ByteBuffer shp = ByteBuffer.allocate(shpBytes);
        ByteBuffer shx = ByteBuffer.allocate(shxBytes);
        writeMainHeader(shp, shpBytes, bounds);
        writeMainHeader(shx, shxBytes, bounds);

        int recordNumber = 1;
        for (byte[] content : records) {
            shx.order(ByteOrder.BIG_ENDIAN).putInt(shp.position() / 2).putInt(content.length / 2);
            shp.order(ByteOrder.BIG_ENDIAN).putInt(recordNumber++).putInt(content.length / 2);
            shp.put(content);
        }

        return new Parts(shp.array(), shx.array(), dbf(fields, features),
                prjWkt.getBytes(StandardCharsets.US_ASCII), "UTF-8".getBytes(StandardCharsets.US_ASCII));
    }

    private static void writeMainHeader(ByteBuffer buffer, int fileBytes, Envelope bounds) {
        buffer.order(ByteOrder.BIG_ENDIAN).putInt(FILE_CODE);
        for (int i = 0; i < 5; i++) {
            buffer.putInt(0);
        }
        buffer.putInt(fileBytes / 2);
        buffer.order(ByteOrder.LITTLE_ENDIAN).putInt(VERSION).putInt(SHAPE_POLYGON);
        if (bounds.isNull()) {
            buffer.putDouble(0).putDouble(0).putDouble(0).putDouble(0);
        } else {
            buffer.putDouble(bounds.getMinX()).putDouble(bounds.getMinY())
                    .putDouble(bounds.getMaxX()).putDouble(bounds.getMaxY());
        }
        // Z and M ranges are unused for 2D polygons.
        buffer.putDouble(0).putDouble(0).putDouble(0).putDouble(0);
    }

    /** Record content of one shape: a Polygon made of every ring of every part of the MultiPolygon. */
    private static byte[] shapeContent(MultiPolygon geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SHAPE_NULL).array();
        }

        List<Coordinate[]> rings = new ArrayList<>();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) geometry.getGeometryN(i);
            if (polygon.isEmpty()) {
                continue;
            }
            // Shapefiles need outer rings clockwise and holes anticlockwise.
            rings.add(oriented(polygon.getExteriorRing(), true));
            for (int h = 0; h < polygon.getNumInteriorRing(); h++) {
                rings.add(oriented(polygon.getInteriorRingN(h), false));
            }
        }

        int points = rings.stream().mapToInt(r -> r.length).sum();
        ByteBuffer content = ByteBuffer.allocate(44 + 4 * rings.size() + 16 * points).order(ByteOrder.LITTLE_ENDIAN);
        Envelope envelope = geometry.getEnvelopeInternal();
        content.putInt(SHAPE_POLYGON)
                .putDouble(envelope.getMinX()).putDouble(envelope.getMinY())
                .putDouble(envelope.getMaxX()).putDouble(envelope.getMaxY())
                .putInt(rings.size())
                .putInt(points);
        int start = 0;
        for (Coordinate[] ring : rings) {
            content.putInt(start);
            start += ring.length;
        }
        for (Coordinate[] ring : rings) {
            for (Coordinate c : ring) {
                content.putDouble(c.x).putDouble(c.y);
            }
        }
        return content.array();
    }

    private static Coordinate[] oriented(LinearRing ring, boolean clockwise) {
        Coordinate[] coordinates = ring.getCoordinates();
        // A ring needs at least 4 points to have an orientation; write anything smaller as stored.
        if (coordinates.length >= 4 && Orientation.isCCW(coordinates) == clockwise) {
            coordinates = coordinates.clone();
            for (int i = 0, j = coordinates.length - 1; i < j; i++, j--) {
                Coordinate tmp = coordinates[i];
                coordinates[i] = coordinates[j];
                coordinates[j] = tmp;
            }
        }
        return coordinates;
    }

    private static byte[] dbf(List<Field> fields, List<Feature> features) {
        int headerLength = 32 + 32 * fields.size() + 1;
        int recordLength = 1 + fields.stream().mapToInt(Field::length).sum();
        ByteBuffer dbf = ByteBuffer.allocate(headerLength + recordLength * features.size() + 1)
                .order(ByteOrder.LITTLE_ENDIAN);

        LocalDate today = LocalDate.now();
        dbf.put((byte) 0x03)
                .put((byte) (today.getYear() - 1900)).put((byte) today.getMonthValue()).put((byte) today.getDayOfMonth())
                .putInt(features.size())
                .putShort((short) headerLength)
                .putShort((short) recordLength)
                .put(new byte[20]);

        for (Field field : fields) {
            dbf.put(Arrays.copyOf(field.name().getBytes(StandardCharsets.US_ASCII), 11))
                    .put((byte) field.type())
                    .put(new byte[4])
                    .put((byte) field.length())
                    .put((byte) field.decimals())
                    .put(new byte[14]);
        }
        dbf.put((byte) 0x0D);

        for (Feature feature : features) {
            dbf.put((byte) ' ');
            for (int i = 0; i < fields.size(); i++) {
                Object value = i < feature.values().size() ? feature.values().get(i) : null;
                dbf.put(fieldBytes(fields.get(i), value));
            }
        }
        dbf.put((byte) 0x1A);
        return dbf.array();
    }

    private static byte[] fieldBytes(Field field, Object value) {
        byte[] cell = new byte[field.length()];
        Arrays.fill(cell, (byte) ' ');
        if (value == null) {
            return cell;
        }
        if (field.type() == 'N') {
            String number = new BigDecimal(value.toString())
                    .setScale(field.decimals(), RoundingMode.HALF_UP).toPlainString();
            byte[] digits = number.getBytes(StandardCharsets.US_ASCII);
            if (digits.length > cell.length) {
                // Too wide for the field; dBASE readers treat an unparseable number as null.
                return cell;
            }
            System.arraycopy(digits, 0, cell, cell.length - digits.length, digits.length);
        } else {
            byte[] text = truncateUtf8(value.toString(), cell.length);
            System.arraycopy(text, 0, cell, 0, text.length);
        }
        return cell;
    }

    /** UTF-8 bytes of the text, cut to at most maxBytes without splitting a character. */
    private static byte[] truncateUtf8(String text, int maxBytes) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return bytes;
        }
        int end = maxBytes;
        while (end > 0 && (bytes[end] & 0xC0) == 0x80) {
            end--;
        }
        return Arrays.copyOf(bytes, end);
    }
}
