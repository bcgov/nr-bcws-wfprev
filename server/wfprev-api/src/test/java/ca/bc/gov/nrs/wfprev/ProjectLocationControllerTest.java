package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.controllers.ProjectLocationController;
import ca.bc.gov.nrs.wfprev.data.models.ProjectLocationModel;
import ca.bc.gov.nrs.wfprev.services.ProjectLocationService;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectLocationController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
class ProjectLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectLocationService projectLocationService;

    private Gson gson;

    @MockBean(name = "springSecurityAuditorAware")
    private AuditorAware<String> auditorAware;

    @BeforeEach
    void setup() {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").serializeSpecialFloatingPointValues();
        gson = builder.create();
    }

    @Test
    @WithMockUser
    void testGetAllProjectLocations_Empty() throws Exception {
        List<ProjectLocationModel> list = Collections.emptyList();
        when(projectLocationService.getAllProjectLocations())
                .thenReturn(CollectionModel.of(list));

        ResultActions result = mockMvc.perform(get("/project-locations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals("{}", result.andReturn().getResponse().getContentAsString());
        verify(projectLocationService, times(1)).getAllProjectLocations();
    }

    @Test
    @WithMockUser
    void testGetAllProjectLocations_WithContent() throws Exception {
        String g1 = UUID.randomUUID().toString();
        String g2 = UUID.randomUUID().toString();

        ProjectLocationModel m1 = ProjectLocationModel.builder()
                .projectGuid(g1)
                .latitude(BigDecimal.valueOf(49.2827))
                .longitude(BigDecimal.valueOf(-123.1207))
                .build();

        ProjectLocationModel m2 = ProjectLocationModel.builder()
                .projectGuid(g2)
                .latitude(BigDecimal.valueOf(48.4284))
                .longitude(BigDecimal.valueOf(-123.3656))
                .build();

        when(projectLocationService.getAllProjectLocations())
                .thenReturn(CollectionModel.of(Arrays.asList(m1, m2)));

        ResultActions result = mockMvc.perform(get("/project-locations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String payload = result.andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(payload)
                .contains(g1)
                .contains(g2);

        verify(projectLocationService, times(1)).getAllProjectLocations();
    }

    @Test
    @WithMockUser
    void testGetAllProjectLocations_ServiceException() throws Exception {
        when(projectLocationService.getAllProjectLocations())
                .thenThrow(new ServiceException("Error getting project locations"));

        ResultActions result = mockMvc.perform(get("/project-locations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        assertEquals(500, result.andReturn().getResponse().getStatus());
        verify(projectLocationService, times(1)).getAllProjectLocations();
    }
}
