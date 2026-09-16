package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.entities.CulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FuelManagementReportEntity;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XlsxReportGeneratorTest {

    private XlsxReportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new XlsxReportGenerator();
    }

    @Test
    void generateXlsx_missingLambdaUrl_throwsServiceException() {
        generator.setReportGeneratorLambdaUrl(null);
        ServiceException ex1 = assertThrows(
                ServiceException.class,
                () -> generator.generateXlsx(Collections.emptyList(), Collections.emptyList(), new ByteArrayOutputStream())
        );
        assertTrue(ex1.getMessage().contains("REPORT_GENERATOR_LAMBDA_URL"));

        generator.setReportGeneratorLambdaUrl("   ");
        ServiceException ex2 = assertThrows(
                ServiceException.class,
                () -> generator.generateXlsx(Collections.emptyList(), Collections.emptyList(), new ByteArrayOutputStream())
        );
        assertTrue(ex2.getMessage().contains("REPORT_GENERATOR_LAMBDA_URL"));
    }

    @Test
    void generateXlsx_success_writesDecodedBytes() throws Exception {
        byte[] expectedBytes = "mock-excel-binary-data".getBytes(StandardCharsets.UTF_8);
        String base64 = Base64.getEncoder().encodeToString(expectedBytes);
        String jsonPayload = "{\"files\":[{\"filename\":\"report.xlsx\",\"content\":\"" + base64 + "\"}]}";

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/lambda", (HttpExchange exchange) -> {
            byte[] response = jsonPayload.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        server.start();

        try {
            String url = "http://localhost:" + server.getAddress().getPort() + "/lambda";
            generator.setReportGeneratorLambdaUrl(url);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            generator.generateXlsx(List.of(new FuelManagementReportEntity()), List.of(new CulturalPrescribedFireReportEntity()), out);

            assertArrayEquals(expectedBytes, out.toByteArray());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void generateXlsx_lambdaNon200_throwsServiceException() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/lambda", (HttpExchange exchange) -> {
            byte[] response = "Internal error".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        server.start();

        try {
            String url = "http://localhost:" + server.getAddress().getPort() + "/lambda";
            generator.setReportGeneratorLambdaUrl(url);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ServiceException ex = assertThrows(
                    ServiceException.class,
                    () -> generator.generateXlsx(Collections.emptyList(), Collections.emptyList(), out)
            );
            assertTrue(ex.getMessage().contains("Lambda returned error"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void generateXlsx_lambdaNoFiles_throwsServiceException() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/lambda", (HttpExchange exchange) -> {
            byte[] response = "{\"files\":[]}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        server.start();

        try {
            String url = "http://localhost:" + server.getAddress().getPort() + "/lambda";
            generator.setReportGeneratorLambdaUrl(url);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ServiceException ex = assertThrows(
                    ServiceException.class,
                    () -> generator.generateXlsx(Collections.emptyList(), Collections.emptyList(), out)
            );
            assertTrue(ex.getMessage().contains("No files returned"));
        } finally {
            server.stop(0);
        }
    }
}
