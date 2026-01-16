package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.models.ReportRequestModel;
import ca.bc.gov.nrs.wfprev.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
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

import java.io.IOException;

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
    public ResponseEntity<StreamingResponseBody> generateReport(@Valid @RequestBody ReportRequestModel request) throws ServiceException, IOException, InterruptedException {
        final String type = request.getReportType();
        log.info("/reports start (type={})", type);

        try {
            if ("XLSX".equalsIgnoreCase(type)) {
                byte[] bytes;
                long t0 = System.currentTimeMillis();

                log.info("exportXlsx -> begin");
                try (var baos = new java.io.ByteArrayOutputStream(1 << 20)) { // 1MB initial cap
                    reportService.exportXlsx(request, baos);
                    bytes = baos.toByteArray();
                }
                long t1 = System.currentTimeMillis();
                log.info("exportXlsx -> end ({} ms, {} bytes)", (t1 - t0), bytes.length);

                StreamingResponseBody stream = out -> {
                    log.info("stream -> write begin");
                    out.write(bytes);
                    out.flush();
                    log.info("stream -> write end");
                };

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=project-report.xlsx")
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .contentLength(bytes.length)
                        .body(stream);

            } else if ("CSV".equalsIgnoreCase(type)) {
                byte[] bytes;
                long t0 = System.currentTimeMillis();

                log.info("writeCsvZipFromEntities -> begin");
                try (var baos = new java.io.ByteArrayOutputStream(1 << 20)) {
                    reportService.writeCsvZipFromEntities(request, baos);
                    bytes = baos.toByteArray();
                }
                long t1 = System.currentTimeMillis();
                log.info("writeCsvZipFromEntities -> end ({} ms, {} bytes)", (t1 - t0), bytes.length);

                StreamingResponseBody stream = out -> {
                    log.info("stream(zip) -> write begin");
                    out.write(bytes);
                    out.flush();
                    log.info("stream(zip) -> write end");
                };

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=project-report.zip")
                        .contentType(MediaType.parseMediaType("application/zip"))
                        .contentLength(bytes.length)
                        .body(stream);

            } else {
                log.warn("Bad report type: {}", type);
                return ResponseEntity.badRequest()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(out -> out.write("Only reportType=XLSX or CSV is supported.".getBytes()));
            }
        }catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw ie;
        }catch (ServiceException | IOException e) {
            throw e;
        }catch (Exception e) {
            throw new ServiceException("Project Download failed: " + e.getMessage(), e);
        }finally {
            log.info("/reports end");
        }
    }

}