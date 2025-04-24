package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping(value = "/gdb")
public class GDBLambdaController {

    @Value("${spring.lambda.gdbExtractorFunctionName}")
    private String lambdaFunctionName;

    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;

    public GDBLambdaController() {
        this.lambdaClient = LambdaClient.builder()
                .region(Region.of("ca-central-1"))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public GDBLambdaController(LambdaClient lambdaClient) {
        this.lambdaClient = lambdaClient;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/extract")
    @Operation(
            summary = "Extract coordinates from File Geodatabase",
            description = "Extract coordinates from File Geodatabase",
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
    public ResponseEntity<String> invokeUpload(@RequestParam("file") MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes();
            String base64Encoded = Base64.getEncoder().encodeToString(fileBytes);

            String jsonPayload = objectMapper.writeValueAsString(Map.of("file", base64Encoded));

            InvokeRequest request = InvokeRequest.builder()
                    .functionName(lambdaFunctionName)
                    .payload(SdkBytes.fromUtf8String(jsonPayload))
                    .build();

            InvokeResponse response = lambdaClient.invoke(request);
            String responseBody = response.payload().asUtf8String();

            return ResponseEntity.ok(responseBody);

        } catch (IOException e) {
            log.error("IO error during file processing", e);
            return ResponseEntity.status(500).body("Encountered error while processing file.");
        } catch (Exception e) {
            log.error("Error invoking Lambda function", e);
            return ResponseEntity.status(500).body("Encountered error while invoking lambda.");
        }
    }
}