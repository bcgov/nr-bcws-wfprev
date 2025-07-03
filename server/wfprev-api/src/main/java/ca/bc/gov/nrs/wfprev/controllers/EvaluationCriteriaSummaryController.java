package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSummaryModel;
import ca.bc.gov.nrs.wfprev.services.EvaluationCriteriaSummaryService;
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
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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

@RestController
@Slf4j
@RequestMapping(value = "/projects/{projectGuid}/evaluationCriteriaSummary")
public class EvaluationCriteriaSummaryController extends CommonController {
    private final EvaluationCriteriaSummaryService evaluationCriteriaSummaryService;

    public EvaluationCriteriaSummaryController(EvaluationCriteriaSummaryService evaluationCriteriaSummaryService) {
        super(EvaluationCriteriaSummaryController.class.getName());
        this.evaluationCriteriaSummaryService = evaluationCriteriaSummaryService;
    }

    @GetMapping
    @Operation(summary = "Fetch all Evaluation Criteria Summary Resources",
            description = "Fetch all Evaluation Criteria Summary Resources",
            security = @SecurityRequirement(name = "Webade-OAUTH2",
                    scopes = {"WFPREV"}),
            extensions = {@Extension(properties = {@ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited")})})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollectionModel.class))), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))})
    @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
    @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
    public ResponseEntity<CollectionModel<EvaluationCriteriaSummaryModel>> getAllEvaluationCriteriaSummaries(@PathVariable("projectGuid") String projectGuid) {
        log.debug(" >> getAllEvaluationCriteriaSummaries");
        ResponseEntity<CollectionModel<EvaluationCriteriaSummaryModel>> response;

        response = ok(evaluationCriteriaSummaryService.getAllEvaluationCriteriaSummaries(projectGuid));

        log.debug(" << getAllEvaluationCriteriaSummaries");
        return response;
    }

    @PostMapping
    @Operation(summary = "Create a Evaluation Criteria Summary Resource",
            description = "Create a new Evaluation Criteria Summary Resource",
            security = @SecurityRequirement(name = "Webade-OAUTH2",
                    scopes = {"WFPREV"}))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = EvaluationCriteriaSummaryModel.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))
    })
    public ResponseEntity<EvaluationCriteriaSummaryModel> createEvaluationCriteriaSummary(
            @Valid @RequestBody EvaluationCriteriaSummaryModel evaluationCriteriaSummaryModel) {
        log.debug(" >> createEvaluationCriteriaSummary");
        ResponseEntity<EvaluationCriteriaSummaryModel> response;

        EvaluationCriteriaSummaryModel createdModel = evaluationCriteriaSummaryService.createEvaluationCriteriaSummary(evaluationCriteriaSummaryModel);
        response = ResponseEntity.status(201).body(createdModel);

        log.debug(" << createEvaluationCriteriaSummary");
        return response;
    }

    @PutMapping("/{evaluationCriteriaSummaryGuid}")
    @Operation(summary = "Update Evaluation Criteria Summary Resource",
            description = "Update Evaluation Criteria Summary Resource",
            security = @SecurityRequirement(name = "Webade-OAUTH2",
                    scopes = {"WFPREV"}),
            extensions = {@Extension(properties = {@ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited")})})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = EvaluationCriteriaSummaryModel.class)), headers = {@Header(name = "ETag", description = "The ETag response-header field provides the current value of the entity tag for the requested variant.", schema = @Schema(implementation = String.class))}), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))})
    @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
    @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
    public ResponseEntity<EvaluationCriteriaSummaryModel> updateEvaluationCriteriaSummary(@Valid @RequestBody EvaluationCriteriaSummaryModel resource, @PathVariable("evaluationCriteriaSummaryGuid") String evaluationCriteriaSummaryGuid) {
        log.debug(" >> updateProject");
        ResponseEntity<EvaluationCriteriaSummaryModel> response;

        if (evaluationCriteriaSummaryGuid.equalsIgnoreCase(resource.getEvaluationCriteriaSummaryGuid())) {
            EvaluationCriteriaSummaryModel updatedResource = evaluationCriteriaSummaryService.updateEvaluationCriteriaSummary(resource);
            response = updatedResource == null ? notFound() : ok(updatedResource);
        } else {
            response = badRequest();
        }

        log.debug(" << updateProject");
        return response;
    }

    @GetMapping("/{evaluationCriteriaSummaryGuid}")
    @Operation(summary = "Fetch a Evaluation Criteria Summary Resource",
            description = "Fetch a Evaluation Criteria Summary Resource",
            security = @SecurityRequirement(name = "Webade-OAUTH2",
                    scopes = {"WFPREV"}),
            extensions = {@Extension(properties = {@ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"), @ExtensionProperty(name = "throttling-tier", value = "Unlimited")})})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = EvaluationCriteriaSummaryModel.class))), @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not Found"), @ApiResponse(responseCode = "409", description = "Conflict"), @ApiResponse(responseCode = "412", description = "Precondition Failed"), @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))})
    @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
    @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
    public ResponseEntity<EvaluationCriteriaSummaryModel> getEvaluationCriteriaSummary(@PathVariable("evaluationCriteriaSummaryGuid") String evaluationCriteriaSummaryGuid) {
        log.debug(" >> getEvaluationCriteriaSummary");
        ResponseEntity<EvaluationCriteriaSummaryModel> response;

        EvaluationCriteriaSummaryModel evaluationCriteriaSummaryModel = evaluationCriteriaSummaryService.getEvaluationCriteriaSummary(evaluationCriteriaSummaryGuid);
        response = evaluationCriteriaSummaryModel == null ? notFound() : ok(evaluationCriteriaSummaryModel);

        log.debug(" << getEvaluationCriteriaSummary");
        return response;
    }

    @DeleteMapping("/{evaluationCriteriaSummaryGuid}")
    @Operation(summary = "Delete a Evaluation Criteria Summary Resource",
            description = "Delete a specific Evaluation Criteria Summary Resource by its ID",
            security = @SecurityRequirement(name = "Webade-OAUTH2",
                    scopes = {"WFPREV"}))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))
    })
    public ResponseEntity<Void> deleteEvaluationCriteriaSummary(@PathVariable("evaluationCriteriaSummaryGuid") String evaluationCriteriaSummaryGuid) {
        log.debug(" >> deleteEvaluationCriteriaSummary with id: {}", evaluationCriteriaSummaryGuid);

        evaluationCriteriaSummaryService.deleteEvaluationCriteriaSummary(evaluationCriteriaSummaryGuid);
        log.debug(" << deleteEvaluationCriteriaSummary success");
        return ResponseEntity.noContent().build();
    }
}
