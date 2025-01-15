package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.GeneralScopeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestAreaCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.GeneralScopeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectResourceAssemblerTest {
    private ProjectResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ProjectResourceAssembler();
    }

    @Test
    void testToEntity_Success() {
        // given there is a project model
        ProjectModel projectModel = new ProjectModel();
        String projectGuid = UUID.randomUUID().toString();
        String programAreaGuid = UUID.randomUUID().toString();
        projectModel.setProjectGuid(projectGuid);
        projectModel.setPrimaryObjectiveTypeCode("random_test_string_1");
        projectModel.setSecondaryObjectiveTypeCode("random_test_string_2");
        projectModel.setTertiaryObjectiveTypeCode("random_test_string_3");
        projectModel.setIsMultiFiscalYearProj(false);
        projectModel.setProjectNumber(1000);
        projectModel.setProgramAreaGuid(programAreaGuid);
        projectModel.setForestAreaCode(new ForestAreaCodeModel());
        projectModel.setGeneralScopeCode(new GeneralScopeCodeModel());
        projectModel.setProjectTypeCode(new ProjectTypeCodeModel());

        // when it is submitted to the toEntity method
        ProjectEntity projectEntity = assembler.toEntity(projectModel);

        // then it should have all the same values in the entity object
        GeneralScopeCodeEntity generalScopeCodeEntity = new GeneralScopeCodeEntity(); // Mock or create an instance
        ForestAreaCodeEntity forestAreaCodeEntity = new ForestAreaCodeEntity();
        ProjectTypeCodeEntity projectTypeCodeEntity = new ProjectTypeCodeEntity();

        ProjectEntity expectedProjectEntity = ProjectEntity.builder().projectGuid(UUID.fromString(projectGuid))
                .primaryObjectiveTypeCode("random_test_string_1")
                .secondaryObjectiveTypeCode("random_test_string_2")
                .tertiaryObjectiveTypeCode("random_test_string_3").projectNumber(1000).programAreaGuid(UUID.fromString(programAreaGuid))
                .forestAreaCode(forestAreaCodeEntity)
                .generalScopeCode(generalScopeCodeEntity)
                .projectTypeCode(projectTypeCodeEntity).build();
        assertEquals(expectedProjectEntity, projectEntity);
    }

    @Test
    void testToModel_Success() {
        // Create and populate the ProjectTypeCodeEntity
        ProjectTypeCodeEntity projectTypeCodeEntity = new ProjectTypeCodeEntity();
        projectTypeCodeEntity.setProjectTypeCode("FUEL_MGMT");
        projectTypeCodeEntity.setDescription("Fuel Management");

        // Create and populate the ProjectEntity
        ProjectEntity projectEntity = new ProjectEntity();
        String projectGuid = UUID.randomUUID().toString();
        String programAreaGuid = UUID.randomUUID().toString();
        projectEntity.setProjectGuid(UUID.fromString(projectGuid));
        projectEntity.setProgramAreaGuid(UUID.fromString(programAreaGuid));
        projectEntity.setProjectTypeCode(projectTypeCodeEntity);
        projectEntity.setPrimaryObjectiveTypeCode("random_test_string_1");
        projectEntity.setSecondaryObjectiveTypeCode("random_test_string_2");
        projectEntity.setTertiaryObjectiveTypeCode("random_test_string_3");
        projectEntity.setIsMultiFiscalYearProj(false);
        projectEntity.setForestAreaCode(new ForestAreaCodeEntity());
        projectEntity.setGeneralScopeCode(new GeneralScopeCodeEntity());

        // Perform the conversion
        ProjectModel projectModel = assembler.toModel(projectEntity);

        // Create the expected ProjectModel
        ProjectModel expectedProjectModel = ProjectModel.builder()
                .projectGuid(projectGuid)
                .programAreaGuid(programAreaGuid)
                .projectTypeCode(ProjectTypeCodeModel.builder()
                        .projectTypeCode("FUEL_MGMT")
                        .description("Fuel Management")
                        .build())
                .primaryObjectiveTypeCode("random_test_string_1")
                .secondaryObjectiveTypeCode("random_test_string_2")
                .tertiaryObjectiveTypeCode("random_test_string_3")
                .isMultiFiscalYearProj(false)
                .forestAreaCode(new ForestAreaCodeModel())
                .generalScopeCode(new GeneralScopeCodeModel())
                .build();

        // Assert that the actual and expected models match
        assertEquals(expectedProjectModel, projectModel);
    }

    @Test
    void testUpdateEntity_Success() {
        ProjectModel model = new ProjectModel();
        model.setProjectName("Updated Project");
        model.setLatitude(BigDecimal.valueOf(50.123));
        model.setForestAreaCode(new ForestAreaCodeModel());

        ProjectEntity existingEntity = new ProjectEntity();
        existingEntity.setProjectGuid(UUID.randomUUID());
        existingEntity.setProjectName("Old Project");

        ProjectEntity updatedEntity = assembler.updateEntity(model, existingEntity);

        assertNotNull(updatedEntity);
        assertEquals("Updated Project", updatedEntity.getProjectName());
        assertEquals(existingEntity.getProjectGuid(), updatedEntity.getProjectGuid());
        assertEquals(BigDecimal.valueOf(50.123), updatedEntity.getLatitude());
    }

}
