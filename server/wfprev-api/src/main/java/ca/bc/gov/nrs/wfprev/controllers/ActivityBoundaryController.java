package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.services.ActivityBoundaryService;
import ca.bc.gov.nrs.wfprev.services.ActivityService;
import ca.bc.gov.nrs.wfprev.services.CoordinatesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@Slf4j
@RequestMapping(value = "/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/activityBoundary")
public class ActivityBoundaryController extends CommonController {
    private final ActivityService activityService;
    private final ActivityBoundaryService activityBoundaryService;
    private final CoordinatesService coordinatesService;

    public ActivityBoundaryController(ActivityBoundaryService activityBoundaryService, CoordinatesService coordinatesService, ActivityService activityService) {
        super(ActivityBoundaryController.class.getName());
        this.activityBoundaryService = activityBoundaryService;
        this.coordinatesService = coordinatesService;
        this.activityService = activityService;
    }

    @GetMapping
    @Operation(
            summary = "Fetch all Activity Boundaries",
            description = "Fetch all Activity Boundaries for an Activity",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"}),
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"),
                            @ExtensionProperty(name = "throttling-tier", value = "Unlimited")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CollectionModel.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))
    })
    @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION,
            required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
    @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION,
            required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
    public ResponseEntity<CollectionModel<ActivityBoundaryModel>> getAllActivityBoundaries(
            @PathVariable String projectGuid,
            @PathVariable String projectPlanFiscalGuid,
            @PathVariable String activityGuid) {
        log.debug(" >> getAllActivityBoundaries");

        try {
            return ok(activityBoundaryService.getAllActivityBoundaries(projectGuid, projectPlanFiscalGuid, activityGuid));
        } catch (RuntimeException e) {
            log.error(" ### Error while fetching Activity Boundaries", e);
            return internalServerError();
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Fetch an Activity Boundary",
            description = "Fetch a specific Activity Boundary by ID",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"})
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ActivityBoundaryModel.class))),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ActivityBoundaryModel> getActivityBoundary(
            @PathVariable String projectGuid,
            @PathVariable String projectPlanFiscalGuid,
            @PathVariable String activityGuid,
            @PathVariable String id) {
        log.debug(" >> getActivityBoundary for boundary: {}", id);

        try {
            ActivityBoundaryModel resource = activityBoundaryService.getActivityBoundary(
                    projectGuid, projectPlanFiscalGuid, activityGuid, id);
            return resource == null ? notFound() : ok(resource);
        } catch (EntityNotFoundException e) {
            log.warn(" ### Activity Boundary not found: {}", id, e);
            return notFound();
        } catch (Exception e) {
            log.error(" ### Error while fetching Activity Boundary", e);
            return internalServerError();
        }
    }

    @PostMapping
    @Operation(
            summary = "Create an Activity Boundary",
            description = "Create a new Activity Boundary for an Activity",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"})
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(schema = @Schema(implementation = ActivityBoundaryModel.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ActivityBoundaryModel> createActivityBoundary(
            @PathVariable String projectGuid,
            @PathVariable String projectPlanFiscalGuid,
            @PathVariable String activityGuid,
            @Valid @RequestBody ActivityBoundaryModel resource) {
        log.debug(" >> createActivityBoundary");

        try {
            resource.setCreateDate(new Date());
            resource.setUpdateDate(new Date());
            resource.setRevisionCount(0);

            ActivityBoundaryModel newResource = activityBoundaryService.createActivityBoundary(
                    projectGuid, projectPlanFiscalGuid, activityGuid, resource);
            coordinatesService.updateProjectCoordinates(projectGuid);

            // Update activity spatial indicator
            ActivityModel activity = activityService.getActivity(projectGuid, projectPlanFiscalGuid, activityGuid);
            if(activity != null) {
                activity.setIsSpatialAddedInd(true);
                activityService.updateActivity(projectGuid, projectPlanFiscalGuid, activity);

            }

            return ResponseEntity.status(201).body(newResource);
        } catch (DataIntegrityViolationException e) {
            log.error(" ### DataIntegrityViolationException while creating Activity Boundary", e);
            return badRequest();
        } catch (IllegalArgumentException e) {
            log.error(" ### IllegalArgumentException while creating Activity Boundary", e);
            return badRequest();
        } catch (RuntimeException e) {
            log.error(" ### RuntimeException while creating Activity Boundary", e);
            return internalServerError();
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update Activity Boundary",
            description = "Update an existing Activity Boundary",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"})
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ActivityBoundaryModel> updateActivityBoundary(
            @PathVariable String projectGuid,
            @PathVariable String projectPlanFiscalGuid,
            @PathVariable String activityGuid,
            @PathVariable String id,
            @Valid @RequestBody ActivityBoundaryModel resource) {
        log.debug(" >> updateActivityBoundary");

        try {
            resource.setUpdateDate(new Date());

            ActivityBoundaryModel updatedResource = activityBoundaryService.updateActivityBoundary(
                    projectGuid, projectPlanFiscalGuid, activityGuid, resource);
            coordinatesService.updateProjectCoordinates(projectGuid);

            // Update activity spatial indicator
            ActivityModel activity = activityService.getActivity(projectGuid, projectPlanFiscalGuid, activityGuid);
            if (activity != null) {
                activity.setIsSpatialAddedInd(true);
                activityService.updateActivity(projectGuid, projectPlanFiscalGuid, activity);
            }

            return updatedResource == null ? notFound() : ok(updatedResource);
        } catch (DataIntegrityViolationException e) {
            log.error(" ### DataIntegrityViolationException while updating Activity Boundary", e);
            return badRequest();
        } catch (EntityNotFoundException e) {
            log.warn(" ### Activity Boundary not found for update: {}", id, e);
            return notFound();
        } catch (IllegalArgumentException e) {
            log.error(" ### IllegalArgumentException while updating Activity Boundary", e);
            return badRequest();
        } catch (RuntimeException e) {
            log.error(" ### RuntimeException while updating Activity Boundary", e);
            return internalServerError();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an Activity Boundary",
            description = "Delete a specific Activity Boundary by ID",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"})
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Void> deleteActivityBoundary(
            @PathVariable String projectGuid,
            @PathVariable String projectPlanFiscalGuid,
            @PathVariable String activityGuid,
            @PathVariable String id,
            @RequestParam(name = "deleteFiles", required = false, defaultValue = "false") boolean deleteFiles) {
        log.debug(" >> deleteActivityBoundary");

        try {
            activityBoundaryService.deleteActivityBoundary(projectGuid, projectPlanFiscalGuid, activityGuid, id, deleteFiles);
            coordinatesService.updateProjectCoordinates(projectGuid);

            // Update activity spatial indicator
            ActivityModel activity = activityService.getActivity(projectGuid, projectPlanFiscalGuid, activityGuid);
            if(activity != null) {
                CollectionModel<ActivityBoundaryModel> boundaries = activityBoundaryService.getAllActivityBoundaries(projectGuid, projectPlanFiscalGuid, activityGuid);
                activity.setIsSpatialAddedInd(!boundaries.getContent().isEmpty());
                activityService.updateActivity(projectGuid, projectPlanFiscalGuid, activity);
            }

            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.warn(" ### Activity Boundary for deletion not found: {}", id, e);
            return notFound();
        } catch (Exception e) {
            log.error(" ### Error while deleting Activity Boundary", e);
            return internalServerError();
        }
    }
}
