package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.wfprev.data.models.ValidationResult;
import ca.bc.gov.nrs.wfprev.handlers.GlobalExceptionHandler;
import ca.bc.gov.nrs.wfprev.services.SpatialValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Geometry;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SpatialControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SpatialValidationService spatialValidationService;

    @InjectMocks
    private SpatialController spatialController;

    @BeforeEach
    void setup() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JtsModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);

        mockMvc = MockMvcBuilders.standaloneSetup(spatialController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .alwaysDo(print())
                .build();
    }

    @Test
    void testValidateGeometry_Valid() throws Exception {
        // Mock valid result
        ValidationResult validResult = ValidationResult.builder()
                .valid(true)
                .message("Geometry is valid")
                .build();

        when(spatialValidationService.validateGeometry(any(Geometry.class))).thenReturn(validResult);

        // GeoJSON for a simple point
        String geoJson = "{ \"type\": \"Point\", \"coordinates\": [125.6, 10.1] }";

        mockMvc.perform(post("/spatial/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(geoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.message").value("Geometry is valid"));
    }

    @Test
    void testValidateGeometry_Invalid() throws Exception {
        // Mock invalid result
        ValidationResult invalidResult = ValidationResult.builder()
                .valid(false)
                .message("Self-intersection")
                .build();

        when(spatialValidationService.validateGeometry(any(Geometry.class))).thenReturn(invalidResult);

        // GeoJSON for a simple point
        String geoJson = "{ \"type\": \"Point\", \"coordinates\": [125.6, 10.1] }";

        mockMvc.perform(post("/spatial/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(geoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Self-intersection"));
    }

    @Test
    void testValidateGeometry_BadRequest() throws Exception {
        // Invalid JSON
        String badJson = "{ \"type\": \"Point\", \"coordinates\": "; // malformed

        mockMvc.perform(post("/spatial/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }
}
