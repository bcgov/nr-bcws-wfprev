package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.SpatialController;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectActivityEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectActivityModel;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class ProjectActivityResourceAssembler extends RepresentationModelAssemblerSupport<ProjectActivityEntity, ProjectActivityModel> {

    public ProjectActivityResourceAssembler() {
        super(SpatialController.class, ProjectActivityModel.class);
    }

    @Override
    public ProjectActivityModel toModel(ProjectActivityEntity entity) {
        if (entity == null) {
            return null;
        }

        if (entity.getActivityGuid() == null) {
            return ProjectActivityModel.builder()
                    .projectGuid(entity.getProjectGuid())
                    .lastUpdated(entity.getLastUpdated())
                    .build();
        }

        MultiPolygon geometry = entity.getGeometry();
        if (geometry != null && geometry.getSRID() == 0) {
            geometry.setSRID(4326);
        }

        return ProjectActivityModel.builder()
                .projectGuid(entity.getProjectGuid())
                .projectNumber(entity.getProjectNumber())
                .projectTypeCode(entity.getProjectTypeCode())
                .projectTypeDesc(entity.getProjectTypeDesc())
                .primaryObjectiveTypeCode(entity.getPrimaryObjectiveTypeCode())
                .primaryObjectiveTypeDesc(entity.getPrimaryObjectiveTypeDesc())
                .secondaryObjectiveTypeCode(entity.getSecondaryObjectiveTypeCode())
                .secondaryObjectiveTypeDesc(entity.getSecondaryObjectiveTypeDesc())
                .resultsProjectCode(entity.getResultsProjectCode())
                .projectPlanFiscalGuid(entity.getProjectPlanFiscalGuid())
                .fiscalYear(entity.getFiscalYear())
                .projectFiscalName(entity.getProjectFiscalName())
                .activityGuid(entity.getActivityGuid())
                .activityName(entity.getActivityName())
                .activityStatusCode(entity.getActivityStatusCode())
                .activityStatusDesc(entity.getActivityStatusDesc())
                .activityStartDate(entity.getActivityStartDate())
                .activityEndDate(entity.getActivityEndDate())
                .completedAreaHa(entity.getCompletedAreaHa())
                .outstandingObligationsInd(entity.getOutstandingObligationsInd())
                .geometry(geometry)
                .lastUpdated(entity.getLastUpdated())
                .build();
    }

    public ProjectActivityEntity toEntity(ProjectActivityModel model) {
        if (model == null) {
            return null;
        }

        return ProjectActivityEntity.builder()
                .rowId(model.getActivityGuid() != null ? model.getActivityGuid() : model.getProjectGuid())
                .projectGuid(model.getProjectGuid())
                .projectNumber(model.getProjectNumber())
                .projectTypeCode(model.getProjectTypeCode())
                .projectTypeDesc(model.getProjectTypeDesc())
                .primaryObjectiveTypeCode(model.getPrimaryObjectiveTypeCode())
                .primaryObjectiveTypeDesc(model.getPrimaryObjectiveTypeDesc())
                .secondaryObjectiveTypeCode(model.getSecondaryObjectiveTypeCode())
                .secondaryObjectiveTypeDesc(model.getSecondaryObjectiveTypeDesc())
                .resultsProjectCode(model.getResultsProjectCode())
                .projectPlanFiscalGuid(model.getProjectPlanFiscalGuid())
                .fiscalYear(model.getFiscalYear())
                .projectFiscalName(model.getProjectFiscalName())
                .activityGuid(model.getActivityGuid())
                .activityName(model.getActivityName())
                .activityStatusCode(model.getActivityStatusCode())
                .activityStatusDesc(model.getActivityStatusDesc())
                .activityStartDate(model.getActivityStartDate())
                .activityEndDate(model.getActivityEndDate())
                .completedAreaHa(model.getCompletedAreaHa())
                .outstandingObligationsInd(model.getOutstandingObligationsInd())
                .geometry(model.getGeometry())
                .lastUpdated(model.getLastUpdated())
                .build();
    }
}

