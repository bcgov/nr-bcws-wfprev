package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import ca.bc.gov.nrs.wfprev.services.FeaturesService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
public class FeaturesController extends CommonController {

    private final FeaturesService featuresService;

    public FeaturesController(FeaturesService featuresService) {
        super(FeaturesController.class.getName());
        this.featuresService = featuresService;
    }

    @GetMapping("/features")
    @Operation(
            summary = "Fetch all GeoJson features",
            description = "Fetch all GeoJson features",
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
    public ResponseEntity<Map<String, Object>> getAllFeatures(
            @RequestParam(required = false) UUID projectGuid,
            @RequestParam(name = "programAreaGuids", required = false) List<UUID> programAreaGuid,
            @RequestParam(name = "fiscalYears", required = false) List<String> fiscalYear,
            @RequestParam(name = "activityCategoryCodes", required = false) List<String> activityCategoryCode,
            @RequestParam(name = "planFiscalStatusCodes", required = false) List<String> planFiscalStatusCode,
            @RequestParam(name = "forestRegionOrgUnitIds", required = false) List<String> forestRegionOrgUnitId,
            @RequestParam(name = "forestDistrictOrgUnitIds", required = false) List<String> forestDistrictOrgUnitId,
            @RequestParam(name = "fireCentreOrgUnitIds",  required = false) List<String> fireCentreOrgUnitId,
            @RequestParam(name = "projectTypeCodes", required = false) List<String> projectTypeCode,
            @RequestParam(required = false) String searchText,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "20") int pageRowCount,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        try {
            FeatureQueryParams queryParams = new FeatureQueryParams();
            queryParams.setProjectGuid(projectGuid); 
            queryParams.setProgramAreaGuids(programAreaGuid);
            queryParams.setFiscalYears(fiscalYear);
            queryParams.setActivityCategoryCodes(activityCategoryCode);
            queryParams.setPlanFiscalStatusCodes(planFiscalStatusCode);
            queryParams.setForestRegionOrgUnitIds(forestRegionOrgUnitId);
            queryParams.setForestDistrictOrgUnitIds(forestDistrictOrgUnitId);
            queryParams.setFireCentreOrgUnitIds(fireCentreOrgUnitId);
            queryParams.setProjectTypeCodes(projectTypeCode);
            queryParams.setSearchText(searchText);
            queryParams.setSortBy(sortBy);
            queryParams.setSortDirection(sortDirection);

            Map<String, Object> result = featuresService.getAllFeatures(queryParams, pageNumber, pageRowCount);
            return ResponseEntity.ok(result);

        } catch(ServiceException e) {
            log.error("Error encountered while fetching features:", e);
            return internalServerError();
        }
    }
}
