package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfprev.data.models.FileAttachmentModel;
import ca.bc.gov.nrs.wfprev.services.FileAttachmentService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@Slf4j
@RequestMapping(value = "/projects/{projectGuid}/projectFiscals/{projectPlanFiscalGuid}/activities/{activityGuid}/attachments")
public class ActivityAttachmentController extends CommonController {
    private final FileAttachmentService fileAttachmentService;
    private final ActivityService activityService;

    public ActivityAttachmentController(FileAttachmentService fileAttachmentService, ActivityService activityService) {
        super(ActivityAttachmentController.class.getName());
        this.fileAttachmentService = fileAttachmentService;
        this.activityService = activityService;
    }

    @GetMapping
    @Operation(
            summary = "Fetch all File Attachments for an Activity",
            description = "Fetch all File Attachments for an Activity",
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
    public ResponseEntity<CollectionModel<FileAttachmentModel>> getAllFileAttachments(
            @PathVariable String projectGuid,
            @PathVariable String projectPlanFiscalGuid,
            @PathVariable String activityGuid) {
        log.debug(" >> getAllFileAttachments");
        try {
            if (!isValidActivity(projectGuid, projectPlanFiscalGuid, activityGuid)) {
                log.warn(" ### Invalid activityGuid for : {}", activityGuid);
                return notFound();
            }
            return ok(fileAttachmentService.getAllActivityAttachments(activityGuid));
        } catch (RuntimeException e) {
            log.error(" ### Error while fetching File Attachments for Activity", e);
            return internalServerError();
        }
    }

    @GetMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = FileAttachmentModel.class)))
    })
    @Operation(
            summary = "Fetch a File Attachment for an Activity",
            description = "Fetch a specific File Attachment by ID",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"})
    )
    public ResponseEntity<FileAttachmentModel> getFileAttachment(
            @PathVariable String id,
            @PathVariable String projectGuid,
            @PathVariable String projectPlanFiscalGuid,
            @PathVariable String activityGuid) {
        log.debug(" >> getFileAttachment: {}", id);
        try {
            if (!isValidActivity(projectGuid, projectPlanFiscalGuid, activityGuid)) {
                log.warn(" ### Invalid activityGuid for getFileAttachment: {}", activityGuid);
                return notFound();
            }
            FileAttachmentModel resource = fileAttachmentService.getFileAttachmentById(id);
            return resource == null ? notFound() : ok(resource);
        } catch (EntityNotFoundException e) {
            log.warn(" ### File Attachment not found: {}", id, e);
            return notFound();
        } catch (Exception e) {
            log.error(" ### Error while fetching File Attachment for Activity", e);
            return internalServerError();
        }
    }

    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(schema = @Schema(implementation = FileAttachmentModel.class)))
    })
    @Operation(
            summary = "Create a File Attachment for an Activity",
            description = "Create a new File Attachment for an Activity",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"})
    )
    public ResponseEntity<FileAttachmentModel> createFileAttachment(
            @Valid @RequestBody FileAttachmentModel resource,
            @PathVariable String projectGuid,
            @PathVariable String projectPlanFiscalGuid,
            @PathVariable String activityGuid) {
        log.debug(" >> createFileAttachment");
        try {
            if (!isValidActivity(projectGuid, projectPlanFiscalGuid, activityGuid)) {
                log.warn(" ### Invalid activityGuid for createFileAttachment: {}", activityGuid);
                return notFound();
            }

            initializeNewActivityAttachment(resource);

            FileAttachmentModel newResource = fileAttachmentService.createFileAttachment(resource);
            return ResponseEntity.status(201).body(newResource);
        } catch (DataIntegrityViolationException e) {
            log.error(" ### DataIntegrityViolationException while creating File Attachment for Activity", e);
            return badRequest();
        } catch (IllegalArgumentException e) {
            log.error(" ### IllegalArgumentException while creating File Attachment for Activity", e);
            return badRequest();
        } catch (RuntimeException e) {
            log.error(" ### RuntimeException while creating File Attachment for Activity", e);
            return internalServerError();
        }
    }

    @PutMapping("/{id}")
    @Operation(
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"}),
            description = "Update an existing File Attachment for an Activity",
            summary = "Update File Attachment for an Activity"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal Server Error Response"),
            @ApiResponse(responseCode = "404", description = "Not Found Response"),
            @ApiResponse(responseCode = "400", description = "Bad Request Response"),
            @ApiResponse(responseCode = "200", description = "OK Response")
    })
    public ResponseEntity<FileAttachmentModel> updateFileAttachment(
            @PathVariable String id,
            @PathVariable String projectGuid,
            @PathVariable String projectPlanFiscalGuid,
            @PathVariable String activityGuid,
            @Valid @RequestBody FileAttachmentModel resource) {
        log.debug(" >> updateFileAttachment");
        try {
            if (!isValidActivity(projectGuid, projectPlanFiscalGuid, activityGuid)) {
                log.warn(" ### Invalid activityGuid for updateFileAttachment: {}", activityGuid);
                return notFound();
            }
            resource.setUpdateDate(new Date());
            FileAttachmentModel updatedResource = fileAttachmentService.updateFileAttachment(resource);
            return updatedResource == null ? notFound() : ok(updatedResource);
        } catch (DataIntegrityViolationException e) {
            log.error(" ### DataIntegrityViolationException while updating File Attachment for Activity", e);
            return badRequest();
        } catch (EntityNotFoundException e) {
            log.warn(" ### File Attachment not found for update: {}", id, e);
            return notFound();
        } catch (IllegalArgumentException e) {
            log.error(" ### IllegalArgumentException while updating File Attachment for Activity", e);
            return badRequest();
        } catch (RuntimeException e) {
            log.error(" ### RuntimeException while updating File Attachment for Activity", e);
            return internalServerError();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an File Attachment for an Activity",
            description = "Delete a specific File Attachment by ID",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"})
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal Server Error Response"),
            @ApiResponse(responseCode = "404", description = "Not Found Response"),
            @ApiResponse(responseCode = "400", description = "Bad Request Response"),
            @ApiResponse(responseCode = "204", description = "No Content Response")
    })
    public ResponseEntity<Void> deleteFileAttachment(
            @PathVariable String id,
            @PathVariable String projectGuid,
            @PathVariable String projectPlanFiscalGuid,
            @PathVariable String activityGuid,
            @RequestParam(name = "deleteFileFromWfdm", required = false, defaultValue = "false") boolean deleteFileFromWfdm) {
        log.debug(" >> deleteFileAttachment");
        try {
            if (!isValidActivity(projectGuid, projectPlanFiscalGuid, activityGuid)) {
                log.warn(" ### Invalid activityGuid for deleteFileAttachment: {}", activityGuid);
                return notFound();
            }
            fileAttachmentService.deleteFileAttachment(id, deleteFileFromWfdm);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.warn(" ### File Attachment for deletion not found: {}", id, e);
            return notFound();
        } catch (Exception e) {
            log.error(" ### Error while deleting File Attachment for Activity", e);
            return internalServerError();
        }
    }

    private boolean isValidActivity(String projectGuid, String projectPlanFiscalGuid, String activityGuid) {
        try {
            return activityService.getActivity(projectGuid, projectPlanFiscalGuid, activityGuid) != null;
        } catch (Exception e) {
            log.error(" ### Error while validating activityGuid: {}", activityGuid, e);
            return false;
        }
    }

    private void initializeNewActivityAttachment(FileAttachmentModel resource) {
        resource.setCreateUser(getWebAdeAuthentication().getUserId());
        resource.setUpdateUser(getWebAdeAuthentication().getUserId());
        resource.setUploadedByUserId(getWebAdeAuthentication().getUserId());
        resource.setUploadedByUserGuid(getWebAdeAuthentication().getClientId());
        resource.setUploadedByUserType(getWebAdeAuthentication().getUserTypeCode());
        resource.setUploadedByTimestamp(new Date());
        resource.setRevisionCount(0);
    }

}
