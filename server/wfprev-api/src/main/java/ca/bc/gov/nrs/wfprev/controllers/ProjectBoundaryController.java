package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.services.CoordinatesService;
import ca.bc.gov.nrs.wfprev.services.ProjectBoundaryService;
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
@RequestMapping(value = "/projects/{projectGuid}/projectBoundary")
public class ProjectBoundaryController extends CommonController {
    private final ProjectBoundaryService projectBoundaryService;
    private final CoordinatesService coordinatesService;

    public ProjectBoundaryController(ProjectBoundaryService projectBoundaryService, CoordinatesService coordinatesService) {
        super(ProjectBoundaryController.class.getName());
        this.projectBoundaryService = projectBoundaryService;
        this.coordinatesService = coordinatesService;
    }

    @GetMapping
    @Operation(
            summary = "Fetch all Project Boundaries",
            description = "Fetch all Project Boundaries for an Project",
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
    public ResponseEntity<CollectionModel<ProjectBoundaryModel>> getAllProjectBoundaries(
            @PathVariable String projectGuid) {
        log.debug(" >> getAllProjectBoundaries");

        try {
            return ok(projectBoundaryService.getAllProjectBoundaries(projectGuid));

        } catch (RuntimeException e) {
            log.error(" ### Error while fetching Project Boundaries", e);
            return internalServerError();
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Fetch an Project Boundary",
            description = "Fetch a specific Project Boundary by ID",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"})
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProjectBoundaryModel.class))),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ProjectBoundaryModel> getProjectBoundary(
            @PathVariable String projectGuid,
            @PathVariable String id) {
        log.debug(" >> getProjectBoundary for boundary: {}", id);

        try {
            ProjectBoundaryModel resource = projectBoundaryService.getProjectBoundary(projectGuid, id);
            return resource == null ? notFound() : ok(resource);
        } catch (Exception e) {
            log.error(" ### Error while fetching Project Boundary", e);
            return internalServerError();
        }
    }

    @PostMapping
    @Operation(
            summary = "Create an Project Boundary",
            description = "Create a new Project Boundary for an Project",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"})
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(schema = @Schema(implementation = ProjectBoundaryModel.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ProjectBoundaryModel> createProjectBoundary(
            @PathVariable String projectGuid,
            @Valid @RequestBody ProjectBoundaryModel resource) {
        log.debug(" >> createProjectBoundary");

        try {
            resource.setCreateDate(new Date());
            resource.setCreateUser(getWebAdeAuthentication().getUserId());
            resource.setUpdateDate(new Date());
            resource.setUpdateUser(getWebAdeAuthentication().getUserId());
            resource.setRevisionCount(0);

            ProjectBoundaryModel newResource = projectBoundaryService.createProjectBoundary(projectGuid, resource);
            coordinatesService.updateProjectCoordinates(projectGuid);

            return ResponseEntity.status(201).body(newResource);
        } catch (DataIntegrityViolationException e) {
            log.error(" ### DataIntegrityViolationException while creating Project Boundary", e);
            return badRequest();
        } catch (IllegalArgumentException e) {
            log.error(" ### IllegalArgumentException while creating Project Boundary", e);
            return badRequest();
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update Project Boundary",
            description = "Update an existing Project Boundary",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"})
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ProjectBoundaryModel> updateProjectBoundary(
            @PathVariable String projectGuid,
            @PathVariable String id,
            @Valid @RequestBody ProjectBoundaryModel resource) {
        log.debug(" >> updateProjectBoundary");

        try {
            resource.setUpdateDate(new Date());
            resource.setUpdateUser(getWebAdeAuthentication().getUserId());

            ProjectBoundaryModel updatedResource = projectBoundaryService.updateProjectBoundary(
                    projectGuid, resource);
            coordinatesService.updateProjectCoordinates(projectGuid);

            return updatedResource == null ? notFound() : ok(updatedResource);
        } catch (DataIntegrityViolationException e) {
            log.error(" ### DataIntegrityViolationException while updating Project Boundary", e);
            return badRequest();
        } catch (IllegalArgumentException e) {
            log.error(" ### IllegalArgumentException while updating Project Boundary", e);
            return badRequest();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an Project Boundary",
            description = "Delete a specific Project Boundary by ID",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"})
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Void> deleteProjectBoundary(
            @PathVariable String projectGuid,
            @PathVariable String id,
            @RequestParam(name = "deleteFiles", required = false, defaultValue = "false") boolean deleteFiles) {
        log.debug(" >> deleteProjectBoundary");

        try {
            projectBoundaryService.deleteProjectBoundary(projectGuid, id, deleteFiles);
            coordinatesService.updateProjectCoordinates(projectGuid);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error(" ### Error while deleting Project Boundary", e);
            return internalServerError();
        }
    }
}
