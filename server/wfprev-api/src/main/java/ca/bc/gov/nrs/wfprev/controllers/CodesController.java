package ca.bc.gov.nrs.wfprev.controllers;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.services.CodesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(value="/codes")
public class CodesController extends CommonController {
  private CodesService codesService;

  public CodesController(CodesService codesService) {
    super(CodesController.class.getName());
    this.codesService = codesService;
  }

  @GetMapping("/{codeTable}")
  @Operation(summary = "Fetch all Forest Area Code Resources",
             description = "Fetch all Forest Area Code Resources",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollectionModel.class))), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameters({ @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER), @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) })
  public ResponseEntity getCodes(@PathVariable("codeTable") String codeTable) {
    log.debug(" >> getCodes");
    ResponseEntity response;

    try {
      switch(codeTable) {
        case CodeTables.FOREST_AREA_CODE -> {
          response = ok(codesService.getAllForestAreaCodes());
        }
        case CodeTables.GENERAL_SCOPE_CODE -> {
          response =  ok(codesService.getAllGeneralScopeCodes());
        }
        case CodeTables.PROJECT_TYPE_CODE -> {
          response =  ok(codesService.getAllProjectTypeCodes());
        }
        case CodeTables.PROGRAM_AREA_CODE -> {
          response =  ok(codesService.getAllProgramAreaCodes());
        }

        default -> {
          response = internalServerError();
        }
      }

    } catch (ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while fetching codes", e);
    }

    log.debug(" << getCodes");
    return response;
  }

  @GetMapping("/{codeTable}/{id}")
  @Operation(summary = "Fetch a Code Resource",
             description = "Fetch a Code Resource",
             security = @SecurityRequirement(name = "Webade-OAUTH2",
             scopes = { "WFPREV" }),
             extensions = { @Extension(properties = { @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited") }) })
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollectionModel.class))), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))) })
  @Parameters({ @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER), @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) })
  public ResponseEntity getCodeById(@PathVariable("codeTable") String codeTable, @PathVariable("id") String id) {
    log.debug(" >> getCodeById");
    CommonModel resource;
    ResponseEntity response;

    try {
      switch(codeTable) {
        case CodeTables.FOREST_AREA_CODE -> {
          resource = codesService.getForestAreaCodeById(id);
        }
        case CodeTables.GENERAL_SCOPE_CODE -> {
          resource =  codesService.getGeneralScopeCodeById(id);
        }
        case CodeTables.PROJECT_TYPE_CODE -> {
          resource =  codesService.getProjectTypeCodeById(id);
        }
        case CodeTables.PROGRAM_AREA_CODE -> {
          resource =  codesService.getProgramAreaCodeById(id);
        }
        default -> {
          resource = null;
        }
      }

      response = resource == null ? notFound() : ok(resource);
    } catch (ServiceException e) {
      response = internalServerError();
      log.error(" ### Error while fetching code", e);
    }

    log.debug(" << getCodeById");
    return response;
  }
}
