package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.controllers.GDBLambdaController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.ResponseEntity;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GDBLambdaControllerTest {

    private LambdaClient lambdaClient;
    private GDBLambdaController controller;

    @BeforeEach
    public void setup() {
        lambdaClient = mock(LambdaClient.class);
        controller = new GDBLambdaController(lambdaClient);
    }

    @Test
    public void testInvokeUpload_success() throws Exception {
        // Arrange
        byte[] fileContent = "test content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "test.gdb.zip", MediaType.APPLICATION_OCTET_STREAM_VALUE, fileContent);

        String expectedLambdaResponse = "{\"status\":\"ok\"}";

        InvokeResponse mockResponse = InvokeResponse.builder()
                .payload(SdkBytes.fromUtf8String(expectedLambdaResponse))
                .build();

        when(lambdaClient.invoke(any(InvokeRequest.class))).thenReturn(mockResponse);

        // Act
        ResponseEntity<String> response = controller.invokeUpload(file);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedLambdaResponse, response.getBody());

        ArgumentCaptor<InvokeRequest> requestCaptor = ArgumentCaptor.forClass(InvokeRequest.class);
        verify(lambdaClient, times(1)).invoke(requestCaptor.capture());

        String payload = requestCaptor.getValue().payload().asUtf8String();
        String base64Encoded = Base64.getEncoder().encodeToString(fileContent);
        assertEquals("{\"file\":\"" + base64Encoded + "\"}", payload);
    }

    @Test
    public void testInvokeUpload_ioExceptionDuringFileProcessing() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("Simulated IO error"));

        // Act
        ResponseEntity<String> response = controller.invokeUpload(file);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Encountered error while processing file."));

        verify(lambdaClient, never()).invoke((InvokeRequest) any()); // Lambda should not be invoked on file read failure
    }

    @Test
    public void testInvokeUpload_lambdaInvocationException() throws Exception {
        // Arrange
        byte[] fileContent = "bad content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "fail.gdb.zip", MediaType.APPLICATION_OCTET_STREAM_VALUE, fileContent);

        when(lambdaClient.invoke(any(InvokeRequest.class)))
                .thenThrow(new RuntimeException("Simulated Lambda failure"));

        // Act
        ResponseEntity<String> response = controller.invokeUpload(file);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Encountered error while invoking lambda."));
    }
}
