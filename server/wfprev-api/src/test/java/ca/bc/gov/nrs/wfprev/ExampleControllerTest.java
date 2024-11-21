package ca.bc.gov.nrs.wfprev;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.bc.gov.nrs.wfprev.controllers.ExampleController;
import ca.bc.gov.nrs.wfprev.data.models.ExampleCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ExampleModel;
import ca.bc.gov.nrs.wfprev.services.ExampleService;

@WebMvcTest(ExampleController.class)
@Import({SecurityConfig.class, TestcontainersConfiguration.class})
class ExampleControllerTest {

    @MockBean
    private ExampleService exampleService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testGetAllExamples() throws Exception {
        String exampleId1 = UUID.randomUUID().toString();
        String exampleId2 = UUID.randomUUID().toString();

        ExampleModel example1 = new ExampleModel();
        example1.setExampleGuid(exampleId1);

        ExampleModel example2 = new ExampleModel();
        example2.setExampleGuid(exampleId2);

        List<ExampleModel> exampleList = Arrays.asList(example1, example1);
        CollectionModel<ExampleModel> collectionModel = CollectionModel.of(exampleList);

        when(exampleService.getAllExamples()).thenReturn(collectionModel);

        mockMvc.perform(get("/wfprev/examples")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetExampleById() throws Exception {
        String exampleId = UUID.randomUUID().toString();
        ExampleModel exampleModel = new ExampleModel();
        exampleModel.setExampleGuid(exampleId);

        when(exampleService.getExampleById(exampleId)).thenReturn(exampleModel);

        mockMvc.perform(get("/wfprev/examples/{id}", exampleId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetExampleByIdNotFound() throws Exception {
        String exampleId = null;
        ExampleModel exampleModel = new ExampleModel();

        when(exampleService.getExampleById(null)).thenReturn(exampleModel);

        mockMvc.perform(get("/wfprev/examples/{id}", exampleId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getUserNotFoundCodeById() throws Exception {
        String exampleCodeId = "INVALID_CODE";

        when(exampleService.getExampleCodeById(eq(exampleCodeId))).thenReturn(null);

        mockMvc.perform(get("/wfprev/exampleCodes/{id}", exampleCodeId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getExampleCodeById() throws Exception {
        String exampleCodeId = "VALID_CODE";
        ExampleCodeModel exampleCodeModel = new ExampleCodeModel();
        exampleCodeModel.setExampleCode(exampleCodeId);

        when(exampleService.getExampleCodeById(eq(exampleCodeId))).thenReturn(exampleCodeModel);

        mockMvc.perform(get("/wfprev/exampleCodes/{id}", exampleCodeId))
                .andExpect(status().isOk());

    }
}
