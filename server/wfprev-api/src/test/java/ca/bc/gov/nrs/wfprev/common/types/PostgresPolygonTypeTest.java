package ca.bc.gov.nrs.wfprev.common.types;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.BasicBinder;
import org.hibernate.type.descriptor.jdbc.BasicExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpolygon;
import org.postgresql.util.PGobject;

import java.lang.reflect.Method;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostgresPolygonTypeTest {

    private PostgresPolygonType postgresPolygonType;
    private JavaType<PGpolygon> javaType;
    private WrapperOptions wrapperOptions;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private CallableStatement callableStatement;

    @BeforeEach
    void setUp() {
        postgresPolygonType = new PostgresPolygonType();
        javaType = (JavaType<PGpolygon>) mock(JavaType.class);
        wrapperOptions = mock(WrapperOptions.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        callableStatement = mock(CallableStatement.class);
    }

    @Test
    void testBinder_DoBind_PreparedStatement() throws Exception {
        PGpolygon polygon = new PGpolygon("((-123.3656,48.4284),(-123.3657,48.4285),(-123.3658,48.4284),(-123.3656,48.4284))");

        BasicBinder<PGpolygon> binder = postgresPolygonType.getBinder(javaType);

        Method doBindMethod = BasicBinder.class.getDeclaredMethod("doBind", PreparedStatement.class, Object.class, int.class, WrapperOptions.class);
        doBindMethod.setAccessible(true);

        doBindMethod.invoke(binder, preparedStatement, polygon, 1, wrapperOptions);

        verify(preparedStatement).setObject(eq(1), any(PGobject.class));
    }

    @Test
    void testBinder_DoBind_NullValue_PreparedStatement() throws Exception {
        BasicBinder<PGpolygon> binder = postgresPolygonType.getBinder(javaType);

        Method doBindMethod = BasicBinder.class.getDeclaredMethod("doBind", PreparedStatement.class, Object.class, int.class, WrapperOptions.class);
        doBindMethod.setAccessible(true);

        doBindMethod.invoke(binder, preparedStatement, null, 1, wrapperOptions);

        verify(preparedStatement).setNull(1, Types.OTHER);
    }

    @Test
    void testExtractor_DoExtract_ResultSet() throws Exception {
        BasicExtractor<PGpolygon> extractor = postgresPolygonType.getExtractor(javaType);
        PGobject pgObject = new PGobject();
        pgObject.setType("polygon");
        pgObject.setValue("((-123.3656,48.4284),(-123.3657,48.4285),(-123.3658,48.4284),(-123.3656,48.4284))");

        when(resultSet.getObject(1)).thenReturn(pgObject);
        when(javaType.wrap(any(), any())).thenAnswer(invocation -> new PGpolygon(pgObject.getValue()));

        Method doExtractMethod = BasicExtractor.class.getDeclaredMethod("doExtract", ResultSet.class, int.class, WrapperOptions.class);
        doExtractMethod.setAccessible(true);

        PGpolygon extractedPolygon = (PGpolygon) doExtractMethod.invoke(extractor, resultSet, 1, wrapperOptions);

        assertNotNull(extractedPolygon);
        assertEquals(pgObject.getValue(), extractedPolygon.getValue());
    }

    @Test
    void testExtractor_DoExtract_NullValue_ResultSet() throws Exception {
        BasicExtractor<PGpolygon> extractor = postgresPolygonType.getExtractor(javaType);

        when(resultSet.getObject(1)).thenReturn(null);

        Method doExtractMethod = BasicExtractor.class.getDeclaredMethod("doExtract", ResultSet.class, int.class, WrapperOptions.class);
        doExtractMethod.setAccessible(true);

        PGpolygon extractedPolygon = (PGpolygon) doExtractMethod.invoke(extractor, resultSet, 1, wrapperOptions);

        assertNull(extractedPolygon);
    }

    @Test
    void testExtractor_DoExtract_CallableStatement() throws Exception {
        BasicExtractor<PGpolygon> extractor = postgresPolygonType.getExtractor(javaType);
        PGobject pgObject = new PGobject();
        pgObject.setType("polygon");
        pgObject.setValue("((-123.3656,48.4284),(-123.3657,48.4285),(-123.3658,48.4284),(-123.3656,48.4284))");

        when(callableStatement.getObject(1)).thenReturn(pgObject);
        when(javaType.wrap(any(), any())).thenAnswer(invocation -> new PGpolygon(pgObject.getValue()));

        Method doExtractMethod = BasicExtractor.class.getDeclaredMethod("doExtract", CallableStatement.class, int.class, WrapperOptions.class);
        doExtractMethod.setAccessible(true);

        PGpolygon extractedPolygon = (PGpolygon) doExtractMethod.invoke(extractor, callableStatement, 1, wrapperOptions);

        assertNotNull(extractedPolygon);
        assertEquals(pgObject.getValue(), extractedPolygon.getValue());
    }

    @Test
    void testBinder_DoBind_CallableStatement_ByName() throws Exception {
        PGpolygon polygon = new PGpolygon("((-123.3656,48.4284),(-123.3657,48.4285),(-123.3658,48.4284),(-123.3656,48.4284))");

        BasicBinder<PGpolygon> binder = postgresPolygonType.getBinder(javaType);

        Method doBindMethod = BasicBinder.class.getDeclaredMethod("doBind", CallableStatement.class, Object.class, String.class, WrapperOptions.class);
        doBindMethod.setAccessible(true);

        doBindMethod.invoke(binder, callableStatement, polygon, "polygon_param", wrapperOptions);

        verify(callableStatement).setObject(eq("polygon_param"), any(PGobject.class));
    }

    @Test
    void testBinder_DoBind_NullValue_CallableStatement_ByName() throws Exception {
        BasicBinder<PGpolygon> binder = postgresPolygonType.getBinder(javaType);

        Method doBindMethod = BasicBinder.class.getDeclaredMethod("doBind", CallableStatement.class, Object.class, String.class, WrapperOptions.class);
        doBindMethod.setAccessible(true);

        doBindMethod.invoke(binder, callableStatement, null, "polygon_param", wrapperOptions);

        verify(callableStatement).setNull("polygon_param", Types.OTHER);
    }

    @Test
    void testExtractor_DoExtract_CallableStatement_ByName() throws Exception {
        BasicExtractor<PGpolygon> extractor = postgresPolygonType.getExtractor(javaType);
        PGobject pgObject = new PGobject();
        pgObject.setType("polygon");
        pgObject.setValue("((-123.3656,48.4284),(-123.3657,48.4285),(-123.3658,48.4284),(-123.3656,48.4284))");

        when(callableStatement.getObject("polygon_param")).thenReturn(pgObject);
        when(javaType.wrap(any(), any())).thenAnswer(invocation -> new PGpolygon(pgObject.getValue()));

        Method doExtractMethod = BasicExtractor.class.getDeclaredMethod("doExtract", CallableStatement.class, String.class, WrapperOptions.class);
        doExtractMethod.setAccessible(true);

        PGpolygon extractedPolygon = (PGpolygon) doExtractMethod.invoke(extractor, callableStatement, "polygon_param", wrapperOptions);

        assertNotNull(extractedPolygon);
        assertEquals(pgObject.getValue(), extractedPolygon.getValue());
    }

    @Test
    void testExtractor_DoExtract_NullValue_CallableStatement_ByName() throws Exception {
        BasicExtractor<PGpolygon> extractor = postgresPolygonType.getExtractor(javaType);

        when(callableStatement.getObject("polygon_param")).thenReturn(null);

        Method doExtractMethod = BasicExtractor.class.getDeclaredMethod("doExtract", CallableStatement.class, String.class, WrapperOptions.class);
        doExtractMethod.setAccessible(true);

        PGpolygon extractedPolygon = (PGpolygon) doExtractMethod.invoke(extractor, callableStatement, "polygon_param", wrapperOptions);

        assertNull(extractedPolygon);
    }

}
