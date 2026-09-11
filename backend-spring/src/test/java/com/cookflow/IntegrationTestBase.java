package com.cookflow;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Arranca una PostgreSQL en Docker (Testcontainers) y expone su JDBC URL
 * como `spring.datasource.*` para el contexto de Spring.
 *
 * <p>Usamos `withReuse(true)` + arranque manual con `@BeforeAll` (en vez de
 * `@Container`) para que el container no se detenga ni elimine entre test
 * classes: todas las clases comparten la misma instancia y el pool de
 * conexiones de HikariCP sigue siendo válido.
 */
@SpringBootTest(classes = CookFlowApplication.class)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withReuse(true)
            .withDatabaseName("cookflow_test")
            .withUsername("cookflow")
            .withPassword("cookflow");

    @BeforeAll
    static void startPostgres() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
