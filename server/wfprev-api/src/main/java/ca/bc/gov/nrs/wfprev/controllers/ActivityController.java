package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.services.ActivityService;
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

@RestController
@Slf4j
@RequestMapping(value = "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities")
public class ActivityController extends CommonController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        super(ActivityController.class.getName());
        this.activityService = activityService;
    }

    @GetMapping
    @Operation(
            summary = "Fetch all Activities",
            description = "Fetch all Activities for a Project Fiscal",
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
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = MessageListRsrc.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))
    })
    @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION,
            required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
    @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION,
            required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
    public ResponseEntity<CollectionModel<ActivityModel>> getAllActivities(
            @PathVariable("projectId") String projectId,
            @PathVariable("projectFiscalId") String projectFiscalId) {
        log.debug(" >> getAllActivities for projectFiscalId: {}", projectFiscalId);
        ResponseEntity<CollectionModel<ActivityModel>> response;

        try {
            response = ok(activityService.getAllActivities(projectId, projectFiscalId));
        } catch (RuntimeException e) {
            response = internalServerError();
            log.error(" ### Error while fetching Activities", e);
        }

        log.debug(" << getAllActivities");
        return response;
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update Activity",
            description = "Update an existing Activity",
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
                    content = @Content(schema = @Schema(implementation = ActivityModel.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = MessageListRsrc.class))),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))
    })
    public ResponseEntity<ActivityModel> updateActivity(
            @PathVariable("projectId") String projectId,
            @PathVariable("projectFiscalId") String projectFiscalId,
            @PathVariable("id") String id,
            @Valid @RequestBody ActivityModel activityModel) {
        log.debug(" >> updateActivity");
        ResponseEntity<ActivityModel> response;

        try {
            if (id.equalsIgnoreCase(activityModel.getActivityGuid())) {
                ActivityModel updatedModel = activityService.updateActivity(projectId, projectFiscalId, activityModel);
                response = updatedModel == null ? notFound() : ok(updatedModel);
            } else {
                response = badRequest();
            }

        } catch (DataIntegrityViolationException e) {
            log.error(" ### DataIntegrityViolationException while updating Activity", e);
            response = badRequest();
        } catch (EntityNotFoundException e) {
            response = notFound();
            log.warn(" ### Activity not found with id: {}", id, e);
        } catch (IllegalArgumentException e) {
            response = ResponseEntity.badRequest().build();
            log.error(" ### IllegalArgumentException while updating Activity", e);
        } catch (RuntimeException e) {
            log.error(" ### RuntimeException while updating Activity", e);
            response = internalServerError();
        }

        log.debug(" << updateActivity");
        return response;
    }

    @PostMapping
    @Operation(
            summary = "Create an Activity",
            description = "Create a new Activity for a Project Fiscal",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"}),
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"),
                            @ExtensionProperty(name = "throttling-tier", value = "Unlimited")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(schema = @Schema(implementation = ActivityModel.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = MessageListRsrc.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))
    })
    public ResponseEntity<ActivityModel> createActivity(
            @PathVariable("projectId") String projectId,
            @PathVariable("projectFiscalId") String projectFiscalId,
            @Valid @RequestBody ActivityModel activityModel) {
        log.debug(" >> createActivity");
        ResponseEntity<ActivityModel> response;

        try {
            ActivityModel createdModel = activityService.createActivity(projectId, projectFiscalId, activityModel);
            response = ResponseEntity.status(201).body(createdModel);
        } catch (DataIntegrityViolationException e) {
            response = badRequest();
            log.error(" ### DataIntegrityViolationException while creating Activity", e);
        } catch (IllegalArgumentException e) {
            response = ResponseEntity.badRequest().build();
            log.error(" ### IllegalArgumentException while creating Activity", e);
        } catch (RuntimeException e) {
            log.error(" ### RuntimeException while creating Activity", e);
            response = internalServerError();
        }

        log.debug(" << createActivity");
        return response;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Fetch an Activity",
            description = "Fetch a specific Activity by ID",
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
                    content = @Content(schema = @Schema(implementation = ActivityModel.class))),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))
    })
    public ResponseEntity<ActivityModel> getActivity(
            @PathVariable("projectId") String projectId,
            @PathVariable("projectFiscalId") String projectFiscalId,
            @PathVariable("id") String id) {
        log.debug(" >> getActivity");
        ResponseEntity<ActivityModel> response;

        try {
            ActivityModel activityModel = activityService.getActivity(projectId, projectFiscalId, id);
            response = activityModel == null ? notFound() : ok(activityModel);
        } catch (Exception e) {
            response = internalServerError();
            log.error(" ### Error while fetching Activity", e);
        }

        log.debug(" << getActivity");
        return response;
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an Activity",
            description = "Delete a specific Activity by ID",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"}),
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"),
                            @ExtensionProperty(name = "throttling-tier", value = "Unlimited")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))
    })
    public ResponseEntity<Void> deleteActivity(
            @PathVariable("projectId") String projectId,
            @PathVariable("projectFiscalId") String projectFiscalId,
            @PathVariable("id") String id,
            @RequestParam(name = "deleteFiles", required = false, defaultValue = "false") boolean deleteFiles) {
        log.debug(" >> deleteActivity with id: {}", id);

        try {
            activityService.deleteActivity(projectId, projectFiscalId, id, deleteFiles);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.warn(" ### Activity not found with id: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(" ### Error while deleting Activity with id: {}", id, e);
            return internalServerError();
        }
    }
}