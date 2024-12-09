package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectStatusCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class ProjectStatusCodeResourceAssemblerTest {

    private ProjectStatusCodeResourceAssembler assembler;
    private static final String PROJECT_STATUS_CODE = "ACTIVE";
    private static final String DESCRIPTION = "Active status";
    private static final Integer DISPLAY_ORDER = 1;
    private static final Date EFFECTIVE_DATE = new Date();
    private static final Date EXPIRY_DATE = new Date();
    private static final String CREATE_USER = "SYSTEM";
    private static final Date CREATE_DATE = new Date();
    private static final String UPDATE_USER = "SYSTEM";
    private static final Date UPDATE_DATE = new Date();
    private static final Integer REVISION_COUNT = 0;

    @BeforeEach
    void setUp() {
        assembler = new ProjectStatusCodeResourceAssembler();
    }

    @Test
    void toModel_ShouldConvertAllFields() {
        // Given
        ProjectStatusCodeEntity entity = new ProjectStatusCodeEntity();
        entity.setProjectStatusCode(PROJECT_STATUS_CODE);
        entity.setDescription(DESCRIPTION);
        entity.setDisplayOrder(DISPLAY_ORDER);
        entity.setEffectiveDate(EFFECTIVE_DATE);
        entity.setExpiryDate(EXPIRY_DATE);
        entity.setCreateDate(CREATE_DATE);
        entity.setCreateUser(CREATE_USER);
        entity.setUpdateDate(UPDATE_DATE);
        entity.setUpdateUser(UPDATE_USER);
        entity.setRevisionCount(REVISION_COUNT);

        // When
        ProjectStatusCodeModel model = assembler.toModel(entity);

        // Then
        assertNotNull(model);
        assertEquals(PROJECT_STATUS_CODE, model.getProjectStatusCode());
        assertEquals(DESCRIPTION, model.getDescription());
        assertEquals(DISPLAY_ORDER, model.getDisplayOrder());
        assertEquals(EFFECTIVE_DATE, model.getEffectiveDate());
        assertEquals(EXPIRY_DATE, model.getExpiryDate());
        assertEquals(CREATE_DATE, model.getCreateDate());
        assertEquals(CREATE_USER, model.getCreateUser());
        assertEquals(UPDATE_DATE, model.getUpdateDate());
        assertEquals(UPDATE_USER, model.getUpdateUser());
        assertEquals(REVISION_COUNT, model.getRevisionCount());
    }

    @Test
    void toEntity_ShouldConvertAllFields() {
        // Given
        ProjectStatusCodeModel model = new ProjectStatusCodeModel();
        model.setProjectStatusCode(PROJECT_STATUS_CODE);
        model.setDescription(DESCRIPTION);
        model.setDisplayOrder(DISPLAY_ORDER);
        model.setEffectiveDate(EFFECTIVE_DATE);
        model.setExpiryDate(EXPIRY_DATE);
        model.setCreateDate(CREATE_DATE);
        model.setCreateUser(CREATE_USER);
        model.setUpdateDate(UPDATE_DATE);
        model.setUpdateUser(UPDATE_USER);
        model.setRevisionCount(REVISION_COUNT);

        // When
        ProjectStatusCodeEntity entity = assembler.toEntity(model);

        // Then
        assertNotNull(entity);
        assertEquals(PROJECT_STATUS_CODE, entity.getProjectStatusCode());
        assertEquals(DESCRIPTION, entity.getDescription());
        assertEquals(DISPLAY_ORDER, entity.getDisplayOrder());
        assertEquals(EFFECTIVE_DATE, entity.getEffectiveDate());
        assertEquals(EXPIRY_DATE, entity.getExpiryDate());
        assertEquals(CREATE_DATE, entity.getCreateDate());
        assertEquals(CREATE_USER, entity.getCreateUser());
        assertEquals(UPDATE_DATE, entity.getUpdateDate());
        assertEquals(UPDATE_USER, entity.getUpdateUser());
        assertEquals(REVISION_COUNT, entity.getRevisionCount());
    }

    @Test
    void toEntity_ShouldReturnNull_WhenModelIsNull() {
        // When
        ProjectStatusCodeEntity entity = assembler.toEntity(null);

        // Then
        assertNull(entity);
    }

    @Test
    void toEntity_ShouldHandleNullFields() {
        // Given
        ProjectStatusCodeModel model = new ProjectStatusCodeModel();

        // When
        ProjectStatusCodeEntity entity = assembler.toEntity(model);

        // Then
        assertNotNull(entity);
        assertNull(entity.getProjectStatusCode());
        assertNull(entity.getDescription());
        assertNull(entity.getDisplayOrder());
        assertNull(entity.getEffectiveDate());
        assertNull(entity.getExpiryDate());
        assertNull(entity.getCreateDate());
        assertNull(entity.getCreateUser());
        assertNull(entity.getUpdateDate());
        assertNull(entity.getUpdateUser());
        assertNull(entity.getRevisionCount());
    }

    @Test
    void toModel_ShouldHandleNullFields() {
        // Given
        ProjectStatusCodeEntity entity = new ProjectStatusCodeEntity();

        // When
        ProjectStatusCodeModel model = assembler.toModel(entity);

        // Then
        assertNotNull(model);
        assertNull(model.getProjectStatusCode());
        assertNull(model.getDescription());
        assertNull(model.getDisplayOrder());
        assertNull(model.getEffectiveDate());
        assertNull(model.getExpiryDate());
        assertNull(model.getCreateDate());
        assertNull(model.getCreateUser());
        assertNull(model.getUpdateDate());
        assertNull(model.getUpdateUser());
        assertNull(model.getRevisionCount());
    }
}