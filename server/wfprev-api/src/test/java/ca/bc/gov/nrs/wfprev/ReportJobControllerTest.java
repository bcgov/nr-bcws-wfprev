package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.controllers.ReportJobController;
import ca.bc.gov.nrs.wfprev.data.models.ReportJobDownloadUrlModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportJobModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportJobRequestModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportType;
import ca.bc.gov.nrs.wfprev.services.reportjobs.ReportJobService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportJobController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class ReportJobControllerTest {

    @MockBean
    private ReportJobService reportJobService;

    @MockBean(name = "springSecurityAuditorAware")
    private AuditorAware<String> auditorAware;

    @Autowired
    private MockMvc mockMvc;

    private static ReportJobModel job(UUID guid, String status) {
        return ReportJobModel.builder()
                .jobGuid(guid)
                .exportGroupGuid(UUID.randomUUID())
                .reportType(ReportType.RESULTS_XLSX)
                .fileName("ReMi_RESULTS.xlsx")
                .description("Coastal Fire Centre · FY 2026/27")
                .status(status)
                .reference(guid.toString().substring(0, 8).toUpperCase())
                .requestTimestamp("2026-09-24T21:14:03Z")
                .build();
    }

    @Test
    @WithMockUser(username = "IDIR\\PLANNER")
    void create_returns202AndPassesTheCaller() throws Exception {
        UUID guid = UUID.randomUUID();
        when(reportJobService.create(any(ReportJobRequestModel.class), eq("IDIR\\PLANNER")))
                .thenReturn(job(guid, "PREPARING"));

        mockMvc.perform(post("/reports/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reportType":"RESULTS_XLSX","description":"Coastal Fire Centre · FY 2026/27",
                                 "projectFilter":{"fireCentreOrgUnitIds":["3"]}}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobGuid").value(guid.toString()))
                .andExpect(jsonPath("$.status").value("PREPARING"))
                .andExpect(jsonPath("$.requestTimestamp").value("2026-09-24T21:14:03Z"));

        ArgumentCaptor<ReportJobRequestModel> captor = ArgumentCaptor.forClass(ReportJobRequestModel.class);
        verify(reportJobService).create(captor.capture(), eq("IDIR\\PLANNER"));
        assertEquals(ReportType.RESULTS_XLSX, captor.getValue().getReportType());
        assertEquals(List.of("3"), captor.getValue().getProjectFilter().getFireCentreOrgUnitIds());
    }

    @Test
    @WithMockUser
    void create_withoutReportType_returns400() throws Exception {
        mockMvc.perform(post("/reports/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectFilter\":{}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void create_withoutFilterOrProjects_returns400() throws Exception {
        when(reportJobService.create(any(), any()))
                .thenThrow(new IllegalArgumentException("At least one project or a filter is required"));

        mockMvc.perform(post("/reports/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportType\":\"PROJECT_CSV\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "IDIR\\PLANNER")
    void list_returnsTheCallersJobs() throws Exception {
        UUID guid = UUID.randomUUID();
        when(reportJobService.list("IDIR\\PLANNER")).thenReturn(List.of(job(guid, "READY")));

        mockMvc.perform(get("/reports/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobGuid").value(guid.toString()))
                .andExpect(jsonPath("$[0].status").value("READY"))
                .andExpect(jsonPath("$[0].fileName").value("ReMi_RESULTS.xlsx"));
    }

    @Test
    @WithMockUser(username = "IDIR\\PLANNER")
    void downloadUrl_returnsTheLink() throws Exception {
        UUID guid = UUID.randomUUID();
        when(reportJobService.downloadUrl(guid, "IDIR\\PLANNER"))
                .thenReturn(new ReportJobDownloadUrlModel("https://s3.example/jobs/x?sig=1", "ReMi_RESULTS.xlsx"));

        mockMvc.perform(post("/reports/jobs/{guid}/download-url", guid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://s3.example/jobs/x?sig=1"))
                .andExpect(jsonPath("$.fileName").value("ReMi_RESULTS.xlsx"));
    }

    @Test
    @WithMockUser
    void downloadUrl_forSomeoneElsesJob_returns404() throws Exception {
        UUID guid = UUID.randomUUID();
        when(reportJobService.downloadUrl(eq(guid), any())).thenThrow(new EntityNotFoundException("Export job not found"));

        mockMvc.perform(post("/reports/jobs/{guid}/download-url", guid))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void downloadUrl_forAnExpiredFile_returns409() throws Exception {
        UUID guid = UUID.randomUUID();
        when(reportJobService.downloadUrl(eq(guid), any()))
                .thenThrow(new IllegalStateException("This file is no longer available"));

        mockMvc.perform(post("/reports/jobs/{guid}/download-url", guid))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "IDIR\\PLANNER")
    void retry_returns202WithTheNewJob() throws Exception {
        UUID old = UUID.randomUUID();
        UUID fresh = UUID.randomUUID();
        when(reportJobService.retry(old, "IDIR\\PLANNER")).thenReturn(job(fresh, "PREPARING"));

        mockMvc.perform(post("/reports/jobs/{guid}/retry", old))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobGuid").value(fresh.toString()));
    }

    @Test
    @WithMockUser(username = "IDIR\\PLANNER")
    void clear_returns204() throws Exception {
        mockMvc.perform(post("/reports/jobs/clear"))
                .andExpect(status().isNoContent());

        verify(reportJobService).clearFinished("IDIR\\PLANNER");
    }

    @Test
    void list_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/reports/jobs"))
                .andExpect(status().isUnauthorized());
    }
}
