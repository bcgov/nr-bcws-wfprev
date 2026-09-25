package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.wfone.common.webade.authentication.WebAdeAuthentication;
import ca.bc.gov.nrs.wfprev.data.models.ReportJobDownloadUrlModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportJobModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportJobRequestModel;
import ca.bc.gov.nrs.wfprev.services.reportjobs.ReportJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Report exports as background jobs: start one per file, poll the list, then fetch a short-lived
 * download link once a file is ready. Jobs belong to the user who started them.
 */
@RestController
@RequestMapping("/reports/jobs")
@RequiredArgsConstructor
public class ReportJobController {

    private static final String EXPORT_AUTHORITY = "hasAuthority('WFPREV.EXPORT_TO_RESULTS')";

    private final ReportJobService reportJobService;

    @PostMapping
    @PreAuthorize(EXPORT_AUTHORITY)
    @Operation(
            summary = "Start a report export",
            description = "Starts generating one report file in the background. Poll GET /reports/jobs for its status.",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"}),
            extensions = @Extension(properties = {
                    @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"),
                    @ExtensionProperty(name = "throttling-tier", value = "Unlimited")
            })
    )
    public ResponseEntity<ReportJobModel> create(@Valid @RequestBody ReportJobRequestModel request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(reportJobService.create(request, currentUserId()));
    }

    @GetMapping
    @PreAuthorize(EXPORT_AUTHORITY)
    @Operation(
            summary = "List the current user's report exports",
            description = "Exports from the last 48 hours that haven't been cleared, newest first.",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"}),
            extensions = @Extension(properties = {
                    @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"),
                    @ExtensionProperty(name = "throttling-tier", value = "Unlimited")
            })
    )
    public ResponseEntity<List<ReportJobModel>> list() {
        return ResponseEntity.ok(reportJobService.list(currentUserId()));
    }

    @PostMapping("/{jobGuid}/download-url")
    @PreAuthorize(EXPORT_AUTHORITY)
    @Operation(
            summary = "Get a download link for a finished export",
            description = "Returns a link to the file that is valid for a few minutes.",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"}),
            extensions = @Extension(properties = {
                    @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"),
                    @ExtensionProperty(name = "throttling-tier", value = "Unlimited")
            })
    )
    public ResponseEntity<ReportJobDownloadUrlModel> downloadUrl(@PathVariable UUID jobGuid) {
        return ResponseEntity.ok(reportJobService.downloadUrl(jobGuid, currentUserId()));
    }

    @PostMapping("/{jobGuid}/retry")
    @PreAuthorize(EXPORT_AUTHORITY)
    @Operation(
            summary = "Run an export again",
            description = "Starts a new job from the original request of a failed or expired export.",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"}),
            extensions = @Extension(properties = {
                    @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"),
                    @ExtensionProperty(name = "throttling-tier", value = "Unlimited")
            })
    )
    public ResponseEntity<ReportJobModel> retry(@PathVariable UUID jobGuid) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(reportJobService.retry(jobGuid, currentUserId()));
    }

    @PostMapping("/clear")
    @PreAuthorize(EXPORT_AUTHORITY)
    @Operation(
            summary = "Clear finished exports",
            description = "Removes the current user's finished exports from the download list.",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"}),
            extensions = @Extension(properties = {
                    @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"),
                    @ExtensionProperty(name = "throttling-tier", value = "Unlimited")
            })
    )
    public ResponseEntity<Void> clearFinished() {
        reportJobService.clearFinished(currentUserId());
        return ResponseEntity.noContent().build();
    }

    private static String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof WebAdeAuthentication webAde && webAde.getUserId() != null) {
            return webAde.getUserId();
        }
        return authentication.getName();
    }
}
