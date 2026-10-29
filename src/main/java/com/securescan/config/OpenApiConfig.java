package com.securescan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI secureScanOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("SecureScan - Antivirus Management System API")
                .version("1.0.0")
                .description("REST API for managing scan records, scanned-file metadata, threat records "
                        + "and quarantine records. IMPORTANT: this backend stores RECORDS only. It does not "
                        + "scan the host computer, does not detect real malware and does not quarantine, move "
                        + "or delete real files. Threat and quarantine records exist only if a client "
                        + "explicitly submits them."));
    }
}
