package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectLocationModel;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import ca.bc.gov.nrs.wfprev.services.ProjectLocationService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping(value = "/project-locations")
public class ProjectLocationController extends CommonController {

    private final ProjectLocationService projectLocationService;

    public ProjectLocationController(ProjectLocationService projectLocationService) {
        super(ProjectLocationController.class.getName());
        this.projectLocationService = projectLocationService;
    }

    @GetMapping
    @Operation(
            summary = "Fetch all Project Location Resources",
            description = "Returns projectGuid, latitude, and longitude for all projects with coordinates",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = { "WFPREV" }),
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
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = MessageListRsrc.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "412", description = "Precondition Failed"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))
    })
    @Parameter(
            name = HeaderConstants.VERSION_HEADER,
            description = HeaderConstants.VERSION_HEADER_DESCRIPTION,
            required = false,
            schema = @Schema(implementation = Integer.class),
            in = ParameterIn.HEADER
    )
    @Parameter(
            name = HeaderConstants.IF_MATCH_HEADER,
            description = HeaderConstants.IF_MATCH_DESCRIPTION,
            required = true,
            schema = @Schema(implementation = String.class),
            in = ParameterIn.HEADER
    )
    public ResponseEntity<CollectionModel<ProjectLocationModel>> getAllProjectLocations(
            @RequestParam(required = false) List<UUID> programAreaGuid,
            @RequestParam(required = false) List<String> fiscalYear,
            @RequestParam(required = false) List<String> activityCategoryCode,
            @RequestParam(required = false) List<String> planFiscalStatusCode,
            @RequestParam(required = false) List<String> forestRegionOrgUnitId,
            @RequestParam(required = false) List<String> forestDistrictOrgUnitId,
            @RequestParam(required = false) List<String> fireCentreOrgUnitId,
            @RequestParam(required = false) List<String> projectTypeCode,
            @RequestParam(required = false) String searchText
    ) {
        log.debug(" >> getAllProjectLocations");
        ResponseEntity<CollectionModel<ProjectLocationModel>> response;

        try {
            FeatureQueryParams queryParams = new FeatureQueryParams();
            queryParams.setProgramAreaGuids(programAreaGuid);
            queryParams.setFiscalYears(fiscalYear);
            queryParams.setActivityCategoryCodes(activityCategoryCode);
            queryParams.setPlanFiscalStatusCodes(planFiscalStatusCode);
            queryParams.setForestRegionOrgUnitIds(forestRegionOrgUnitId);
            queryParams.setForestDistrictOrgUnitIds(forestDistrictOrgUnitId);
            queryParams.setFireCentreOrgUnitIds(fireCentreOrgUnitId);
            queryParams.setProjectTypeCodes(projectTypeCode);
            queryParams.setSearchText(searchText);

            response = ok(projectLocationService.getAllProjectLocations(queryParams));
        } catch (ServiceException e) {
            response = internalServerError();
            log.error(" ### Error while fetching Project Locations", e);
        }

        log.debug(" << getAllProjectLocations");
        return response;
    }
}
