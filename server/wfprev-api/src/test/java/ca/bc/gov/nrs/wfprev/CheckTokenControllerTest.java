package ca.bc.gov.nrs.wfprev;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.utility.TestcontainersConfiguration;

import ca.bc.gov.nrs.wfprev.common.controllers.CheckTokenController;

@WebMvcTest(CheckTokenController.class)
@Import({SecurityConfig.class, TestcontainersConfiguration.class})
class CheckTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
