package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.wfprev.data.models.SpatialExportModel;
import ca.bc.gov.nrs.wfprev.data.models.ValidationResult;
import ca.bc.gov.nrs.wfprev.services.SpatialExportService;
import ca.bc.gov.nrs.wfprev.services.SpatialValidationService;
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
import org.locationtech.jts.geom.Geometry;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.beans.PropertyEditorSupport;
import java.time.Instant;
import java.util.Date;

@RestController
@Slf4j
@RequestMapping("/spatial")
public class SpatialController {

    private final SpatialValidationService spatialValidationService;
    private final SpatialExportService spatialExportService;

    public SpatialController(SpatialValidationService spatialValidationService,
                             SpatialExportService spatialExportService) {
        this.spatialValidationService = spatialValidationService;
        this.spatialExportService = spatialExportService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.trim().isEmpty()) {
                    setValue(null);
                } else {
                    try {
                        setValue(Date.from(Instant.parse(text.trim())));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid ISO-8601 UTC date: " + text, e);
                    }
                }
            }
        });
    }

    @PostMapping("/validate")
    @Operation(
            summary = "Validate Spatial Geometry",
            description = "Validates the provided geometry using JTS topology checks.",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"}),
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"),
                            @ExtensionProperty(name = "throttling-tier", value = "Unlimited")
                    })
            }
    )
    @PreAuthorize("hasAuthority('WFPREV.CREATE_SPATIAL_UPLOAD')")
    public ResponseEntity<ValidationResult> validateGeometry(@RequestBody Geometry geometry) {
        ValidationResult result = spatialValidationService.validateGeometry(geometry);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('WFPREV.GET_SPATIAL_EXPORT')")
    @Operation(
            summary = "Export Changed Spatial Boundaries",
            description = "Reads changed project activities since the specified timestamp and returns a model of ProjectActivityModel with geometry as GeoJSON in EPSG:4326 and a timestamp for the next cursor.",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV", "WFPREV.EXPORT_SPATIAL"}),
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
    public ResponseEntity<SpatialExportModel> export(
            @Parameter(description = "Timestamp cursor (ISO-8601 UTC) indicating changes since this time", required = true)
            @RequestParam(name = "since", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date since
    ) {
        log.info(" >> export since={}", since);

        SpatialExportModel response = spatialExportService.getSpatialExport(since);
        return ResponseEntity.ok(response);
    }
}
