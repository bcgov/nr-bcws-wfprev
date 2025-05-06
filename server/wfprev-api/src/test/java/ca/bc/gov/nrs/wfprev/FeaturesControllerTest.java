package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.controllers.FeaturesController;
import ca.bc.gov.nrs.wfprev.services.FeaturesService;
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

class FeaturesControllerTest {

    @Mock
    private FeaturesService featuresService;

    @InjectMocks
    private FeaturesController featuresController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllFeatures_Success() {
        // Mock response
        Map<String, Object> mockGeoJson = Collections.singletonMap("type", "FeatureCollection");
        when(featuresService.getAllFeatures()).thenReturn(mockGeoJson);

        // Call controller method
        Map<String, Object> response = featuresController.getAllFeatures();

        // Verify response
        assertNotNull(response);
        assertEquals("FeatureCollection", response.get("type"));

        // Verify interaction with service
        verify(featuresService, times(1)).getAllFeatures();
    }

    @Test
    void testGetAllFeatures_Failure() {
        // Mock exception in service
        when(featuresService.getAllFeatures())
                .thenThrow(new RuntimeException("Database error"));

        // Verify exception handling
        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> featuresController.getAllFeatures()
        );

        assertTrue(thrown.getMessage().contains("Error encountered while fetching GeoJson features"));
        verify(featuresService, times(1)).getAllFeatures();
    }
}
