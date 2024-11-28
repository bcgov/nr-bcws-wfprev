package ca.bc.gov.nrs.wfprev;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.bc.gov.nrs.wfprev.controllers.ProgramAreaController;
import ca.bc.gov.nrs.wfprev.data.models.ProgramAreaModel;
import ca.bc.gov.nrs.wfprev.services.ProgramAreaService;

@WebMvcTest(ProgramAreaController.class)
@Import({TestSpringSecurity.class, TestcontainersConfiguration.class})
class ProgramAreaControllerTest {
    @MockBean
    private ProgramAreaService programAreaService;

    @Autowired
    private MockMvc mockMvc;

    private Gson gson;

    @BeforeEach
    void setup() {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").serializeSpecialFloatingPointValues();
        gson = builder.create();
    }

    @Test
    @WithMockUser
    void testGetProgramArea() throws Exception {
        String guid = UUID.randomUUID().toString();

        ProgramAreaModel programArea = new ProgramAreaModel();
        programArea.setProgramAreaGuid(guid);

        List<ProgramAreaModel> programAreaList = Arrays.asList(programArea);
        CollectionModel<ProgramAreaModel> programAreaModel = CollectionModel.of(programAreaList);

        when(programAreaService.getAllProgramAreas()).thenReturn(programAreaModel);

        mockMvc.perform(get("/programAreas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        when(programAreaService.getProgramAreaById(guid)).thenReturn(programArea);

        mockMvc.perform(get("/programAreas/{id}", guid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testCreateUpdateProgramArea() throws Exception {
        ProgramAreaModel programArea = new ProgramAreaModel();
        programArea.setProgramAreaName("Test");
        String id = UUID.randomUUID().toString();
        programArea.setProgramAreaGuid(id);
        when(programAreaService.createOrUpdateProgramArea(any(ProgramAreaModel.class))).thenReturn(programArea);

        String json = gson.toJson(programArea);

        mockMvc.perform(post("/programAreas")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("application/json")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isCreated());


        programArea.setProgramAreaName("Test Change");
        when(programAreaService.createOrUpdateProgramArea(programArea)).thenReturn(programArea);

        json = gson.toJson(programArea);

        mockMvc.perform(put("/programAreas/{id}", id)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.programAreaName").value("Test Change"));
    }

    @Test
    @WithMockUser
    void testDeleteProgramArea() throws Exception {
        ProgramAreaModel programArea = new ProgramAreaModel();
        when(programAreaService.createOrUpdateProgramArea(any(ProgramAreaModel.class))).thenReturn(programArea);

        String json = gson.toJson(programArea);

        mockMvc.perform(post("/programAreas")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isCreated());

        when(programAreaService.deleteProgramArea(programArea.getProgramAreaGuid())).thenReturn(null);

        mockMvc.perform(delete("/programAreas/{id}", programArea.getProgramAreaGuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }
}
