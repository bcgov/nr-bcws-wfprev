package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;

import java.util.UUID;

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
        projectModel.setProjectGuid(projectGuid);
        projectModel.setPrimaryObjectiveTypeCode("random_test_string_1");
        projectModel.setSecondaryObjectiveTypeCode("random_test_string_2");
        projectModel.setTertiaryObjectiveTypeCode("random_test_string_3");
        projectModel.setIsMultiFiscalYearProj(false);

        // when it is submitted to the toEntity method
        ProjectEntity projectEntity = assembler.toEntity(projectModel);

        // then it should have all the same values in the entity object
        ProjectEntity expectedProjectEntity = ProjectEntity.builder().projectGuid(UUID.fromString(projectGuid)).primaryObjectiveTypeCode("random_test_string_1").secondaryObjectiveTypeCode("random_test_string_2").tertiaryObjectiveTypeCode("random_test_string_3").build();
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
                .build();

        // Assert that the actual and expected models match
        assertEquals(expectedProjectModel, projectModel);
    }

}
