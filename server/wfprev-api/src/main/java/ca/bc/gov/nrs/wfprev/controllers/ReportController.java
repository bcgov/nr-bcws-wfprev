package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfprev.common.exceptions.ServiceException;
import ca.bc.gov.nrs.wfprev.data.models.ReportRequestModel;
import ca.bc.gov.nrs.wfprev.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.rmi.server.ServerCloneException;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(
            summary = "Generate Fuel Management Report",
            description = "Generate and download a fuel management report as an XLSX or CSV file",
            security = @SecurityRequirement(name = "Webade-OAUTH2", scopes = {"WFPREV"}),
            extensions = @Extension(properties = {
                    @ExtensionProperty(name = "auth-type", value = "#{wso2.x-auth-type.app_and_app_user}"),
                    @ExtensionProperty(name = "throttling-tier", value = "Unlimited")
            })
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid report request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))
    })
    public ResponseEntity<StreamingResponseBody> generateReport(@Valid @RequestBody ReportRequestModel request) {
        String type = request.getReportType();
        log.debug(" >> generateReport with type: {}", type);

        if ("XLSX".equalsIgnoreCase(type)) {
            StreamingResponseBody stream = outputStream -> {
                try {
                    reportService.exportXlsx(request.getProjectGuids(), outputStream);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to stream XLSX report", e);
                }
            };

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=project-report.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(stream);

        } else if ("CSV".equalsIgnoreCase(type)) {
            StreamingResponseBody stream = outputStream -> {
                try {
                    reportService.writeCsvZipFromEntities(request.getProjectGuids(), outputStream);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to stream CSV report", e);
                }
            };

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=project-report.zip")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(stream);

        } else {
            log.warn("Unsupported report type requested: {}", type);
            return ResponseEntity.badRequest()
                    .body(out -> out.write("Only reportType=XLSX or CSV is supported.".getBytes()));
        }
    }
}
