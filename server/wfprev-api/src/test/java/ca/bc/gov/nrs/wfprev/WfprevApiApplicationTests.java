package ca.bc.gov.nrs.wfprev;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class WfprevApiApplicationTests {

	@Test
	void contextLoads() {
		// empty test stub
	}

}
