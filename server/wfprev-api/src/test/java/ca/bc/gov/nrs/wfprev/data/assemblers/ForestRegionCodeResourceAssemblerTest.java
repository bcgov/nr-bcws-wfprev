package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ForestOrgUnitCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestRegionUnitCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ForestRegionCodeResourceAssemblerTest {

    public static final Integer ORG_UNIT_IDENTIFIER = 1;
    public static final Date EFFECTIVE_DATE = new Date();
    public static final Date EXPIRY_DATE = new Date();
    public static final String FOREST_ORG_UNIT_TYPE_CODE = "forestOrgUnitTypeCode";
    public static final String PARENT_ORG_UNIT_IDENTIFIER = "parentOrgUnitIdentifier";
    public static final String ORG_UNIT_NAME = "orgUnitName";
    public static final Integer INTEGER_ALIAS = 1;
    public static final String CHARACTER_ALIAS = "characterAlias";
    ForestRegionUnitCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ForestRegionUnitCodeResourceAssembler();
    }

    @Test
    void toModel_ShouldConvertAllFields() {
        // Given
        ForestOrgUnitCodeEntity entity = new ForestOrgUnitCodeEntity();
        entity.setOrgUnitIdentifier(ORG_UNIT_IDENTIFIER);
        entity.setEffectiveDate(EFFECTIVE_DATE);
        entity.setExpiryDate(EXPIRY_DATE);
        entity.setForestOrgUnitTypeCode(FOREST_ORG_UNIT_TYPE_CODE);
        entity.setParentOrgUnitIdentifier(PARENT_ORG_UNIT_IDENTIFIER);
        entity.setOrgUnitName(ORG_UNIT_NAME);
        entity.setIntegerAlias(INTEGER_ALIAS);
        entity.setCharacterAlias(CHARACTER_ALIAS);

        // When

        ForestRegionUnitCodeModel model = assembler.toModel(entity);
        // Then
        assertEquals(ORG_UNIT_IDENTIFIER, model.getOrgUnitId());
        assertEquals(EFFECTIVE_DATE, model.getEffectiveDate());
        assertEquals(EXPIRY_DATE, model.getExpiryDate());
        assertEquals(FOREST_ORG_UNIT_TYPE_CODE, model.getForestOrgUnitTypeCode());
        assertEquals(PARENT_ORG_UNIT_IDENTIFIER, model.getParentOrgUnitId());
        assertEquals(ORG_UNIT_NAME, model.getOrgUnitName());
        assertEquals(INTEGER_ALIAS, model.getIntegerAlias());
        assertEquals(CHARACTER_ALIAS, model.getCharacterAlias());
    }
}
