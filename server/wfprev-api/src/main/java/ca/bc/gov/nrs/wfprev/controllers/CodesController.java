package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.services.CodesService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(value = "/codes")
public class CodesController extends CommonController {
    private CodesService codesService;

    public CodesController(CodesService codesService) {
        super(CodesController.class.getName());
        this.codesService = codesService;
    }

    @GetMapping("/{codeTable}")
    @Operation(summary = "Fetch all codes from the specified code table",
            description = "Fetch all code resources for the specified table",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"}))
    public ResponseEntity<CollectionModel<?>> getCodes(@PathVariable("codeTable") String codeTable) {
        log.debug(" >> getCodes");
        try {
            CollectionModel<?> result;

            switch (codeTable) {
                case CodeTables.FOREST_AREA_CODE -> result = codesService.getAllForestAreaCodes();
                case CodeTables.GENERAL_SCOPE_CODE -> result = codesService.getAllGeneralScopeCodes();
                case CodeTables.PROJECT_TYPE_CODE -> result = codesService.getAllProjectTypeCodes();
                case CodeTables.PROGRAM_AREA_CODE -> result = codesService.getAllProgramAreaCodes();
                case CodeTables.FOREST_REGION_CODE -> result = codesService.getAllForestRegionCodes();
                case CodeTables.FOREST_DISTRICT_CODE -> result = codesService.getAllForestDistrictCodes();
                case CodeTables.BC_PARKS_REGION_CODE -> result = codesService.getAllBCParksRegionCodes();
                case CodeTables.BC_PARKS_SECTION_CODE -> result = codesService.getAllBCParksSectionCodes();
                case CodeTables.OBJECTIVE_TYPE_CODE -> result = codesService.getAllObjectiveTypeCodes();
                case CodeTables.PROJECT_PLAN_STATUS_CODE -> result = codesService.getAllProjectPlanStatusCodes();
                case CodeTables.ACTIVITY_STATUS_CODE -> result = codesService.getAllActivityStatusCodes();
                case CodeTables.RISK_RATING_CODE -> result = codesService.getAllRiskRatingCodes();
                case CodeTables.CONTRACT_PHASE_CODE -> result = codesService.getAllContractPhaseCodes();
                case CodeTables.ACTIVITY_CATEGORY_CODE -> result = codesService.getAllActivityCategoryCodes();
                case CodeTables.PLAN_FISCAL_STATUS_CODE -> result = codesService.getAllPlanFiscalStatusCodes();
                case CodeTables.ANCILLARY_FUNDING_SOURCE_CODE -> result = codesService.getAllAncillaryFundingSourceCodes();
                case CodeTables.FUNDING_SOURCE_CODE -> result = codesService.getAllFundingSourceCodes();
                case CodeTables.SOURCE_OBJECT_NAME_CODE -> result = codesService.getAllSourceObjectNameCodes();
                case CodeTables.ATTACHMENT_CONTENT_TYPE_CODE -> result = codesService.getAllAttachmentContentTypeCodes();
                case CodeTables.SILVICULTURE_BASE_CODE -> result = codesService.getAllSilvicultureBaseCodes();
                case CodeTables.SILVICULTURE_METHOD_CODE -> result = codesService.getAllSilvicultureMethodCodes();
                case CodeTables.SILVICULTURE_TECHNIQUE_CODE -> result = codesService.getAllSilvicultureTechniqueCodes();
                case CodeTables.PROPOSAL_TYPE_CODE -> result = codesService.getAllProposalTypeCodes();
                case CodeTables.WUI_RISK_CLASS_CODE -> result = codesService.getAllWuiRiskClassCodes();
                case CodeTables.EVALUATION_CRITERIA_CODE -> result = codesService.getAllEvaluationCriteriaCodes();
                case CodeTables.PROJECT_STATUS_CODE -> result = codesService.getAllProjectStatusCodes();
                case CodeTables.WILDFIRE_ORG_UNIT -> result = codesService.getAllWildfireOrgUnits();
                case CodeTables.REPORTING_PERIOD_CODE -> result = codesService.getAllReportingPeriodCodes();
                case CodeTables.PROGRESS_STATUS_CODE -> result = codesService.getAllReportingPeriodCodes();

                default -> {
                    log.error("Invalid code table: {}", codeTable);
                    return internalServerError();
                }
            }

            return ok(result);
        } catch (ServiceException e) {
            log.error(" ### Error while fetching codes", e);
            return internalServerError();
        } finally {
            log.debug(" << getCodes");
        }
    }

    @GetMapping("/{codeTable}/{id}")
    @Operation(
            summary = "Fetch a Code Resource",
            description = "Fetch a Code Resource by ID from the specified code table",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"}),
            extensions = {
                    @Extension(
                            properties = {
                                    @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"),
                                    @ExtensionProperty(name = "throttling-tier", value = "Unlimited")
                            })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollectionModel.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "412", description = "Precondition Failed"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))
    })
    public ResponseEntity<?> getCodeById(
            @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
            @PathVariable("codeTable") String codeTable,

            @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
            @PathVariable("id") String id
    ) {
        log.debug(" >> getCodeById");

        try {
            CommonModel<?> resource = fetchCodeById(codeTable, id);
            if (resource == null) {
                return notFound();
            }
            return ok(resource);
        } catch (ServiceException e) {
            log.error(" ### Error while fetching code", e);
            return internalServerError();
        } catch (NumberFormatException e) {
            log.error(" ### Invalid ID format", e);
            return ResponseEntity.badRequest().body("Invalid ID format for code table: " + codeTable);
        } finally {
            log.debug(" << getCodeById");
        }
    }

    private CommonModel<?> fetchCodeById(String codeTable, String id) throws ServiceException {
        return switch (codeTable) {
            case CodeTables.FOREST_AREA_CODE -> codesService.getForestAreaCodeById(id);
            case CodeTables.GENERAL_SCOPE_CODE -> codesService.getGeneralScopeCodeById(id);
            case CodeTables.PROJECT_TYPE_CODE -> codesService.getProjectTypeCodeById(id);
            case CodeTables.PROGRAM_AREA_CODE -> codesService.getProgramAreaCodeById(id);
            case CodeTables.FOREST_REGION_CODE -> codesService.getForestRegionCodeById(Integer.parseInt(id));
            case CodeTables.FOREST_DISTRICT_CODE -> codesService.getForestDistrictCodeById(Integer.parseInt(id));
            case CodeTables.BC_PARKS_REGION_CODE -> codesService.getBCParksRegionCodeById(Integer.parseInt(id));
            case CodeTables.BC_PARKS_SECTION_CODE -> codesService.getBCParksSectionCodeById(Integer.parseInt(id));
            case CodeTables.OBJECTIVE_TYPE_CODE -> codesService.getObjectiveTypeCodeById(id);
            case CodeTables.PROJECT_PLAN_STATUS_CODE -> codesService.getProjectPlanStatusCodeById(id);
            case CodeTables.ACTIVITY_STATUS_CODE -> codesService.getActivityStatusCodeById(id);
            case CodeTables.RISK_RATING_CODE -> codesService.getRiskRatingCodeById(id);
            case CodeTables.CONTRACT_PHASE_CODE -> codesService.getContractPhaseCodeById(id);
            case CodeTables.ACTIVITY_CATEGORY_CODE -> codesService.getActivityCategoryCodeById(id);
            case CodeTables.PLAN_FISCAL_STATUS_CODE -> codesService.getPlanFiscalStatusCodeById(id);
            case CodeTables.ANCILLARY_FUNDING_SOURCE_CODE -> codesService.getAncillaryFundingSourceCodeById(id);
            case CodeTables.FUNDING_SOURCE_CODE -> codesService.getFundingSourceCodeById(id);
            case CodeTables.SOURCE_OBJECT_NAME_CODE -> codesService.getSourceObjectNameCodeById(id);
            case CodeTables.ATTACHMENT_CONTENT_TYPE_CODE -> codesService.getAttachmentContentTypeCodeById(id);
            case CodeTables.SILVICULTURE_BASE_CODE -> codesService.getSilvicultureBaseCodeById(id);
            case CodeTables.SILVICULTURE_METHOD_CODE -> codesService.getSilvicultureMethodCodeById(id);
            case CodeTables.SILVICULTURE_TECHNIQUE_CODE -> codesService.getSilvicultureTechniqueCodeById(id);
            case CodeTables.PROPOSAL_TYPE_CODE -> codesService.getProposalTypeCodeById(id);
            case CodeTables.WUI_RISK_CLASS_CODE -> codesService.getWuiRiskClassCodeById(id);
            case CodeTables.EVALUATION_CRITERIA_CODE -> codesService.getEvaluationCriteriaCodeById(id);
            case CodeTables.PROJECT_STATUS_CODE -> codesService.getProjectStatusCodeById(id);
            case CodeTables.WILDFIRE_ORG_UNIT -> codesService.getWildfireOrgUnitById(id);
            case CodeTables.REPORTING_PERIOD_CODE -> codesService.getReportingPeriodCodeById(id);
            default -> null;
        };
    }
}