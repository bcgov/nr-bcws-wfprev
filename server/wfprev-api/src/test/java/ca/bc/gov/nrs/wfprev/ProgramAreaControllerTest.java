package ca.bc.gov.nrs.wfprev;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.bc.gov.nrs.wfprev.controllers.ProgramAreaController;
import ca.bc.gov.nrs.wfprev.data.models.ProgramAreaModel;
import ca.bc.gov.nrs.wfprev.services.ProgramAreaService;

@WebMvcTest(ProgramAreaController.class)
@Import({SecurityConfig.class, TestcontainersConfiguration.class})
class ProgramAreaControllerTest {
  @MockBean
  private ProgramAreaService programAreaService;

  @Autowired
  private MockMvc mockMvc;
  
  @Test
  @WithMockUser
  void testGetProject() throws Exception {
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
  void testCreateUpdateProject() throws Exception {
    ProgramAreaModel programArea = new ProgramAreaModel();
    when(programAreaService.createOrUpdateProgramArea(programArea)).thenReturn(programArea);

    mockMvc.perform(post("/programAreas", programArea)
           .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isCreated());


    programArea.setProgramAreaName("Test");
    when(programAreaService.createOrUpdateProgramArea(programArea)).thenReturn(programArea);

    mockMvc.perform(put("/programAreas", programArea)
           .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser
  void testDeleteProject() throws Exception {
    ProgramAreaModel programArea = new ProgramAreaModel();
    when(programAreaService.createOrUpdateProgramArea(programArea)).thenReturn(programArea);

    mockMvc.perform(post("/programArea", programArea)
           .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isCreated());

    when(programAreaService.deleteProgramArea(programArea.getProgramAreaGuid())).thenReturn(null);

    mockMvc.perform(delete("/programArea/{id}", programArea.getProgramAreaGuid())
           .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk());
  }
}
