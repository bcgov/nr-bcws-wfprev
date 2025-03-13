package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.controllers.GeoJsonFeaturesController;
import ca.bc.gov.nrs.wfprev.services.GeoJsonFeaturesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeoJsonFeaturesControllerTest {

    @Mock
    private GeoJsonFeaturesService geoJsonFeaturesService;

    @InjectMocks
    private GeoJsonFeaturesController geoJsonFeaturesController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllFeaturesGeoJson_Success() {
        // Mock response
        Map<String, Object> mockGeoJson = Collections.singletonMap("type", "FeatureCollection");
        when(geoJsonFeaturesService.getAllFeaturesGeoJson()).thenReturn(mockGeoJson);

        // Call controller method
        Map<String, Object> response = geoJsonFeaturesController.getAllFeaturesGeoJson();

        // Verify response
        assertNotNull(response);
        assertEquals("FeatureCollection", response.get("type"));

        // Verify interaction with service
        verify(geoJsonFeaturesService, times(1)).getAllFeaturesGeoJson();
    }

    @Test
    void testGetAllFeaturesGeoJson_Failure() {
        // Mock exception in service
        when(geoJsonFeaturesService.getAllFeaturesGeoJson())
                .thenThrow(new RuntimeException("Database error"));

        // Verify exception handling
        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> geoJsonFeaturesController.getAllFeaturesGeoJson()
        );

        assertTrue(thrown.getMessage().contains("Error encountered while fetching GeoJson features"));
        verify(geoJsonFeaturesService, times(1)).getAllFeaturesGeoJson();
    }
}
