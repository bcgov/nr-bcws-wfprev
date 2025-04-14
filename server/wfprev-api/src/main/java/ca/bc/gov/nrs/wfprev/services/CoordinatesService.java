package ca.bc.gov.nrs.wfprev.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

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
        List<MultiPolygon> allMultiPolygons = getAllPolygonsForProject(projectGuid);

        if (project != null && !allMultiPolygons.isEmpty()) {

            // Get the GeometryFactory from the first polygon
            // (We're just borrowing the factory, not limiting which polygons are included)
            Optional<MultiPolygon> firstNonNull = allMultiPolygons.stream()
                    .filter(Objects::nonNull)
                    .findFirst();

            if (firstNonNull.isEmpty()) {
                log.info("No valid MultiPolygon geometries found to calculate centroid.");
                return; 
            }

            GeometryFactory factory = firstNonNull.get().getFactory();

            // Convert the entire list of MultiPolygons to an array of Geometry objects
            Geometry[] geometryArray = allMultiPolygons.stream()
                    .filter(Objects::nonNull)
                    .toArray(Geometry[]::new);

            // Create a GeometryCollection containing all MultiPolygons from the list
            // This collection now includes every MultiPolygon, not just the first one
            GeometryCollection collection = factory.createGeometryCollection(geometryArray);

            // Get the centroid of the entire collection
            Point overallCentroid = collection.getCentroid();

            BigDecimal latitude = BigDecimal.valueOf(overallCentroid.getY());
            BigDecimal longitude = BigDecimal.valueOf(overallCentroid.getX());

            project.setLatitude(latitude.scale() > 7 ? latitude.setScale(7, RoundingMode.HALF_UP) : latitude);
            project.setLongitude(longitude.scale() > 7 ? longitude.setScale(7, RoundingMode.HALF_UP) : longitude);
            projectService.updateProject(project);
        } else if (project != null && allMultiPolygons.isEmpty()){
            log.info("No polygons found for project while attempting to update coordinates");
        }
        else {
            throw new EntityNotFoundException("Project could not be found while attempting to update coordinates");
        }
    }

    public List<MultiPolygon> getAllPolygonsForProject(String projectGuid) {
        List<MultiPolygon> polygons = new ArrayList<>();
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