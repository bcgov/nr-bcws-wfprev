package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfprev.common.controllers.CommonController;
import ca.bc.gov.nrs.wfprev.services.BoundaryTileService;
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
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@Validated
@RequestMapping("/tiles")
public class BoundaryTileController extends CommonController {

    private final BoundaryTileService boundaryTileService;

    public BoundaryTileController(BoundaryTileService boundaryTileService) {
        super(BoundaryTileController.class.getName());
        this.boundaryTileService = boundaryTileService;
    }

    @GetMapping(
            value = "/project_boundary/{z}/{x}/{y}.mvt",
            produces = "application/vnd.mapbox-vector-tile")
    @Operation(
            summary = "Fetch all File Attachments for an Activity",
            description = "Fetch all File Attachments for an Activity",
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
            @RequestParam(name = "projectGuid", required = true) @NotEmpty List<UUID> projectGuids) {
        log.info(" >> getProjectBoundaryTiles");
        if (projectGuids == null || projectGuids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "projectGuid is required (at least one). Pass multiple like ?projectGuid=a&projectGuid=b");
        }

        try {
            byte[] mvt = boundaryTileService.getProjectBoundaryTile(z, x, y, projectGuids);
            if (mvt == null) mvt = new byte[0];

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, immutable")
                    .body(mvt);
        } catch (RuntimeException e) {
            log.info(" ### Error while fetching File Attachments for Activity", e);
            return internalServerError();
        }
    }

    @GetMapping(
            value = "/activity_boundary/{z}/{x}/{y}.mvt",
            produces = "application/vnd.mapbox-vector-tile"
    )
    @Operation(
            summary = "Fetch activity boundaries for one or more projects",
            description = "Returns activity boundaries styled by fiscal year",
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
            @RequestParam(name = "projectGuid", required = true) @NotEmpty List<UUID> projectGuids) {
        log.info(">>getActivityBoundaryTiles");
        if (projectGuids == null || projectGuids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "projectGuid is required (at least one). Pass multiple like ?projectGuid=a&projectGuid=b");
        }

        try {
            byte[] mvt = boundaryTileService.getActivityBoundaryTile(z, x, y, projectGuids);
            if (mvt == null) mvt = new byte[0];

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, immutable")
                    .body(mvt);
        } catch (RuntimeException e) {
            log.error("### Error while fetching Activity Boundaries", e);
            return internalServerError();
        }
    }
}
