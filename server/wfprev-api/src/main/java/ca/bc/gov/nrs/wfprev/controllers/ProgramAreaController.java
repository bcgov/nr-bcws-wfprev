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
import ca.bc.gov.nrs.wfprev.data.models.ProgramAreaModel;
import ca.bc.gov.nrs.wfprev.services.ProgramAreaService;
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
@RequestMapping(value="/programAreas")
public class ProgramAreaController extends CommonController {
  private ProgramAreaService programAreaService;

  public ProgramAreaController(ProgramAreaService programAreaService) {
    super(ProgramAreaController.class.getName());
    this.programAreaService = programAreaService;
  }

  @GetMapping
  @Operation(summary = "Fetch all Program Area Resources",
             description = "Fetch all Program Area Resources",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollectionModel.class))), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
  @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
  public ResponseEntity<CollectionModel<ProgramAreaModel>> getAllProgramAreas() {
    log.debug(" >> getAllProgramAreas");
    ResponseEntity<CollectionModel<ProgramAreaModel>> response;

    try {
      response = ok(programAreaService.getAllProgramAreas());
    } catch (ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while fetching program areas", e);
    }

    log.debug(" << getAllProgramAreas");
    return response;
  }

  @GetMapping("/{id}")
  @Operation(summary = "Fetch program area by ID",
             description = "Fetch program area by ID",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProgramAreaModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
  @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
  public ResponseEntity<ProgramAreaModel> getById(@PathVariable("id") String id) {
    log.debug(" >> getById {}", id);
    ResponseEntity<ProgramAreaModel> response;

    try {
      ProgramAreaModel resource = programAreaService.getProgramAreaById(id);
      response = resource == null ? notFound() : ok(resource);
    } catch(ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while fetching Program Area {}", id, e);
    }
    
    log.debug(" << getById");
    return response;
  }

  @PostMapping
  @Operation(summary = "Create a Program Area Resource",
             description = "Create a Program Area Resource",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "OK", content = @Content(schema = @Schema(implementation = ProgramAreaModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
  @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
  public ResponseEntity<ProgramAreaModel> createProgramArea(@RequestBody ProgramAreaModel resource) {
    log.debug(" >> createProgramArea");
    ResponseEntity<ProgramAreaModel> response;

    try {
      // set the default values for a newly created resource
      resource.setCreateDate(new Date());
      resource.setCreateUser(getWebAdeAuthentication().getUserId());
      resource.setUpdateUser(getWebAdeAuthentication().getUserId());
      resource.setRevisionCount(0);
      resource.setProgramAreaGuid(UUID.randomUUID().toString());

      ProgramAreaModel newResource = programAreaService.createOrUpdateProgramArea(resource);
      response = newResource == null ? badRequest() : created(newResource);
    } catch(ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while creating resource", e);
    }
    
    log.debug(" << createProgramArea");
    return response;
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update Program Area Resource",
             description = "Update Program Area Resource",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProgramAreaModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
  @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
  public ResponseEntity<ProgramAreaModel> updateProgramArea(@RequestBody ProgramAreaModel resource, @PathVariable("id") String id) {
    log.debug(" >> updateProgramArea");
    ResponseEntity<ProgramAreaModel> response;

    try {
      // Update the UpdatedBy
      resource.setUpdateUser(getWebAdeAuthentication().getUserId());
      // ensure that the user hasn't changed the primary key
      if (id.equalsIgnoreCase(resource.getProgramAreaGuid())) {
        ProgramAreaModel updatedResource = programAreaService.createOrUpdateProgramArea(resource);
        response = updatedResource == null ? notFound() : ok(updatedResource);
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
    
    log.debug(" << updateProgramArea");
    return response;
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete Program Area Resource",
             description = "Delete Program Area Resource",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProgramAreaModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
  @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
  public ResponseEntity<ProgramAreaModel> deleteProgramArea(@PathVariable("id") String id) {
    log.debug(" >> deleteProgramArea");
    ResponseEntity<ProgramAreaModel> response;

    try {
      ProgramAreaModel resource = programAreaService.deleteProgramArea(id);
      response = resource == null ? notFound() : ok(resource);
    } catch(ServiceException e) {
      // most responses here will actually be Bad Requests, not Internal Server Errors
      // This would be an ideal place to expand the "Catch" and return sensible
      // HTTP status codes
      response = internalServerError();
      log.error(" ### Error while updating Program Area", e);
    }
    
    log.debug(" << deleteProgramArea");
    return response;
  }
}
