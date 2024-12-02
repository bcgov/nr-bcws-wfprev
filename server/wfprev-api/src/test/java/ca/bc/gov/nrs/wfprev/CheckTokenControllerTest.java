package ca.bc.gov.nrs.wfprev;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.utility.TestcontainersConfiguration;

import ca.bc.gov.nrs.wfone.common.webade.oauth2.token.client.impl.TokenServiceImpl;
import ca.bc.gov.nrs.wfone.common.webade.oauth2.token.client.resource.CheckedToken;
import ca.bc.gov.nrs.wfprev.common.controllers.CheckTokenController;

@WebMvcTest(CheckTokenController.class)
@Import({SecurityConfig.class, TestcontainersConfiguration.class})
@TestPropertySource(locations = "classpath:application-test.properties")
class CheckTokenControllerTest {

    @MockBean
    private TokenServiceImpl tokenService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testToken_SuccessfulCheck() throws Exception {
        String testToken = "Bearer valid-token";
        CheckedToken checkedToken = new CheckedToken();

        // Mock the behavior of the token service
        when(tokenService.checkToken("admin-token")).thenReturn(checkedToken);

        mockMvc.perform(get("/check/checkToken")
                .header("Authorization", testToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testToken_MissingAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/check/checkToken")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testToken_InvalidToken() throws Exception {
        String testToken = "Bearer invalid-token";

        // Simulate the token service throwing an exception for an invalid token
        when(tokenService.checkToken("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(get("/check/checkToken")
                .header("Authorization", testToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
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