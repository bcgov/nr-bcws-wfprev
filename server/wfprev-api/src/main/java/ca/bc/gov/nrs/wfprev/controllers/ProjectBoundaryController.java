package ca.bc.gov.nrs.wfprev.controllers;

import java.util.Date;
import java.util.UUID;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.services.ProjectBoundaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(value="/projectBoundaries")
public class ProjectBoundaryController extends CommonController {
  private ProjectBoundaryService projectBoundaryService;

  public ProjectBoundaryController(ProjectBoundaryService projectBoundaryService) {
    super(ProjectBoundaryController.class.getName());
    this.projectBoundaryService = projectBoundaryService;
  }

  @GetMapping
  @Operation(summary = "Fetch all Project Boundary Resources",
             description = "Fetch all Project Boundary Resources",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollectionModel.class))), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
  @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
  public ResponseEntity<CollectionModel<ProjectBoundaryModel>> getAllProjectBoundaries() {
    log.debug(" >> getAllProjectBoundaries");
    ResponseEntity<CollectionModel<ProjectBoundaryModel>> response;

    try {
      response = ok(projectBoundaryService.getAllProjectBoundaries());
    } catch (ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while fetching Projects", e);
    }

    log.debug(" << getAllProjectBoundaries");
    return response;
  }

  @GetMapping("/{id}")
  @Operation(summary = "Fetch project boundary by ID",
             description = "Fetch project boundary by ID",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProjectBoundaryModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
  @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
  public ResponseEntity<ProjectBoundaryModel> getById(@PathVariable("id") String id) {
    log.debug(" >> getById {}", id);
    ResponseEntity<ProjectBoundaryModel> response;

    try {
      ProjectBoundaryModel resource = projectBoundaryService.getProjectBoundaryById(id);
      response = resource == null ? notFound() : ok(resource);
    } catch(ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while fetching project {}", id, e);
    }
    
    log.debug(" << getById");
    return response;
  }

  @PostMapping(consumes = "application/json")
  @Operation(summary = "Create a project boundary Resource",
             description = "Create a project boundary Resource",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "OK", content = @Content(schema = @Schema(implementation = ProjectBoundaryModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
  @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
  public ResponseEntity<ProjectBoundaryModel> createProjectBoundary(@RequestBody ProjectBoundaryModel resource) {
    log.debug(" >> createProjectBoundary");
    ResponseEntity<ProjectBoundaryModel> response;

    try {
      // set the default values for a newly created resource
      resource.setCreateDate(new Date());
      resource.setCreateUser(getWebAdeAuthentication().getUserId());
      resource.setUpdateUser(getWebAdeAuthentication().getUserId());
      resource.setRevisionCount(0);
      resource.setProjectBoundaryGuid(UUID.randomUUID().toString());

      ProjectBoundaryModel newResource = projectBoundaryService.createOrUpdateProjectBoundary(resource);
      response = newResource == null ? badRequest() : created(newResource);
    } catch(ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while creating resource", e);
    }
    
    log.debug(" << createProjectBoundary");
    return response;
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update Project Boundary Resource",
             description = "Update Project Boundary Resource",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProjectBoundaryModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
  @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
  public ResponseEntity<ProjectBoundaryModel> updateProjectBoundary(@RequestBody ProjectBoundaryModel resource, @PathVariable("id") String id) {
    log.debug(" >> updateProjectBoundary");
    ResponseEntity<ProjectBoundaryModel> response;

    try {
      // Update the UpdatedBy
      resource.setUpdateUser(getWebAdeAuthentication().getUserId());
      // ensure that the user hasn't changed the primary key
      if (id.equalsIgnoreCase(resource.getProjectBoundaryGuid())) {
        ProjectBoundaryModel updatedResource = projectBoundaryService.createOrUpdateProjectBoundary(resource);
        response = updatedResource == null ? badRequest() : ok(updatedResource);
      } else {
        response = badRequest();
      }
    } catch(ServiceException e) {
      // most responses here will actually be Bad Requests, not Internal Server Errors
      // This would be an ideal place to expand the "Catch" and return sensible
      // HTTP status codes
      response = internalServerError();
      log.error(" ### Error while updating Program Area", e);
    }
    
    log.debug(" << updateProjectBoundary");
    return response;
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete Project Boundary Resource",
             description = "Delete Project Boundary Resource",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProjectBoundaryModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
  @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
  public ResponseEntity<ProjectBoundaryModel> deleteProjectBoundary(@PathVariable("id") String id) {
    log.debug(" >> deleteProjectBoundary");
    ResponseEntity<ProjectBoundaryModel> response;

    try {
      ProjectBoundaryModel resource = projectBoundaryService.deleteProjectBoundary(id);
      response = resource == null ? badRequest() : ok(resource);
    } catch(ServiceException e) {
      // most responses here will actually be Bad Requests, not Internal Server Errors
      // This would be an ideal place to expand the "Catch" and return sensible
      // HTTP status codes
      response = internalServerError();
      log.error(" ### Error while updating Project Boundary", e);
    }
    
    log.debug(" << deleteProjectBoundary");
    return response;
  }
}
