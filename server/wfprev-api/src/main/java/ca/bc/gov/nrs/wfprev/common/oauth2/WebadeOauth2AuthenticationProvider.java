package ca.bc.gov.nrs.wfprev.common.oauth2;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.nrs.wfone.common.webade.oauth2.authentication.WebAdeOAuth2Authentication;
import ca.bc.gov.nrs.wfone.common.webade.oauth2.token.client.Oauth2ClientException;
import ca.bc.gov.nrs.wfone.common.webade.oauth2.token.client.TokenService;
import ca.bc.gov.nrs.wfone.common.webade.oauth2.token.client.resource.AccessToken;
import ca.bc.gov.nrs.wfone.common.webade.oauth2.token.client.resource.CheckedToken;

/**
 * Updated WebadeOauth2 filter to use latest OAuth2 classes from spring boot/security
 * You must use the common 1.5 which includes conversion to Jakarta libraries
 */

public class WebadeOauth2AuthenticationProvider implements AuthenticationProvider{
	private static final Logger logger = LoggerFactory.getLogger(WebadeOauth2AuthenticationProvider.class);
		private static ObjectMapper objectMapper = new ObjectMapper();

	private TokenService tokenService;
	private String scope;
	
	public WebadeOauth2AuthenticationProvider(TokenService tokenService, String scope) {
		logger.debug("<WebadeOauth2AuthenticationProvider");

		this.tokenService = tokenService;
		this.scope = scope;
		
		logger.debug(">WebadeOauth2AuthenticationProvider");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		logger.debug("<supports "+authentication);
		boolean result = false;
		
		if(UsernamePasswordAuthenticationToken.class.equals(authentication)) {
			
			result = true;
		} else if(BearerTokenAuthenticationToken.class.equals(authentication)) {
			
			result = true;
		}
		
		logger.debug(">supports "+result);
		return result;
	}
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		logger.debug("<authenticate "+authentication.getClass());
		Authentication result = null;
		
		if(authentication instanceof UsernamePasswordAuthenticationToken) {
			
			result = authenticateBasicCredentials((UsernamePasswordAuthenticationToken) authentication);
		} else if(authentication instanceof BearerTokenAuthenticationToken) {
			
			result = authenticateBearerToken((BearerTokenAuthenticationToken) authentication);
		}
		
		logger.debug(">authenticate "+result);
		return result;
	}

	private Authentication authenticateBearerToken(BearerTokenAuthenticationToken authentication) {
		logger.debug("<authenticateBearerToken "+authentication.getClass());
		Authentication result = null;
		
		result = authenticateBearerToken(authentication.getToken());
		
		logger.debug(">authenticateBearerToken "+result);
		return result;
	}

	private Authentication authenticateBasicCredentials(UsernamePasswordAuthenticationToken authentication) {
		logger.debug("<authenticateBasicCredentials");
		Authentication result = null;
		
		if(authentication.getPrincipal()!=null&&authentication.getCredentials()!=null) {
			
			String webadeOauth2ClientId = authentication.getPrincipal().toString();
			String webadeOauth2ClientSecret = authentication.getCredentials().toString();
			
			String token;
			try {
				AccessToken accessToken = tokenService.getToken(webadeOauth2ClientId, webadeOauth2ClientSecret, scope);
				
				token = accessToken.getAccessToken();
			} catch (Oauth2ClientException e) {
				int responseCode = e.getHttpResponseCode();
				logger.debug("responseCode="+responseCode);
				if(responseCode>=500) {
					
					throw new AuthenticationServiceException("Failed to get token.", e);
				}
				
				throw new UsernameNotFoundException("Failed to authenticate user", e);
			}
			
			result = authenticateBearerToken(token);
		}
		
		logger.debug(">authenticateBasicCredentials "+result);
		return result;
	}
	
	private Authentication authenticateBearerToken(String token) {
		logger.debug("<authenticateBasicCredentials");
		Authentication result = null;
		
		CheckedToken checkedToken;
		try {
			checkedToken = this.tokenService.checkToken(token);
		} catch (Oauth2ClientException e) {
			throw new AuthenticationServiceException("Failed to check token", e);
		}
		
		try {
			result = convertCheckedToken(token, checkedToken);
		} catch (IOException e) {
			throw new AuthenticationServiceException("Failed to parse checked token", e);
		}
		
		logger.debug(">authenticateBasicCredentials "+result);
		return result;
	}

	private static AbstractAuthenticationToken convertCheckedToken(String token, CheckedToken checkedToken) throws IOException {
		logger.debug("<convertCheckedToken");
		AbstractAuthenticationToken result;
		
		OAuth2AuthenticatedPrincipal principal = getPrincipal(checkedToken);
		
		Instant iat = principal.getAttribute(ISSUED_AT);
		Instant exp = principal.getAttribute(EXPIRES_AT);
		OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
				token, iat, exp);
		
		String clientAppCode = checkedToken.getClientAppCode();
		String clientId = checkedToken.getClientId();
		String userId = checkedToken.getUserId();
		String userType = checkedToken.getUserType();
		String userGuid = checkedToken.getUserGuid();
		String givenName = checkedToken.getGivenName();
		String familyName = checkedToken.getFamilyName();
		Long onBehalfOfOrganizationId = checkedToken.getOnBehalfOfOrganizationId();
		String onBehalfOfOrganizationCode = checkedToken.getOnBehalfOfOrganizationCode();
		String onBehalfOfOrganizationName = checkedToken.getOnBehalfOfOrganizationName();
		Map<String, String> onBehalfOfOrganizationAdditionalInfo = checkedToken.getOnBehalfOfOrganizationAdditionalInfo();
		String businessNumber = checkedToken.getBusinessNumber();
		String businessGuid = checkedToken.getBusinessGuid();
		
		result = new WebAdeOAuth2Authentication(
				principal,
				accessToken, 
				principal.getAuthorities(), 
				clientAppCode, 
				clientId, 
				userId, 
				userType, 
				userGuid, 
				givenName, 
				familyName, 
				onBehalfOfOrganizationId, 
				onBehalfOfOrganizationCode, 
				onBehalfOfOrganizationName, 
				onBehalfOfOrganizationAdditionalInfo, 
				businessNumber, 
				businessGuid);

		logger.debug(">convertCheckedToken "+result);
		return result;
	}
	
	private static final String SCOPE = "scope";
	private static final String ISSUED_AT = "iat";
	private static final String EXPIRES_AT = "exp";

	private static OAuth2AuthenticatedPrincipal getPrincipal(CheckedToken checkedToken) throws IOException {
		logger.debug("<getPrincipal "+checkedToken);
		OAuth2AuthenticatedPrincipal result;
		
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		
		byte[] bytes = objectMapper.writeValueAsBytes(checkedToken);
		
		TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {
			// do nothing
		};
		
		Map<String, Object> claims = objectMapper.readValue(bytes, typeRef);
		
		if(checkedToken.getIat()!=null) {
			
			Long iat = checkedToken.getIat();
			Instant value = Instant.ofEpochMilli(iat.longValue());
			
			claims.put(ISSUED_AT, value);
		}
		
		if(checkedToken.getExp()!=null) {
			
			Long iat = checkedToken.getExp();
			Instant value = Instant.ofEpochMilli(iat.longValue());
			
			claims.put(EXPIRES_AT, value);
		}
		
		if (checkedToken.getScope() != null) {
			List<String> scopes = Collections.unmodifiableList(Arrays.asList(checkedToken.getScope()));
			claims.put(SCOPE, scopes);

			for (String scope : scopes) {
				authorities.add(new SimpleGrantedAuthority(scope));
			}
		}

		result = new DefaultOAuth2AuthenticatedPrincipal(claims, authorities);

		logger.debug(">getPrincipal "+result);
		return result;
	}

}
