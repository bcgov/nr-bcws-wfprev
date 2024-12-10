package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ForestRegionCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestRegionCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ForestRegionCodeResourceAssemblerTest {

    public static final String ORG_UNIT_IDENTIFIER = "orgUnitIdentifier";
    public static final Date EFFECTIVE_DATE = new Date();
    public static final Date EXPIRY_DATE = new Date();
    public static final String FOREST_ORG_UNIT_TYPE_CODE = "forestOrgUnitTypeCode";
    public static final String PARENT_ORG_UNIT_IDENTIFIER = "parentOrgUnitIdentifier";
    public static final String ORG_UNIT_NAME = "orgUnitName";
    public static final Integer INTEGER_ALIAS = 1;
    public static final String CHARACTER_ALIAS = "characterAlias";
    ForestRegionCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ForestRegionCodeResourceAssembler();
    }

    @Test
    void toModel_ShouldConvertAllFields() {
        // Given
        ForestRegionCodeEntity entity = new ForestRegionCodeEntity();
        entity.setOrgUnitIdentifier(ORG_UNIT_IDENTIFIER);
        entity.setEffectiveDate(EFFECTIVE_DATE);
        entity.setExpiryDate(EXPIRY_DATE);
        entity.setForestOrgUnitTypeCode(FOREST_ORG_UNIT_TYPE_CODE);
        entity.setParentOrgUnitIdentifier(PARENT_ORG_UNIT_IDENTIFIER);
        entity.setOrgUnitName(ORG_UNIT_NAME);
        entity.setIntegerAlias(INTEGER_ALIAS);
        entity.setCharacterAlias(CHARACTER_ALIAS);

        // When

        ForestRegionCodeModel model = assembler.toModel(entity);
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
