package com.expilicit.InvoiceCentral.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public OpenAPI invoiceSystemAPI(){

        return new OpenAPI()
                .info(new Info()
                        .title("invoice system api")
                        .description("API Documentation for Invoice Management System")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Dee")
                                .email("princedickson03@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createApiScheme()));
    }

    private SecurityScheme createApiScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
