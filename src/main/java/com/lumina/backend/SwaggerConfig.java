package com.lumina.backend;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lumina Management API") // Título Principal
                        .version("1.0.0")
                        .description("Esta é a documentação completa dos endpoints do sistema.")
                        .contact(new Contact().name("Lumina").email("email@exemplo.com")));
    }


}
