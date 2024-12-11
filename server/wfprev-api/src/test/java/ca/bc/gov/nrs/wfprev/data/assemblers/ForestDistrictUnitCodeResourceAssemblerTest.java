package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ForestOrgUnitCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestDistrictUnitCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ForestDistrictUnitCodeResourceAssemblerTest {

    public static final Integer ORG_UNIT_IDENTIFIER = 1;
    public static final Date EFFECTIVE_DATE = new Date();
    public static final Date EXPIRY_DATE = new Date();
    public static final String FOREST_ORG_UNIT_TYPE_CODE = "forestOrgUnitTypeCode";
    public static final Integer PARENT_ORG_UNIT_IDENTIFIER = 1;
    public static final String ORG_UNIT_NAME = "orgUnitName";
    public static final Integer INTEGER_ALIAS = 1;
    public static final String CHARACTER_ALIAS = "characterAlias";

    ForestDistrictUnitCodeResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ForestDistrictUnitCodeResourceAssembler();
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
        ForestDistrictUnitCodeModel model = assembler.toModel(entity);

        // Then
        assertEquals(ORG_UNIT_IDENTIFIER, model.getOrgUnitId());
        assertEquals(EFFECTIVE_DATE, model.getEffectiveDate());
        assertEquals(EXPIRY_DATE, model.getExpiryDate());
        assertEquals(FOREST_ORG_UNIT_TYPE_CODE, model.getForestOrgUnitTypeCode());
        assertEquals(PARENT_ORG_UNIT_IDENTIFIER.intValue(), Integer.parseInt(model.getParentOrgUnitId()));
        assertEquals(ORG_UNIT_NAME, model.getOrgUnitName());
        assertEquals(INTEGER_ALIAS, model.getIntegerAlias());
        assertEquals(CHARACTER_ALIAS, model.getCharacterAlias());
    }

    @Test
    void toCollectionModel_ShouldAddSelfLink() {
        // Given
        ForestOrgUnitCodeEntity entity1 = new ForestOrgUnitCodeEntity();
        entity1.setOrgUnitIdentifier(1);
        entity1.setEffectiveDate(EFFECTIVE_DATE);
        entity1.setExpiryDate(EXPIRY_DATE);

        ForestOrgUnitCodeEntity entity2 = new ForestOrgUnitCodeEntity();
        entity2.setOrgUnitIdentifier(2);
        entity2.setEffectiveDate(EFFECTIVE_DATE);
        entity2.setExpiryDate(EXPIRY_DATE);

        Iterable<ForestOrgUnitCodeEntity> entities = List.of(entity1, entity2);

        // When
        var collectionModel = assembler.toCollectionModel(entities);

        // Then
        assertEquals(2, collectionModel.getContent().size());
        assertEquals(
                "/codes/forestDistrictCodes",
                collectionModel.getLink("self").orElseThrow().getHref()
        );
    }
}