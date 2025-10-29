package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.controllers.BoundaryTileController;
import ca.bc.gov.nrs.wfprev.services.BoundaryTileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoundaryTileController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class, MockMvcRestExceptionConfiguration.class})
@MockBean(JpaMetamodelMappingContext.class)
@WithMockUser
class BoundaryTileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoundaryTileService boundaryTileService;

    @MockBean(name = "springSecurityAuditorAware")
    private AuditorAware<String> auditorAware;


    @Test
    void getProjectBoundaryTiles_ok_setsHeaders_andBody() throws Exception {
        byte[] tile = new byte[]{0x01, 0x02};
        when(boundaryTileService.getProjectBoundaryTile(anyInt(), anyInt(), anyInt(), anyList()))
                .thenReturn(tile);

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        mockMvc.perform(get("/tiles/project_boundary/{z}/{x}/{y}.mvt", 8, 123, 456)
                        .param("projectGuid", p1.toString(), p2.toString())
                        .accept("application/vnd.mapbox-vector-tile"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.mapbox-vector-tile"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, immutable"))
                .andExpect(content().bytes(tile));

        verify(boundaryTileService).getProjectBoundaryTile(
                eq(8),
                eq(123),
                eq(456),
                argThat((java.util.List<UUID> list) ->
                        list.size() == 2 && list.contains(p1) && list.contains(p2)
                )
        );
    }

    @Test
    void getProjectBoundaryTiles_nullFromService_returns200WithEmptyBody() throws Exception {
        when(boundaryTileService.getProjectBoundaryTile(anyInt(), anyInt(), anyInt(), anyList()))
                .thenReturn(null);

        UUID p1 = UUID.randomUUID();

        mockMvc.perform(get("/tiles/project_boundary/{z}/{x}/{y}.mvt", 10, 1, 2)
                        .param("projectGuid", p1.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.mapbox-vector-tile"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, immutable"))
                .andExpect(content().bytes(new byte[0])); // empty
    }

    @Test
    void getProjectBoundaryTiles_runtimeException_returns500() throws Exception {
        doThrow(new RuntimeException("boom"))
                .when(boundaryTileService).getProjectBoundaryTile(anyInt(), anyInt(), anyInt(), anyList());

        UUID p1 = UUID.randomUUID();

        mockMvc.perform(get("/tiles/project_boundary/{z}/{x}/{y}.mvt", 8, 1, 2)
                        .param("projectGuid", p1.toString()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getActivityBoundaryTiles_ok_setsHeaders_andBody() throws Exception {
        byte[] tile = new byte[]{0x0A, 0x0B, 0x0C};
        when(boundaryTileService.getActivityBoundaryTile(anyInt(), anyInt(), anyInt(), anyList()))
                .thenReturn(tile);

        UUID p1 = UUID.randomUUID();

        mockMvc.perform(get("/tiles/activity_boundary/{z}/{x}/{y}.mvt", 12, 654, 321)
                        .param("projectGuid", p1.toString())
                        .accept("application/vnd.mapbox-vector-tile"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.mapbox-vector-tile"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, immutable"))
                .andExpect(content().bytes(tile));

        verify(boundaryTileService).getActivityBoundaryTile(
                eq(12),
                eq(654),
                eq(321),
                argThat((java.util.List<UUID> list) -> list.size() == 1 && list.contains(p1))
        );
    }

    @Test
    void getActivityBoundaryTiles_nullFromService_returns200WithEmptyBody() throws Exception {
        when(boundaryTileService.getActivityBoundaryTile(anyInt(), anyInt(), anyInt(), anyList()))
                .thenReturn(null);

        UUID p1 = UUID.randomUUID();

        mockMvc.perform(get("/tiles/activity_boundary/{z}/{x}/{y}.mvt", 9, 3, 4)
                        .param("projectGuid", p1.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.mapbox-vector-tile"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, immutable"))
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    void getActivityBoundaryTiles_runtimeException_returns500() throws Exception {
        doThrow(new RuntimeException("kaboom"))
                .when(boundaryTileService).getActivityBoundaryTile(anyInt(), anyInt(), anyInt(), anyList());

        UUID p1 = UUID.randomUUID();

        mockMvc.perform(get("/tiles/activity_boundary/{z}/{x}/{y}.mvt", 9, 3, 4)
                        .param("projectGuid", p1.toString()))
                .andExpect(status().isInternalServerError());
    }
}
