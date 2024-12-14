package ca.bc.gov.nrs.wfprev;

import org.springframework.boot.SpringApplication;

public class TestWfprevApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(WfprevApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
