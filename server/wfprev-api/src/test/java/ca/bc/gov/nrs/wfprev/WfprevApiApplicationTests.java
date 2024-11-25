package ca.bc.gov.nrs.wfprev;

import static org.assertj.core.api.Assertions.assertThat; // Using AssertJ for more fluent assertions

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class WfprevApiApplicationTests {

    @Autowired
    private TestcontainersConfiguration testcontainersConfiguration;

    @Test
    void contextLoads() {
     assertThat(testcontainersConfiguration).isNotNull();
    }

}