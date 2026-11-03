package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectActivityEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectActivityModel;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ProjectActivityResourceAssembler {

    public ProjectActivityModel.Activity toActivityModel(ProjectActivityEntity entity) {
        if (entity == null || entity.getActivityGuid() == null) {
            return null;
        }

        MultiPolygon geometry = entity.getGeometry();
        if (geometry != null && geometry.getSRID() == 0) {
            geometry.setSRID(4326);
        }

        return ProjectActivityModel.Activity.builder()
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
                .build();
    }

    public List<ProjectActivityModel> toModels(List<ProjectActivityEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        Map<UUID, ProjectActivityModel> projectMap = new LinkedHashMap<>();

        for (ProjectActivityEntity entity : entities) {
            if (entity == null || entity.getProjectGuid() == null) {
                continue;
            }

            ProjectActivityModel model = projectMap.computeIfAbsent(entity.getProjectGuid(), k -> {
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
                        .activities(new ArrayList<>())
                        .lastUpdated(entity.getLastUpdated())
                        .build();
            });

            if (entity.getActivityGuid() != null) {
                if (model.getActivities() == null) {
                    model.setActivities(new ArrayList<>());
                }
                ProjectActivityModel.Activity activity = toActivityModel(entity);
                if (activity != null) {
                    model.getActivities().add(activity);
                }
            }
        }

        return new ArrayList<>(projectMap.values());
    }
}
