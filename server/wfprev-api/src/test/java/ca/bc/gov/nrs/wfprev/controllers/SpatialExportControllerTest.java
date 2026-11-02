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
import java.util.Date;
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
    void testExportChanges_DeletedProjects() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        Date lastUpdated = Date.from(Instant.parse("2026-10-30T10:15:30.000Z"));
        Date nextSince = Date.from(Instant.parse("2026-10-30T12:00:00.000Z"));

        ProjectActivityModel deletedModel = ProjectActivityModel.builder()
                .projectGuid(projectGuid)
                .lastUpdated(lastUpdated)
                .build();

        SpatialExportModel exportModel = SpatialExportModel.builder()
                .projectActivities(List.of(deletedModel))
                .since(nextSince)
                .build();

        when(spatialExportService.getSpatialExport(any(Date.class))).thenReturn(exportModel);

        mockMvc.perform(get("/spatial-export/changes")
                        .param("since", "2026-10-30T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectActivities", hasSize(1)))
                .andExpect(jsonPath("$.projectActivities[0].projectGuid").value(projectGuid.toString()))
                .andExpect(jsonPath("$.projectActivities[0].activityGuid").doesNotExist())
                .andExpect(jsonPath("$.projectActivities[0].geometry").doesNotExist())
                .andExpect(jsonPath("$.projectActivities[0].projectNumber").doesNotExist())
                .andExpect(jsonPath("$.projectActivities[0].lastUpdated").value("2026-10-30T10:15:30.000Z"))
                .andExpect(jsonPath("$.since").value("2026-10-30T12:00:00.000Z"));
    }

    @Test
    void testExportChanges_EmptyResult() throws Exception {
        Date cursor = Date.from(Instant.parse("2026-10-30T12:00:00.000Z"));

        SpatialExportModel emptyExportModel = SpatialExportModel.builder()
                .projectActivities(Collections.emptyList())
                .since(cursor)
                .build();

        when(spatialExportService.getSpatialExport(any(Date.class))).thenReturn(emptyExportModel);

        mockMvc.perform(get("/spatial-export/changes")
                        .param("since", "2026-10-30T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectActivities", hasSize(0)))
                .andExpect(jsonPath("$.since").value("2026-10-30T12:00:00.000Z"));
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
        when(spatialExportService.getSpatialExport(any(Date.class)))
                .thenThrow(new IllegalArgumentException("'since' parameter is required"));

        mockMvc.perform(get("/spatial-export/changes")
                        .param("since", "2026-10-30T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("'since' parameter is required"));
    }
}

