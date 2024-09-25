package ca.bc.gov.nrs.wfprev;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ca.bc.gov.nrs.wfprev.controllers.ExampleController;
import ca.bc.gov.nrs.wfprev.data.resources.ExampleModel;
import ca.bc.gov.nrs.wfprev.services.ExampleService;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ExampleControllerTest {

    @Mock
    private ExampleService exampleService;

    @InjectMocks
    private ExampleController exampleController;

    @Autowired
    private MockMvc mockMvc;

    @Test
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

    // @Test
    void testGetExampleById() throws Exception {
        String exampleId = UUID.randomUUID().toString();
        ExampleModel exampleModel = new ExampleModel();
        exampleModel.setExampleGuid(exampleId);

        when(exampleService.getExampleById(exampleId)).thenReturn(exampleModel);

        mockMvc.perform(get("/wfprev/examples/{id}", exampleId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // @Test
    void testGetExampleByIdNotFound() throws Exception {
        String exampleId = UUID.randomUUID().toString();

        when(exampleService.getExampleById(exampleId)).thenReturn(null);

        mockMvc.perform(get("/wfprev/examples/{id}", exampleId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // @Test
    void getExampleCodeById_ShouldReturnNotFound_WhenExampleCodeDoesNotExist() throws Exception {
        String exampleCodeId = "INVALID_CODE";
        when(exampleService.getExampleCodeById(eq(exampleCodeId))).thenReturn(null);

        mockMvc.perform(get("/wfprev/exampleCodes/{id}", exampleCodeId))
                .andExpect(status().isNotFound());
    }

     private ExampleModel getExampleModel(String uuid) {
        return ExampleModel.builder().exampleGuid(uuid).build();
    }

}