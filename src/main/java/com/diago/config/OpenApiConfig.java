package com.diago.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentation interactive de l'API : http://localhost:8080/swagger-ui.html */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI diagoOpenApi() {
        return new OpenAPI()
                .info(new Info().title("DiaGo API")
                        .version("1.0")
                        .description("API REST de location de voitures à Madagascar. "
                                + "Authentification HTTP Basic (email + mot de passe). "
                                + "Compte de test : demo@diago.mg / demo1234 ; admin : admin@diago.mg / admin1234"))
                .components(new Components().addSecuritySchemes("basicAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}
