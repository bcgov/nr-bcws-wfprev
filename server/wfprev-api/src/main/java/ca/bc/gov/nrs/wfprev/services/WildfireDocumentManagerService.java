package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.webade.authentication.WebAdeAuthentication;
import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class WildfireDocumentManagerService {

    private final RestClient restClient;
    private final String wfdmRestUrl;

    public WildfireDocumentManagerService(@Value("${wfdm.restUrl}") String wfdmRestUrl, RestClient.Builder restClientBuilder) {
        this.wfdmRestUrl = wfdmRestUrl;
        this.restClient = restClientBuilder.build();
    }

    public void deleteDocument(String fileIdentifier) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = "";
        
        if (authentication instanceof WebAdeAuthentication webAdeAuthentication) {
            token = (String) webAdeAuthentication.getCredentials();
        } else if (authentication != null && authentication.getCredentials() != null) {
            token = authentication.getCredentials().toString();
        } else {
             log.warn("No authentication found in security context when deleting document {}", fileIdentifier);
        }

        try {
            restClient.delete()
                    .uri(wfdmRestUrl + "/documents/" + fileIdentifier)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Successfully deleted document {} from WFDM", fileIdentifier);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error deleting document {} from WFDM: {} - {}", fileIdentifier, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceException("Failed to delete document from WFDM", e);
        } catch (Exception e) {
             log.error("Unexpected error deleting document {} from WFDM", fileIdentifier, e);
             throw new ServiceException("Failed to delete document from WFDM", e);
        }
    }
}
