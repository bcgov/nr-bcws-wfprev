package ca.bc.gov.nrs.wfprev.services.spatial;

import ca.bc.gov.nrs.wfprev.services.spatial.ShapefileWriter.Feature;
import ca.bc.gov.nrs.wfprev.services.spatial.ShapefileWriter.Field;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShapefileWriterTest {

    private static final List<Field> FIELDS = List.of(
            Field.text("NAME", 10), Field.number("AREA_HA", 10, 2));

    // First polygon: anticlockwise shell (wrong for a Shapefile) with a clockwise hole (also wrong).
    // Second polygon: clockwise shell, already right.
    private static final String TWO_PARTS_WITH_HOLE = "MULTIPOLYGON("
            + "((0 0, 10 0, 10 10, 0 10, 0 0), (2 2, 2 4, 4 4, 4 2, 2 2)),"
            + "((20 20, 20 30, 30 30, 30 20, 20 20)))";

    private static MultiPolygon multiPolygon(String wkt) throws Exception {
        return (MultiPolygon) new WKTReader().read(wkt);
    }

    private static ByteBuffer le(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    }

    private static ByteBuffer be(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
    }

    @Test
    void shp_headerAndRecordFollowTheSpec() throws Exception {
        ShapefileWriter.Parts parts = ShapefileWriter.write(FIELDS,
                List.of(new Feature(multiPolygon(TWO_PARTS_WITH_HOLE), List.of("a", 1))), "PRJ");
        byte[] shp = parts.shp();

        assertEquals(9994, be(shp).getInt(0));
        assertEquals(shp.length / 2, be(shp).getInt(24));
        assertEquals(1000, le(shp).getInt(28));
        assertEquals(5, le(shp).getInt(32));
        assertEquals(0.0, le(shp).getDouble(36));
        assertEquals(0.0, le(shp).getDouble(44));
        assertEquals(30.0, le(shp).getDouble(52));
        assertEquals(30.0, le(shp).getDouble(60));

        assertEquals(1, be(shp).getInt(100));
        int contentWords = be(shp).getInt(104);
        assertEquals(shp.length - 108, contentWords * 2);

        ByteBuffer record = le(shp);
        record.position(108);
        assertEquals(5, record.getInt());
        record.position(record.position() + 32);
        int numParts = record.getInt();
        int numPoints = record.getInt();
        assertEquals(3, numParts);
        assertEquals(15, numPoints);
        int[] starts = new int[numParts];
        for (int i = 0; i < numParts; i++) {
            starts[i] = record.getInt();
        }
        assertArrayEquals(new int[]{0, 5, 10}, starts);

        List<Coordinate[]> rings = new ArrayList<>();
        for (int part = 0; part < numParts; part++) {
            int end = part + 1 < numParts ? starts[part + 1] : numPoints;
            Coordinate[] ring = new Coordinate[end - starts[part]];
            for (int i = 0; i < ring.length; i++) {
                ring[i] = new Coordinate(record.getDouble(), record.getDouble());
            }
            rings.add(ring);
        }
        assertFalse(Orientation.isCCW(rings.get(0)), "outer ring must be clockwise");
        assertTrue(Orientation.isCCW(rings.get(1)), "hole must be anticlockwise");
        assertFalse(Orientation.isCCW(rings.get(2)), "outer ring must be clockwise");
        assertEquals(rings.get(0)[0], rings.get(0)[rings.get(0).length - 1], "rings stay closed");
    }

    @Test
    void shx_indexesEachRecord() throws Exception {
        MultiPolygon square = multiPolygon("MULTIPOLYGON(((0 0, 0 1, 1 1, 1 0, 0 0)))");
        ShapefileWriter.Parts parts = ShapefileWriter.write(FIELDS,
                List.of(new Feature(square, List.of("a", 1)), new Feature(square, List.of("b", 2))), "PRJ");

        byte[] shx = parts.shx();
        assertEquals(9994, be(shx).getInt(0));
        assertEquals(100 + 2 * 8, shx.length);
        assertEquals(shx.length / 2, be(shx).getInt(24));

        int firstOffset = be(shx).getInt(100);
        int firstLength = be(shx).getInt(104);
        int secondOffset = be(shx).getInt(108);
        assertEquals(50, firstOffset);
        assertEquals(firstLength, be(parts.shp()).getInt(104));
        assertEquals(firstOffset + 4 + firstLength, secondOffset);
        assertEquals(2, be(parts.shp()).getInt(secondOffset * 2));
    }

    @Test
    void dbf_holdsTheAttributes() throws Exception {
        MultiPolygon square = multiPolygon("MULTIPOLYGON(((0 0, 0 1, 1 1, 1 0, 0 0)))");
        ShapefileWriter.Parts parts = ShapefileWriter.write(FIELDS,
                List.of(new Feature(square, Arrays.asList("Café", 3.14159)),
                        new Feature(square, Arrays.asList(null, null))), "PRJ");
        byte[] dbf = parts.dbf();

        assertEquals(0x03, dbf[0]);
        assertEquals(2, le(dbf).getInt(4));
        int headerLength = le(dbf).getShort(8);
        int recordLength = le(dbf).getShort(10);
        assertEquals(32 + 2 * 32 + 1, headerLength);
        assertEquals(1 + 10 + 10, recordLength);
        assertEquals(dbf.length, headerLength + 2 * recordLength + 1);
        assertEquals(0x0D, dbf[headerLength - 1]);
        assertEquals(0x1A, dbf[dbf.length - 1]);

        assertEquals("NAME", new String(dbf, 32, 4, StandardCharsets.US_ASCII));
        assertEquals(0, dbf[32 + 4]);
        assertEquals('C', dbf[32 + 11]);
        assertEquals(10, dbf[32 + 16]);
        assertEquals("AREA_HA", new String(dbf, 64, 7, StandardCharsets.US_ASCII));
        assertEquals('N', dbf[64 + 11]);
        assertEquals(2, dbf[64 + 17]);

        String first = new String(dbf, headerLength, recordLength, StandardCharsets.UTF_8);
        assertEquals(" Café     " + "      3.14", first);
        String second = new String(dbf, headerLength + recordLength, recordLength, StandardCharsets.US_ASCII);
        assertEquals(" ".repeat(recordLength), second);
    }

    @Test
    void dbf_cutsLongUtf8TextOnACharacterBoundary() throws Exception {
        MultiPolygon square = multiPolygon("MULTIPOLYGON(((0 0, 0 1, 1 1, 1 0, 0 0)))");
        // 9 ASCII bytes then a 2-byte character: the character doesn't fit in 10 bytes and is dropped whole.
        ShapefileWriter.Parts parts = ShapefileWriter.write(FIELDS,
                List.of(new Feature(square, List.of("123456789é", 1))), "PRJ");
        byte[] dbf = parts.dbf();
        int headerLength = le(dbf).getShort(8);

        assertEquals("123456789 ", new String(dbf, headerLength + 1, 10, StandardCharsets.UTF_8));
    }

    @Test
    void nullGeometry_isWrittenAsANullShape() {
        ShapefileWriter.Parts parts = ShapefileWriter.write(FIELDS, List.of(new Feature(null, List.of("a", 1))), "PRJ");
        byte[] shp = parts.shp();

        assertEquals(112, shp.length);
        assertEquals(2, be(shp).getInt(104));
        assertEquals(0, le(shp).getInt(108));
    }

    @Test
    void emptyPolygonsInAMultiPolygon_areSkipped() {
        GeometryFactory factory = new GeometryFactory();
        Polygon square = factory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(0, 1), new Coordinate(1, 1), new Coordinate(1, 0), new Coordinate(0, 0)});
        MultiPolygon geometry = factory.createMultiPolygon(new Polygon[]{square, factory.createPolygon()});

        byte[] shp = ShapefileWriter.write(FIELDS, List.of(new Feature(geometry, List.of("a", 1))), "PRJ").shp();

        assertEquals(1, le(shp).getInt(108 + 36));
    }

    @Test
    void prjAndCpg() throws Exception {
        ShapefileWriter.Parts parts = ShapefileWriter.write(FIELDS,
                List.of(new Feature(multiPolygon(TWO_PARTS_WITH_HOLE), List.of("a", 1))), ShapefileWriter.BC_ALBERS_PRJ);

        assertTrue(new String(parts.prj(), StandardCharsets.US_ASCII).startsWith("PROJCS[\"NAD_1983_BC_Environment_Albers\""));
        assertEquals("UTF-8", new String(parts.cpg(), StandardCharsets.US_ASCII));
        assertEquals(List.of(".shp", ".shx", ".dbf", ".prj", ".cpg"),
                parts.asList().stream().map(ShapefileWriter.Part::extension).toList());
    }

    @Test
    void fields_rejectInvalidDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> Field.text("ELEVEN_CHAR", 10));
        assertThrows(IllegalArgumentException.class, () -> Field.text("", 10));
        assertThrows(IllegalArgumentException.class, () -> Field.text("NAME", 255));
        assertThrows(IllegalArgumentException.class, () -> new Field("NAME", 'D', 8, 0));
    }
}
