package com.securescan.config;

import java.sql.Connection;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Runs once at startup: verifies the database connection and logs the useful URLs.
 * If the connection fails, the exception stops the application (no silent failure).
 */
@Component
public class StartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    private final DataSource dataSource;
    private final String port;

    public StartupLogger(DataSource dataSource, @Value("${server.port:8080}") String port) {
        this.dataSource = dataSource;
        this.port = port;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            log.info("Database connection verified: {} {}",
                    connection.getMetaData().getDatabaseProductName(),
                    connection.getMetaData().getDatabaseProductVersion());
        }
        log.info("SecureScan started. Swagger UI: http://localhost:{}/swagger-ui/index.html", port);
        log.info("OpenAPI JSON: http://localhost:{}/v3/api-docs", port);
    }
}
