package com.finance.dashboard.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configures OpenAPI / Swagger metadata including:
 * - API info (title, description, version)
 * - Bearer JWT security scheme so the "Authorize" button appears in Swagger UI
 * - Global security requirement applied to all endpoints except auth
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Finance Dashboard API",
                version = "1.0.0",
                description = """
                        REST API for the Finance Dashboard application.
                        
                        **Roles:**
                        - `VIEWER` — read access to records and summary dashboard
                        - `ANALYST` — read + analytics endpoints (categories, monthly)
                        - `ADMIN` — full access including user management and delete
                        
                        **Authentication:** Click **Authorize**, enter `Bearer <your-jwt-token>`
                        """,
                contact = @Contact(name = "Finance Dashboard Team", email = "admin@finance.com")
        ),
        servers = @Server(url = "http://localhost:8080", description = "Local Development"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Provide a valid JWT token obtained from POST /api/auth/login"
)
public class OpenApiConfig {
    // Configuration is fully annotation-driven; no bean methods needed.
}
