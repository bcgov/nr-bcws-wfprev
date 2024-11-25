package ca.bc.gov.nrs.wfprev;

import org.springframework.boot.SpringApplication;

import ca.bc.gov.nrs.wfprev.WfprevApiApplication;

public class TestWfprevApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(WfprevApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
