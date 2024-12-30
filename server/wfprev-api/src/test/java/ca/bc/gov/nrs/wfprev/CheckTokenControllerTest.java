package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.common.controllers.CheckTokenController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.utility.TestcontainersConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckTokenController.class)
@Import({SecurityConfig.class, TestcontainersConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class CheckTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "springSecurityAuditorAware")  // Changed to match the expected bean name
    private AuditorAware<String> auditorAware;

    @Test
    @WithMockUser
    void testToken_MissingAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/check/checkToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testToken_NullTokenInHeader() throws Exception {
        mockMvc.perform(get("/check/checkToken")
                        .header("Authorization", "Bearer ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}