package ca.bc.gov.nrs.wfprev.common.controllers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.nrs.common.wfone.rest.resource.HeaderConstants;
import ca.bc.gov.nrs.common.wfone.rest.resource.MessageListRsrc;
import ca.bc.gov.nrs.wfone.common.webade.oauth2.token.client.Oauth2ClientException;
import ca.bc.gov.nrs.wfone.common.webade.oauth2.token.client.impl.TokenServiceImpl;
import ca.bc.gov.nrs.wfone.common.webade.oauth2.token.client.resource.CheckedToken;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(value = "/check")
public class CheckTokenController {

    @Value("${security.oauth.clientId}")
    private String oauthClientId;

    @Value("${security.oauth.clientSecret}")
    private String oauthClientSecret;

    @Value("${security.oauth.checkTokenUrl}")
    private String oauthCheckTokenUrl;

    @Value("${security.oauth.authTokenUrl}")
    private String authTokenUrl;

    private static final Logger logger = LoggerFactory.getLogger(CheckTokenController.class);

    @GetMapping(value = "/checkToken", headers = "Accept=*/*", produces = {"application/json", "text/xml"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollectionModel.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = MessageListRsrc.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not Found"),
        @ApiResponse(responseCode = "409", description = "Conflict"),
        @ApiResponse(responseCode = "412", description = "Precondition Failed"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = MessageListRsrc.class)))})
    @Parameter(name = HeaderConstants.VERSION_HEADER, description = HeaderConstants.VERSION_HEADER_DESCRIPTION, required = false, schema = @Schema(implementation = Integer.class), in = ParameterIn.HEADER)
    @Parameter(name = HeaderConstants.IF_MATCH_HEADER, description = HeaderConstants.IF_MATCH_DESCRIPTION, required = true, schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
    public CheckedToken token(HttpServletRequest request, HttpServletResponse response) throws Oauth2ClientException, IOException {
        logger.debug("<checkToken");

        TokenServiceImpl tokenService;

        tokenService = new TokenServiceImpl(
                oauthClientId,
                oauthClientSecret,
                oauthCheckTokenUrl,
                authTokenUrl);

        CheckedToken result = null;
        String authorizationHeader = request.getHeader("Authorization");
        request.getSession().setAttribute("authToken", authorizationHeader);
        try {
            if (authorizationHeader == null) {
                response.sendError(401);
            } else {
                result = tokenService.checkToken(authorizationHeader.replace("Bearer ", ""));
            }
        } catch (Oauth2ClientException | IOException t) {
            response.sendError(500, "Authentication request was unable to be processed, please try again later.");
            log.error(" ### Error while checking for valid authorization token", t);
        }

        logger.debug(">checkToken");
        return result;
    }
}
