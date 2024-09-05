package ca.bc.gov.nrs.wfprev.controllers;

import java.util.Date;
import java.util.UUID;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfprev.data.resources.ExampleCodeModel;
import ca.bc.gov.nrs.wfprev.data.resources.ExampleModel;
import ca.bc.gov.nrs.wfprev.services.ExampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;

/**
 * Your Rest controller. This is where you define the request mappings that consumers
 * can hit. This should include any additional rules needed for the security implementation
 * 
 * Typically, a controller will not contain any direct business logic. This should be in
 * service implementations. The controller will only handle passing the request through
 * to services (after validation, etc), and returning the response.
 * 
 * This means that even though we have repository objects here in the controller for this
 * example, generally, you won't do that unless it's for something really simple that
 * doesn't need a service implementation
 * 
 * Note: We could use the more traditional Java Interface/Impl architecture if desired
 * This allows us to put a lot of the annotation mess for swagger out of the way of implementation
 */

@RestController
@Slf4j
@RequestMapping(value="wfprev")
public class ExampleController extends CommonController {
  private ExampleService exampleService;

  public ExampleController(ExampleService exampleService) {
    super(ExampleController.class.getName());
    this.exampleService = exampleService;
  }

  @GetMapping("/examples")
  @Operation(summary = "Fetch all Example Resources",
             description = "Fetch all Example Resources",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "ExampleScope" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollectionModel.class))), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameters({ @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER), @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) })
  public ResponseEntity<CollectionModel<ExampleModel>> getAllExamples() {
    log.debug(" >> getAllExamples");
    ResponseEntity<CollectionModel<ExampleModel>> response;

    try {
      response = ok(exampleService.getAllExamples());
    } catch (ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while fetching examples", e);
    }

    log.debug(" << getAllExamples");
    return response;
  }
   
  @GetMapping("/examples/{id}")
  @Operation(summary = "Fetch an Example Resource by ID",
             description = "Fetch an Example Resource by ID",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "ExampleScope" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExampleModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameters({ @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER), @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) })
  public ResponseEntity<ExampleModel> getExampleById(@PathVariable("id") String id) {
    log.debug(" >> getExampleById {}", id);
    ResponseEntity<ExampleModel> response;

    try {
      ExampleModel resource = exampleService.getExampleById(id);
      response = resource == null ? notFound() : ok(resource);
    } catch(ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while fetching example {}", id, e);
    }
    
    log.debug(" << getExampleById");
    return response;
  }

  @PostMapping("/examples")
  @Operation(summary = "Create an Example Resource",
             description = "Create an Example Resource",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "ExampleScope" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "OK", content = @Content(schema = @Schema(implementation = ExampleModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameters({ @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER), @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) })
  public ResponseEntity<ExampleModel> createExample(@RequestBody ExampleModel resource) {
    log.debug(" >> createExample");
    ResponseEntity<ExampleModel> response;

    try {
      // set the default values for a newly created resource
      resource.setCreateDate(new Date());
      resource.setCreatedBy(getWebAdeAuthentication().getUserId());
      resource.setUpdatedBy(getWebAdeAuthentication().getUserId());
      resource.setRevisionCount(0);
      resource.setExampleGuid(UUID.randomUUID().toString());

      ExampleModel newResource = exampleService.createOrUpdateExample(resource);
      response = newResource == null ? badRequest() : created(newResource);
    } catch(ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while creating resource", e);
    }
    
    log.debug(" << createExample");
    return response;
  }

  @PutMapping("/examples/{id}")
  @Operation(summary = "Create an Example Resource",
             description = "Create an Example Resource",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "ExampleScope" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "OK", content = @Content(schema = @Schema(implementation = ExampleModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameters({ @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER), @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) })
  public ResponseEntity<ExampleModel> updateExample(@RequestBody ExampleModel resource, @PathVariable("id") String id) {
    log.debug(" >> updateExample");
    ResponseEntity<ExampleModel> response;

    try {
      // Update the UpdatedBy
      resource.setUpdatedBy(getWebAdeAuthentication().getUserId());
      // ensure that the user hasn't changed the primary key
      if (id.equalsIgnoreCase(resource.getExampleGuid())) {
        ExampleModel updatedResource = exampleService.createOrUpdateExample(resource);
        response = updatedResource == null ? badRequest() : created(updatedResource);
      } else {
        response = badRequest();
      }
    } catch(ServiceException e) {
      // most responses here will actually be Bad Requests, not Internal Server Errors
      // This would be an ideal place to expand the "Catch" and return sensible
      // HTTP status codes
      response = internalServerError();
      log.error(" ### Error while updating resource", e);
    }
    
    log.debug(" << updateExample");
    return response;
  }
  
  @GetMapping("/exampleCodes")
  @Operation(summary = "Fetch all Example Code Resources",
    description = "Fetch all Example Code ResourceS",
    security = @SecurityRequirement(name = "Webade-OAUTH2",
    scopes = { "ExampleScope" }),
    extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollectionModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameters({ @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER), @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) })
  public ResponseEntity<CollectionModel<ExampleCodeModel>> getAllExampleCodes() {
    log.debug(" >> getAllExampleCodes");
    ResponseEntity<CollectionModel<ExampleCodeModel>> response;

    try {
      response = ok(exampleService.getAllExampleCodes());
    } catch (ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while fetching exampleCodes", e);
    }

    log.debug(" << getAllExampleCodes");
    return response;
  }
   
  @GetMapping("/exampleCodes/{id}")
  @Operation(summary = "Fetch an Example Code Resource by ID",
    description = "Fetch an Example Code Resource by ID",
    security = @SecurityRequirement(name = "Webade-OAUTH2",
    scopes = { "ExampleScope" }),
    extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExampleCodeModel.class)), headers = { @Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class)) }), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameters({ @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER), @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) })
  public ResponseEntity<ExampleCodeModel> getExampleCodeById(@PathVariable("id") String id) {
    log.debug(" >> getExampleCodeById {}", id);
    ResponseEntity<ExampleCodeModel> response;

    try {
      ExampleCodeModel resource = exampleService.getExampleCodeById(id);
      response = resource == null ? notFound() : ok(resource);
    } catch(ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while fetching exampleCode {}", id, e);
    }

    log.debug(" << getExampleCodeById");
    return response;
  }
}
