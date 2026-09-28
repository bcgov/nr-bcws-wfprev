package ca.bc.gov.nrs.wfprev.services.reportjobs;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Runs {@link ReportJobSweeper}. */
@Configuration
@EnableScheduling
public class ReportJobSchedulingConfig {
}
