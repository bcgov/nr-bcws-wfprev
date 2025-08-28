package ca.bc.gov.nrs.wfprev;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;

import ca.bc.gov.nrs.wfprev.controllers.ReportController;
import ca.bc.gov.nrs.wfprev.data.models.ReportRequestModel;
import ca.bc.gov.nrs.wfprev.services.ReportService;

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

       doAnswer(invocation -> null).when(reportService).exportXlsx(any(), any(), anyString());

       ReportRequestModel request = new ReportRequestModel();
       request.setReportType("XLSX");
       request.setProjectGuids(List.of(guid));

       String json = gson.toJson(request);

       mockMvc.perform(post("/reports")
                       .content(json)
                       .contentType(MediaType.APPLICATION_JSON)
                       .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
               .andExpect(status().isOk());
   }

   @Test
   @WithMockUser
   void testGenerateCsvReport() throws Exception {
       UUID guid = UUID.randomUUID();

       doAnswer(invocation -> null).when(reportService).writeCsvZipFromEntities(any(), any());

       ReportRequestModel request = new ReportRequestModel();
       request.setReportType("CSV");
       request.setProjectGuids(List.of(guid));

       String json = gson.toJson(request);

       mockMvc.perform(post("/reports")
                       .content(json)
                       .contentType(MediaType.APPLICATION_JSON)
                       .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
               .andExpect(status().isOk());
   }

   @Test
   @WithMockUser
   void testGenerateReport_InvalidType() throws Exception {
       ReportRequestModel request = new ReportRequestModel();
       request.setReportType("TXT");
       request.setProjectGuids(Collections.singletonList(UUID.randomUUID()));

       String json = gson.toJson(request);

       ResultActions result = mockMvc.perform(post("/reports")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(json))
               .andExpect(status().isBadRequest());

       assertEquals(400, result.andReturn().getResponse().getStatus());
       verifyNoInteractions(reportService);
   }
}
