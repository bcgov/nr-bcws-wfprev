package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.controllers.ReportController;
import ca.bc.gov.nrs.wfprev.data.models.ReportRequestModel;
import ca.bc.gov.nrs.wfprev.services.ReportService;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class ReportControllerTest {

   @MockBean
   private ReportService reportService;

   @Autowired
   private MockMvc mockMvc;

   @MockBean(name = "springSecurityAuditorAware")
   private AuditorAware<String> auditorAware;

   private Gson gson;

   @BeforeEach
   void setup() {
       gson = new GsonBuilder()
               .serializeNulls()
               .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
               .serializeSpecialFloatingPointValues()
               .create();
   }

    @Test
    @WithMockUser
    void testGenerateXlsxReport() throws Exception {
        UUID guid = UUID.randomUUID();
        
        doAnswer(inv -> {
            OutputStream os = inv.getArgument(1);
            os.write("test-xlsx".getBytes(StandardCharsets.UTF_8)); // simulate XLSX bytes
            return null;
        }).when(reportService).exportXlsx(any(ReportRequestModel.class), any(OutputStream.class), anyString());
        
        ReportRequestModel.Project p = new ReportRequestModel.Project();
        p.setProjectGuid(guid);
        p.setProjectFiscalGuids(List.of()); 

        ReportRequestModel request = new ReportRequestModel();
        request.setReportType("XLSX");
        request.setProjects(List.of(p));

        String json = gson.toJson(request);

        mockMvc.perform(post("/reports")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=project-report.xlsx"))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        verify(reportService, times(1))
                .exportXlsx(any(ReportRequestModel.class), any(OutputStream.class), anyString());
    }

    @Test
    @WithMockUser
    void testGenerateCsvReport() throws Exception {
        UUID guid = UUID.randomUUID();
        
        doAnswer(inv -> {
            OutputStream os = inv.getArgument(1);
            os.write("test-zip".getBytes(StandardCharsets.UTF_8)); 
            return null;
        }).when(reportService).writeCsvZipFromEntities(any(ReportRequestModel.class), any(OutputStream.class));

        ReportRequestModel.Project p = new ReportRequestModel.Project();
        p.setProjectGuid(guid);
        p.setProjectFiscalGuids(List.of());

        ReportRequestModel request = new ReportRequestModel();
        request.setReportType("CSV");
        request.setProjects(List.of(p));

        String json = gson.toJson(request);

        mockMvc.perform(post("/reports")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=project-report.zip"))
                .andExpect(content().contentType("application/zip"));

        verify(reportService, times(1))
                .writeCsvZipFromEntities(any(ReportRequestModel.class), any(OutputStream.class));
    }

    @Test
    @WithMockUser
    void testGenerateReport_InvalidType() throws Exception {
        ReportRequestModel request = new ReportRequestModel();
        request.setReportType("TXT");

        String json = gson.toJson(request);

        ResultActions result = mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(400, result.andReturn().getResponse().getStatus());
        verifyNoInteractions(reportService);
    }
}
