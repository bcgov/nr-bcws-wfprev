package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import ca.bc.gov.nrs.wfprev.services.BoundaryTileService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@Validated
@RequestMapping("/tiles")
public class BoundaryTileController extends CommonController {

    private final BoundaryTileService boundaryTileService;
    private final ProjectLocationService projectLocationService;

    public BoundaryTileController(BoundaryTileService boundaryTileService, ProjectLocationService projectLocationService) {
        super(BoundaryTileController.class.getName());
        this.boundaryTileService = boundaryTileService;
        this.projectLocationService = projectLocationService;
    }

    @GetMapping(
            value = "/project_boundary/{z}/{x}/{y}.mvt",
            produces = "application/vnd.mapbox-vector-tile")
    @Operation(
            summary = "Fetch Project Boundaries as Vector Tiles",
            description = "Fetch Project Boundaries as Vector Tiles, optionally filtered by project GUIDs or feature query parameters",
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
    public ResponseEntity<byte[]> getProjectBoundaryTiles(
            @PathVariable int z, @PathVariable int x, @PathVariable int y,
            @RequestParam(name = "projectGuid", required = false) List<UUID> projectGuids,
            @RequestParam(required = false) List<UUID> programAreaGuids,
            @RequestParam(required = false) List<String> fiscalYears,
            @RequestParam(required = false) List<String> activityCategoryCodes,
            @RequestParam(required = false) List<String> planFiscalStatusCodes,
            @RequestParam(required = false) List<String> forestRegionOrgUnitIds,
            @RequestParam(required = false) List<String> forestDistrictOrgUnitIds,
            @RequestParam(required = false) List<String> fireCentreOrgUnitIds,
            @RequestParam(required = false) List<String> projectTypeCodes,
            @RequestParam(required = false) String searchText) {
        log.info(" >> getProjectBoundaryTiles");

        try {
            List<UUID> resolved = resolveProjectGuids(projectGuids, programAreaGuids, fiscalYears,
                    activityCategoryCodes, planFiscalStatusCodes, forestRegionOrgUnitIds,
                    forestDistrictOrgUnitIds, fireCentreOrgUnitIds, projectTypeCodes, searchText);

            if (resolved == null || resolved.isEmpty()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
                        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, immutable")
                        .body(new byte[0]);
            }

            byte[] mvt = boundaryTileService.getProjectBoundaryTile(z, x, y, resolved);
            if (mvt == null) mvt = new byte[0];

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, immutable")
                    .body(mvt);
        } catch (Exception e) {
            log.error(" ### Error while fetching Project Boundaries", e);
            return internalServerError();
        }
    }

    @GetMapping(
            value = "/activity_boundary/{z}/{x}/{y}.mvt",
            produces = "application/vnd.mapbox-vector-tile"
    )
    @Operation(
            summary = "Fetch activity boundaries for one or more projects",
            description = "Returns activity boundaries styled by fiscal year, optionally filtered by project GUIDs or feature query parameters",
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
    @Parameter(name = HeaderConstants.VERSION_HEADER, in = ParameterIn.HEADER)
    @Parameter(name = HeaderConstants.IF_MATCH_HEADER, in = ParameterIn.HEADER)
    public ResponseEntity<byte[]> getActivityBoundaryTiles(
            @PathVariable int z, @PathVariable int x, @PathVariable int y,
            @RequestParam(name = "projectGuid", required = false) List<UUID> projectGuids,
            @RequestParam(required = false) List<UUID> programAreaGuids,
            @RequestParam(required = false) List<String> fiscalYears,
            @RequestParam(required = false) List<String> activityCategoryCodes,
            @RequestParam(required = false) List<String> planFiscalStatusCodes,
            @RequestParam(required = false) List<String> forestRegionOrgUnitIds,
            @RequestParam(required = false) List<String> forestDistrictOrgUnitIds,
            @RequestParam(required = false) List<String> fireCentreOrgUnitIds,
            @RequestParam(required = false) List<String> projectTypeCodes,
            @RequestParam(required = false) String searchText) {
        log.info(">>getActivityBoundaryTiles");

        try {
            List<UUID> resolved = resolveProjectGuids(projectGuids, programAreaGuids, fiscalYears,
                    activityCategoryCodes, planFiscalStatusCodes, forestRegionOrgUnitIds,
                    forestDistrictOrgUnitIds, fireCentreOrgUnitIds, projectTypeCodes, searchText);

            if (resolved == null || resolved.isEmpty()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
                        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, immutable")
                        .body(new byte[0]);
            }

            byte[] mvt = boundaryTileService.getActivityBoundaryTile(z, x, y, resolved);
            if (mvt == null) mvt = new byte[0];

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, immutable")
                    .body(mvt);
        } catch (Exception e) {
            log.error("### Error while fetching Activity Boundaries", e);
            return internalServerError();
        }
    }

    private List<UUID> resolveProjectGuids(
            List<UUID> explicitGuids,
            List<UUID> programAreaGuids,
            List<String> fiscalYears,
            List<String> activityCategoryCodes,
            List<String> planFiscalStatusCodes,
            List<String> forestRegionOrgUnitIds,
            List<String> forestDistrictOrgUnitIds,
            List<String> fireCentreOrgUnitIds,
            List<String> projectTypeCodes,
            String searchText) throws ServiceException {

        if (explicitGuids != null && !explicitGuids.isEmpty()) {
            return explicitGuids;
        }

        FeatureQueryParams queryParams = new FeatureQueryParams();
        queryParams.setProgramAreaGuids(programAreaGuids);
        queryParams.setFiscalYears(fiscalYears);
        queryParams.setActivityCategoryCodes(activityCategoryCodes);
        queryParams.setPlanFiscalStatusCodes(planFiscalStatusCodes);
        queryParams.setForestRegionOrgUnitIds(forestRegionOrgUnitIds);
        queryParams.setForestDistrictOrgUnitIds(forestDistrictOrgUnitIds);
        queryParams.setFireCentreOrgUnitIds(fireCentreOrgUnitIds);
        queryParams.setProjectTypeCodes(projectTypeCodes);
        queryParams.setSearchText(searchText);

        return projectLocationService.getProjectGuids(queryParams);
    }
}

