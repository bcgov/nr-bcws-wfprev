package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.wfprev.data.models.ProjectActivityModel;
import ca.bc.gov.nrs.wfprev.data.models.SpatialExportModel;
import ca.bc.gov.nrs.wfprev.handlers.GlobalExceptionHandler;
import ca.bc.gov.nrs.wfprev.services.SpatialExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SpatialExportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SpatialExportService spatialExportService;

    @InjectMocks
    private SpatialExportController spatialExportController;

    @BeforeEach
    void setup() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JtsModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);

        mockMvc = MockMvcBuilders.standaloneSetup(spatialExportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .alwaysDo(print())
                .build();
    }

    @Test
    void testExportChanges_PreservesMicrosecondsInput() throws Exception {
        Instant expectedSince = Instant.parse("2026-10-30T00:00:00.123456Z");
        Instant returnSince = Instant.parse("2026-10-30T12:00:00.987654Z");

        SpatialExportModel exportModel = SpatialExportModel.builder()
                .projectActivities(Collections.emptyList())
                .since(returnSince)
                .build();

        when(spatialExportService.getSpatialExport(org.mockito.ArgumentMatchers.eq(expectedSince), any()))
                .thenReturn(exportModel);

        mockMvc.perform(get("/spatial-export/changes")
                        .param("since", "2026-10-30T00:00:00.123456Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.since").value("2026-10-30T12:00:00.987654Z"));
    }

    @Test
    void testExportChanges_DeletedProjects() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        Instant lastUpdated = Instant.parse("2026-10-30T10:15:30.123456Z");
        Instant nextSince = Instant.parse("2026-10-30T12:00:00.654321Z");

        ProjectActivityModel deletedModel = ProjectActivityModel.builder()
                .projectGuid(projectGuid)
                .lastUpdated(lastUpdated)
                .build();

        SpatialExportModel exportModel = SpatialExportModel.builder()
                .projectActivities(List.of(deletedModel))
                .since(nextSince)
                .build();

        when(spatialExportService.getSpatialExport(any(Instant.class), any())).thenReturn(exportModel);

        mockMvc.perform(get("/spatial-export/changes")
                        .param("since", "2026-10-30T00:00:00.000000Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectActivities", hasSize(1)))
                .andExpect(jsonPath("$.projectActivities[0].projectGuid").value(projectGuid.toString()))
                .andExpect(jsonPath("$.projectActivities[0].activityGuid").doesNotExist())
                .andExpect(jsonPath("$.projectActivities[0].geometry").doesNotExist())
                .andExpect(jsonPath("$.projectActivities[0].projectNumber").doesNotExist())
                .andExpect(jsonPath("$.projectActivities[0].lastUpdated").value("2026-10-30T10:15:30.123456Z"))
                .andExpect(jsonPath("$.since").value("2026-10-30T12:00:00.654321Z"));
    }

    @Test
    void testExportChanges_EmptyResult() throws Exception {
        Instant cursor = Instant.parse("2026-10-30T12:00:00.123456Z");

        SpatialExportModel emptyExportModel = SpatialExportModel.builder()
                .projectActivities(Collections.emptyList())
                .since(cursor)
                .build();

        when(spatialExportService.getSpatialExport(any(Instant.class), any())).thenReturn(emptyExportModel);

        mockMvc.perform(get("/spatial-export/changes")
                        .param("since", "2026-10-30T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectActivities", hasSize(0)))
                .andExpect(jsonPath("$.since").value("2026-10-30T12:00:00.123456Z"));
    }

    @Test
    void testExportChanges_InvalidSince() throws Exception {
        mockMvc.perform(get("/spatial-export/changes")
                        .param("since", "not-a-valid-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter: since"));
    }

    @Test
    void testExportChanges_MissingSince() throws Exception {
        mockMvc.perform(get("/spatial-export/changes"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testExportChanges_ServiceThrowsIllegalArgumentException() throws Exception {
        when(spatialExportService.getSpatialExport(any(Instant.class), any()))
                .thenThrow(new IllegalArgumentException("'since' parameter is required"));

        mockMvc.perform(get("/spatial-export/changes")
                        .param("since", "2026-10-30T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("'since' parameter is required"));
    }

    @Test
    void testExportChanges_WithAfterProjectGuid() throws Exception {
        UUID afterProjectGuid = UUID.randomUUID();
        Instant cursor = Instant.parse("2026-10-30T12:00:00.999999Z");

        SpatialExportModel emptyExportModel = SpatialExportModel.builder()
                .projectActivities(Collections.emptyList())
                .since(cursor)
                .build();

        when(spatialExportService.getSpatialExport(any(Instant.class), org.mockito.ArgumentMatchers.eq(afterProjectGuid)))
                .thenReturn(emptyExportModel);

        mockMvc.perform(get("/spatial-export/changes")
                        .param("since", "2026-10-30T00:00:00Z")
                        .param("afterProjectGuid", afterProjectGuid.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectActivities", hasSize(0)))
                .andExpect(jsonPath("$.since").value("2026-10-30T12:00:00.999999Z"));
    }

    @Test
    void testExportChanges_ServiceThrowsDatabaseClockRuntimeException() throws Exception {
        when(spatialExportService.getSpatialExport(any(Instant.class), any()))
                .thenThrow(new RuntimeException("Could not fetch database clock"));

        mockMvc.perform(get("/spatial-export/changes")
                        .param("since", "2026-10-30T00:00:00Z"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Unexpected error occurred: Could not fetch database clock"));
    }
}

