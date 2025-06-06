package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.common.exceptions.ServiceException;
import ca.bc.gov.nrs.wfprev.controllers.FeaturesController;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import ca.bc.gov.nrs.wfprev.services.FeaturesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeaturesControllerTest {

    private FeaturesService featuresService;
    private FeaturesController featuresController;

    @BeforeEach
    void setUp() {
        featuresService = mock(FeaturesService.class);
        featuresController = new FeaturesController(featuresService);
    }

    @Test
    void testGetAllFeatures_WithQueryParams_ReturnsExpectedMap() throws ServiceException {
        // Prepare input parameters
        List<UUID> programAreaGuids = List.of(UUID.randomUUID());
        List<String> fiscalYears = List.of("2024");
        List<String> activityCategoryCodes = List.of("AC1");
        List<String> planFiscalStatusCodes = List.of("PFS1");
        List<String> forestRegionOrgUnitIds = List.of("FR1");
        List<String> forestDistrictOrgUnitIds = List.of("FD1");
        List<String> fireCentreOrgUnitIds = List.of("FC1");
        String searchText = "some text";

        // Expected result from service
        Map<String, Object> expectedResponse = Map.of("type", "FeatureCollection");

        // Mock service call
        when(featuresService.getAllFeatures(any(FeatureQueryParams.class))).thenReturn(expectedResponse);

        // Call controller method
        ResponseEntity<Map<String, Object>> result = featuresController.getAllFeatures(
                programAreaGuids,
                fiscalYears,
                activityCategoryCodes,
                planFiscalStatusCodes,
                forestRegionOrgUnitIds,
                forestDistrictOrgUnitIds,
                fireCentreOrgUnitIds,
                searchText
        );

        // Assert status and body
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(featuresService, times(1)).getAllFeatures(any(FeatureQueryParams.class));
    }

    @Test
    void testGetAllFeatures_WithNoParams_CallsServiceWithEmptyParams() throws ServiceException {
        // Expected result
        Map<String, Object> expected = Map.of("type", "FeatureCollection");

        when(featuresService.getAllFeatures(any(FeatureQueryParams.class))).thenReturn(expected);

        ResponseEntity<Map<String, Object>> result = featuresController.getAllFeatures(
                null, null, null, null, null, null, null, null
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expected, result.getBody());
        verify(featuresService, times(1)).getAllFeatures(any(FeatureQueryParams.class));
    }

    @Test
    void testGetAllFeatures_ServiceThrowsException_ReturnsInternalServerError() throws ServiceException {
        // Mock exception
        when(featuresService.getAllFeatures(any(FeatureQueryParams.class))).thenThrow(new ServiceException("Service failed"));

        ResponseEntity<Map<String, Object>> result = featuresController.getAllFeatures(
                null, null, null, null, null, null, null, null
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }
}
