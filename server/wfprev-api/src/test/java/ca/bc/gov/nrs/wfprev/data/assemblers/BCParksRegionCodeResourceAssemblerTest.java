package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.BCParksOrgUnitEntity;
import ca.bc.gov.nrs.wfprev.data.models.BCParksRegionCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class BCParksRegionCodeResourceAssemblerTest {

    public static final Integer ORG_UNIT_IDENTIFIER = 1;
    public static final Integer PARENT_ORG_UNIT_IDENTIFIER = 2;
    public static final Date EFFECTIVE_DATE = new Date();
    public static final Date EXPIRY_DATE = new Date();
    public static final String BC_PARKS_ORG_UNIT_TYPE_CODE = "bcParksOrgUnitTypeCode";
    public static final String ORG_UNIT_NAME = "orgUnitName";
    public static final int INTEGER_ALIAS = 1;
    public static final String CHARACTER_ALIAS = "characterAlias";
    private BCParksRegionCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new BCParksRegionCodeResourceAssembler();
    }

    @Test
    void toModel_ShouldConvertAllFields() {
        // Given
        BCParksOrgUnitEntity entity = new BCParksOrgUnitEntity();
        entity.setOrgUnitIdentifier(ORG_UNIT_IDENTIFIER);
        entity.setEffectiveDate(EFFECTIVE_DATE);
        entity.setExpiryDate(EXPIRY_DATE);
        entity.setBcParksOrgUnitTypeCode(BC_PARKS_ORG_UNIT_TYPE_CODE);
        entity.setParentOrgUnitIdentifier(PARENT_ORG_UNIT_IDENTIFIER);
        entity.setOrgUnitName(ORG_UNIT_NAME);
        entity.setIntegerAlias(INTEGER_ALIAS);
        entity.setCharacterAlias(CHARACTER_ALIAS);

        // When
        BCParksRegionCodeModel model = assembler.toModel(entity);

        // Then
        assertEquals(ORG_UNIT_IDENTIFIER, model.getOrgUnitId());
        assertEquals(EFFECTIVE_DATE, model.getEffectiveDate());
        assertEquals(EFFECTIVE_DATE, model.getExpiryDate());
        assertEquals(BC_PARKS_ORG_UNIT_TYPE_CODE, model.getBcParksOrgUnitTypeCode());
        assertEquals(PARENT_ORG_UNIT_IDENTIFIER.toString(), model.getParentOrgUnitId());
        assertEquals(ORG_UNIT_NAME, model.getOrgUnitName());
        assertEquals(ORG_UNIT_IDENTIFIER, model.getIntegerAlias());
        assertEquals(CHARACTER_ALIAS, model.getCharacterAlias());

    }
}
