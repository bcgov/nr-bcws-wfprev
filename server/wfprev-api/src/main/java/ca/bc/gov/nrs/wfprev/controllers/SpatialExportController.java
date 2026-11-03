package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.wfprev.data.models.SpatialExportModel;
import ca.bc.gov.nrs.wfprev.services.SpatialExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.beans.PropertyEditorSupport;
import java.time.Instant;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/spatial-export")
public class SpatialExportController {

    private final SpatialExportService spatialExportService;

    public SpatialExportController(SpatialExportService spatialExportService) {
        this.spatialExportService = spatialExportService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Instant.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.trim().isEmpty()) {
                    setValue(null);
                } else {
                    try {
                        setValue(Instant.parse(text.trim()));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid ISO-8601 UTC date: " + text, e);
                    }
                }
            }

            @Override
            public String getAsText() {
                Instant value = (Instant) getValue();
                return value != null ? value.toString() : "";
            }
        });
    }

    @GetMapping("/changes")
    @PreAuthorize("hasAuthority('WFPREV.GET_SPATIAL_EXPORT')")
    @Operation(
            summary = "Export Changed Spatial Boundaries",
            description = "Reads changed project activities since the specified timestamp and returns a model of ProjectActivityModel with geometry as GeoJSON in EPSG:4326 and a timestamp for the next cursor.",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV", "WFPREV.GET_SPATIAL_EXPORT"}),
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"),
                            @ExtensionProperty(name = "throttling-tier", value = "Unlimited")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = SpatialExportModel.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request (e.g. invalid 'since' timestamp)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<SpatialExportModel> exportChanges(
            @Parameter(description = "Timestamp cursor (ISO-8601 UTC) indicating changes since this time", required = true)
            @RequestParam(name = "since", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since,
            @Parameter(description = "Project GUID cursor indicating changes after this project ID when timestamp is identical", required = false)
            @RequestParam(name = "afterProjectGuid", required = false) UUID afterProjectGuid
    ) {
        log.info(" >> export changes since={}, afterProjectGuid={}", since, afterProjectGuid);

        SpatialExportModel response = spatialExportService.getSpatialExport(since, afterProjectGuid);
        return ResponseEntity.ok(response);
    }
}

