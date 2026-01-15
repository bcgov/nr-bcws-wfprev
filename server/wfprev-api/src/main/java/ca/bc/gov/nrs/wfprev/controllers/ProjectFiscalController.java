package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfone.common.service.api.NotFoundException;
import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.services.ProjectFiscalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
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

import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/projects/{projectId}/projectFiscals")
public class ProjectFiscalController extends CommonController {
    private final ProjectFiscalService projectFiscalService;

    public ProjectFiscalController(ProjectFiscalService projectFiscalService) {
        super(ProjectFiscalController.class.getName());
        this.projectFiscalService = projectFiscalService;
    }

    @GetMapping
    @Operation(summary = "Fetch all Project Fiscal Resources",
            description = "Fetch all Project Fiscal Resources",
            security = @SecurityRequirement(name = "Webade-OAUTH2",
                    scopes = {"WFPREV"}),
            extensions = {@Extension(properties = {@ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited")})})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollectionModel.class))), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))})
    @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
    @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
    public ResponseEntity<CollectionModel<ProjectFiscalModel>> getAllProjectFiscals(@PathVariable("projectId") String projectId) {
        log.debug(" >> getAllProjectFiscals");
        ResponseEntity<CollectionModel<ProjectFiscalModel>> response;

        try {
            response = ok(projectFiscalService.getAllProjectFiscals(projectId));
        } catch (ServiceException e) {
            response = internalServerError();
            log.error(" ### ServiceException while fetching Project Fiscals", e);
        } catch (RuntimeException e) {
            response = internalServerError();
            log.error(" ### Error while fetching Project Fiscals", e);
        }

        log.debug(" << getAllProjectFiscals");
        return response;
    }

    @PostMapping
    @Operation(summary = "Create a Project Fiscal Resource",
            description = "Create a new Project Fiscal Resource",
            security = @SecurityRequirement(name = "Webade-OAUTH2",
                    scopes = {"WFPREV"}))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = ProjectFiscalModel.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))
    })
    public ResponseEntity<ProjectFiscalModel> createProjectFiscal(
            @Valid @RequestBody ProjectFiscalModel projectFiscalModel) {
        log.debug(" >> createProjectFiscal");
        ResponseEntity<ProjectFiscalModel> response;

        try {
            ProjectFiscalModel createdModel = projectFiscalService.createProjectFiscal(projectFiscalModel);
            response = ResponseEntity.status(201).body(createdModel);
        } catch (DataIntegrityViolationException e) {
            response = badRequest();
            log.error(" ### DataIntegrityViolationException while creating Project Fiscal", e);
        } catch (ServiceException e) {
            response = internalServerError();
            log.error(" ### Service Exception while creating Project Fiscal", e);
        } catch (Exception e) {
            response = internalServerError();
            log.error(" ### Error while creating Project Fiscal", e);
        }

        log.debug(" << createProjectFiscal");
        return response;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Project Fiscal Resource",
            description = "Update Project Fiscal Resource",
            security = @SecurityRequirement(name = "Webade-OAUTH2",
                    scopes = {"WFPREV"}),
            extensions = {@Extension(properties = {@ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited")})})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProjectFiscalModel.class)), headers = {@Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class))}), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))})
    @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
    @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
    public ResponseEntity<ProjectFiscalModel> updateProjectFiscal(@RequestBody ProjectFiscalModel resource, @PathVariable("id") String id) {
        log.debug(" >> updateProject");
        ResponseEntity<ProjectFiscalModel> response;

        try {
            // ensure that the user hasn't changed the primary key
            if (id.equalsIgnoreCase(resource.getProjectPlanFiscalGuid())) {
                ProjectFiscalModel updatedResource = projectFiscalService.updateProjectFiscal(resource);
                response = updatedResource == null ? notFound() : ok(updatedResource);
            } else {
                response = badRequest();
            }
        } catch (EntityNotFoundException e) {
            response = notFound();
            log.warn(" ### Project Fiscal not found with id: {}", id, e);
        }

        log.debug(" << updateProject");
        return response;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch a Project Fiscal Resource",
            description = "Fetch a Project Fiscal Resource",
            security = @SecurityRequirement(name = "Webade-OAUTH2",
                    scopes = {"WFPREV"}),
            extensions = {@Extension(properties = {@ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited")})})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProjectFiscalModel.class))), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))})
    @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
    @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
    public ResponseEntity<ProjectFiscalModel> getProjectFiscal(@PathVariable("id") String id) {
        log.debug(" >> getProjectFiscal");
        ResponseEntity<ProjectFiscalModel> response;

        try {
            ProjectFiscalModel projectFiscalModel = projectFiscalService.getProjectFiscal(id);
            response = projectFiscalModel == null ? notFound() : ok(projectFiscalModel);
        } catch (Exception e) {
            response = internalServerError();
            log.error(" ### Error while fetching Project Fiscal", e);
        }

        log.debug(" << getProjectFiscal");
        return response;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Project Fiscal Resource",
            description = "Delete a specific Project Fiscal Resource by its ID",
            security = @SecurityRequirement(name = "Webade-OAUTH2",
                    scopes = {"WFPREV"}))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))
    })
    public ResponseEntity<Void> deleteProjectFiscal(@PathVariable("id") String id,
                                                    @RequestParam(name = "deleteFiles", required = false, defaultValue = "false") boolean deleteFiles) {
        log.debug(" >> deleteProjectFiscal with id: {}", id);

        try {
            projectFiscalService.deleteProjectFiscal(id, deleteFiles);
            log.debug(" << deleteProjectFiscal success");
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.warn(" ### Project Fiscal not found with id: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn(" ### Invalid ID provided: {}", id, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error(" ### Error while deleting Project Fiscal with id: {}", id, e);
            return internalServerError();
        }
    }
}
