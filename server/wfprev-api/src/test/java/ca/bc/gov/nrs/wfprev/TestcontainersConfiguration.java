package ca.bc.gov.nrs.wfprev;

import java.time.temporal.ChronoUnit;
import java.time.Duration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
	@Bean
	@ServiceConnection
	static PostgreSQLContainer<?> postgresContainer() {
		DockerImageName image = DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres");
		PostgreSQLContainer<?> container = new PostgreSQLContainer<>(image)
		.withDatabaseName("wfprev")
		.withUsername("wfprev")
		.withPassword("password")
		.withExposedPorts(5432);

		container.setWaitStrategy(
			new LogMessageWaitStrategy()
							.withRegEx(".*database system is ready to accept connections.*\\s")
							.withTimes(1)
							.withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS))
		);

	  return container;
	}

	@DynamicPropertySource
	static void postgresqlProperties(DynamicPropertyRegistry registry) {
			registry.add("spring.datasource.url", postgresContainer()::getJdbcUrl);
			registry.add("spring.datasource.username", postgresContainer()::getUsername);
			registry.add("spring.datasource.password", postgresContainer()::getPassword);
			registry.add("spring.datasource.password", postgresContainer()::getPassword);
	}
}