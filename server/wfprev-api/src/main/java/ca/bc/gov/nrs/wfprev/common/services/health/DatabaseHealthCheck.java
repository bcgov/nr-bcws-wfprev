package ca.bc.gov.nrs.wfprev.common.services.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

@Component
public class DatabaseHealthCheck extends AbstractHealthIndicator  {
  
  private HikariDataSource ds;

  public DatabaseHealthCheck(HikariDataSource ds) {
    this.ds = ds;
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) throws Exception {
    double maxSize = ds.getMaximumPoolSize();
    double active = new HikariDataSourcePoolMetadata(ds).getActive();
    Double usage = (active / maxSize) * 100d;

    Health.Builder workingBuilder;

    if(usage > 90) {
        workingBuilder = builder.down();
    }else {
        workingBuilder = builder.up();
    }

    workingBuilder.withDetail("max", maxSize) //
    .withDetail("active", active)//
    .withDetail("usage", usage);
  }
}