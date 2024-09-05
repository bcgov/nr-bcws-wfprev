package ca.bc.gov.nrs.wfprev;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ca.bc.gov.nrs.wfprev.controllers.ExampleController;
import ca.bc.gov.nrs.wfprev.data.resources.ExampleModel;
import ca.bc.gov.nrs.wfprev.services.ExampleService;

class ExampleControllerTest {

    @Mock
    private ExampleService exampleService;

    @InjectMocks
    private ExampleController exampleController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(exampleController).build();
    }

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

}