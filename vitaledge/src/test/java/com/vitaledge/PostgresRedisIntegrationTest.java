package com.vitaledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vitaledge.config.TestRedisConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Verifies Flyway migrations and seed data against a real PostgreSQL container.
 * Runs via failsafe ({@code mvn verify -Dgroups=integration}) and is skipped
 * automatically when the Docker daemon is unavailable.
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class PostgresRedisIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("vitaledge")
            .withUsername("vitaledge")
            .withPassword("vitaledge");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.sql.init.mode", () -> "never");
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void flywayAppliesMigrationsAndSeed() {
        List<String> migrations = jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success = TRUE ORDER BY installed_rank",
                String.class);
        assertTrue(migrations.contains("1"), "V1 schema migration must apply");
        assertTrue(migrations.contains("2"), "V2 seed migration must apply");

        Integer products = jdbcTemplate.queryForObject("SELECT count(*) FROM products", Integer.class);
        assertEquals(10, products);

        Integer questions = jdbcTemplate.queryForObject("SELECT count(*) FROM questions", Integer.class);
        assertEquals(16, questions);

        Integer roles = jdbcTemplate.queryForObject("SELECT count(*) FROM roles", Integer.class);
        assertEquals(3, roles);
    }
}