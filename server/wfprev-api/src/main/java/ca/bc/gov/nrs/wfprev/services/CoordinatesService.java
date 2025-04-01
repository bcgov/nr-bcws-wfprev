package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CoordinatesService implements CommonService {

    private final ProjectService projectService;
    private final ProjectBoundaryService projectBoundaryService;
    private final ProjectFiscalService projectFiscalService;
    private final ActivityService activityService;
    private final ActivityBoundaryService activityBoundaryService;

    public CoordinatesService(
            ProjectService projectService,
            ProjectBoundaryService projectBoundaryService,
            ProjectFiscalService projectFiscalService,
            ActivityService activityService,
            ActivityBoundaryService activityBoundaryService) {
        this.projectService = projectService;
        this.projectBoundaryService = projectBoundaryService;
        this.projectFiscalService = projectFiscalService;
        this.activityService = activityService;
        this.activityBoundaryService = activityBoundaryService;
    }

    public void updateProjectCoordinates(String projectGuid) {
        ProjectModel project = projectService.getProjectById(projectGuid);
        List<PGpolygon> allPolygons = getAllPolygonsForProject(projectGuid);

        if (project != null && !allPolygons.isEmpty()) {

            PGpoint centroid = calculateCentroid(allPolygons);

            BigDecimal latitude = BigDecimal.valueOf(centroid.y);
            BigDecimal longitude = BigDecimal.valueOf(centroid.x);

            project.setLatitude(latitude.scale() > 7 ? latitude.setScale(7, RoundingMode.HALF_UP) : latitude);
            project.setLongitude(longitude.scale() > 7 ? longitude.setScale(7, RoundingMode.HALF_UP) : longitude);
            projectService.updateProject(project);
        } else {
            throw new EntityNotFoundException("Project could not be found while attempting to update coordinates");
        }
    }

    public PGpoint calculateCentroid(List<PGpolygon> polygons) {
        // Initialize variables for centroid calculation
        double totalX = 0;
        double totalY = 0;
        int totalPoints = 0;

        // Iterate through all polygons
        for (PGpolygon polygon : polygons) {
            if (polygon.points == null) {
                continue; // Skip empty polygons
            }

            // Sum up all points' coordinates
            for (PGpoint point : polygon.points) {
                totalX += point.x;
                totalY += point.y;
                totalPoints++;
            }
        }

        // Check if any points were processed
        if (totalPoints == 0) {
            throw new IllegalArgumentException("No points found in the polygons.");
        }

        // Calculate the average of all points
        double centroidX = totalX / totalPoints;
        double centroidY = totalY / totalPoints;

        return new PGpoint(centroidX, centroidY);

    }

    public List<PGpolygon> getAllPolygonsForProject(String projectGuid) {
        List<PGpolygon> polygons = new ArrayList<>();
        CollectionModel<ProjectBoundaryModel> projectBoundaries = projectBoundaryService.getAllProjectBoundaries(projectGuid);

        for (ProjectBoundaryModel projectBoundary : projectBoundaries) {
            polygons.add(projectBoundary.getBoundaryGeometry());
        }

        CollectionModel<ProjectFiscalModel> projectFiscals = projectFiscalService.getAllProjectFiscals(String.valueOf(projectGuid));

        for (ProjectFiscalModel projectFiscal : projectFiscals) {
            String fiscalGuid = projectFiscal.getProjectPlanFiscalGuid();
            CollectionModel<ActivityModel> activities = activityService.getAllActivities(projectGuid, fiscalGuid);
            for (ActivityModel activity : activities) {
                CollectionModel<ActivityBoundaryModel> activityBoundaries = activityBoundaryService.getAllActivityBoundaries(projectGuid, fiscalGuid, activity.getActivityGuid());
                for (ActivityBoundaryModel activityBoundary : activityBoundaries) {
                    polygons.add(activityBoundary.getGeometry());
                }
            }
        }
        return polygons;
    }
}
