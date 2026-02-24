package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.wfprev.data.models.ValidationResult;
import ca.bc.gov.nrs.wfprev.services.SpatialValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.locationtech.jts.geom.Geometry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spatial")
public class SpatialController {
    
    private final SpatialValidationService spatialValidationService;
    
    public SpatialController(SpatialValidationService spatialValidationService) {
        this.spatialValidationService = spatialValidationService;
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
    public ResponseEntity<ValidationResult> validateGeometry(@RequestBody Geometry geometry) {
        ValidationResult result = spatialValidationService.validateGeometry(geometry);
        return ResponseEntity.ok(result);
    }
}
