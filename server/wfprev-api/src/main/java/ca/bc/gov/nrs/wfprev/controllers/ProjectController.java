package ca.bc.gov.nrs.wfprev.controllers;

import java.util.Date;
import java.util.UUID;

import org.springframework.hateoas.CollectionModel;
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
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.services.ProjectService;
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
@RequestMapping(value="/projects")
public class ProjectController extends CommonController {
  private ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    super(ProjectController.class.getName());
    this.projectService = projectService;
  }

  @GetMapping("/")
  @Operation(summary = "Fetch all Project Resources",
             description = "Fetch all Project Resources",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollectionModel.class))), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameters({ @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER), @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) })
  public ResponseEntity<CollectionModel<ProjectModel>> getAllProjects() {
    log.debug(" >> getAllProjects");
    ResponseEntity<CollectionModel<ProjectModel>> response;

    try {
      response = ok(projectService.getAllProjects());
    } catch (ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while fetching Projects", e);
    }

    log.debug(" << getAllProjects");
    return response;
  }

  @GetMapping("/{id}")
  @Operation(summary = "Fetch Project by ID",
             description = "Fetch Project by ID",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProjectModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameters({ @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER), @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) })
  public ResponseEntity<ProjectModel> getById(@PathVariable("id") String id) {
    log.debug(" >> getById {}", id);
    ResponseEntity<ProjectModel> response;

    try {
      ProjectModel resource = projectService.getProjectById(id);
      response = resource == null ? notFound() : ok(resource);
    } catch(ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while fetching project {}", id, e);
    }
    
    log.debug(" << getById");
    return response;
  }

  @PostMapping("/")
  @Operation(summary = "Create a Project Resource",
             description = "Create a Project Resource",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "OK", content = @Content(schema = @Schema(implementation = ProjectModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameters({ @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER), @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) })
  public ResponseEntity<ProjectModel> createProject(@RequestBody ProjectModel resource) {
    log.debug(" >> createProject");
    ResponseEntity<ProjectModel> response;

    try {
      // set the default values for a newly created resource
      resource.setCreateDate(new Date());
      resource.setCreateUser(getWebAdeAuthentication().getUserId());
      resource.setUpdateUser(getWebAdeAuthentication().getUserId());
      resource.setRevisionCount(0);
      resource.setProgramAreaGuid(UUID.randomUUID().toString());

      ProjectModel newResource = projectService.createOrUpdateProject(resource);
      response = newResource == null ? badRequest() : created(newResource);
    } catch(ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while creating resource", e);
    }
    
    log.debug(" << createProject");
    return response;
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update Project Resource",
             description = "Update Project Resource",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProjectModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameters({ @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER), @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) })
  public ResponseEntity<ProjectModel> updateProject(@RequestBody ProjectModel resource, @PathVariable("id") String id) {
    log.debug(" >> updateProject");
    ResponseEntity<ProjectModel> response;

    try {
      // Update the UpdatedBy
      resource.setUpdateUser(getWebAdeAuthentication().getUserId());
      // ensure that the user hasn't changed the primary key
      if (id.equalsIgnoreCase(resource.getProgramAreaGuid())) {
        ProjectModel updatedResource = projectService.createOrUpdateProject(resource);
        response = updatedResource == null ? badRequest() : ok(updatedResource);
      } else {
        response = badRequest();
      }
    } catch(ServiceException e) {
      // most responses here will actually be Bad Requests, not Internal Server Errors
      // This would be an ideal place to expand the "Catch" and return sensible
      // HTTP status codes
      response = internalServerError();
      log.error(" ### Error while updating Project", e);
    }
    
    log.debug(" << updateProject");
    return response;
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete Project Resource",
             description = "Delete Project Resource",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProjectModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameters({ @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER), @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) })
  public ResponseEntity<ProjectModel> deleteProject(@PathVariable("id") String id) {
    log.debug(" >> deleteProject");
    ResponseEntity<ProjectModel> response;

    try {
      ProjectModel resource = projectService.deleteProject(id);
      response = resource == null ? badRequest() : ok(resource);
    } catch(ServiceException e) {
      // most responses here will actually be Bad Requests, not Internal Server Errors
      // This would be an ideal place to expand the "Catch" and return sensible
      // HTTP status codes
      response = internalServerError();
      log.error(" ### Error while updating Project", e);
    }
    
    log.debug(" << deleteProject");
    return response;
  }
}
