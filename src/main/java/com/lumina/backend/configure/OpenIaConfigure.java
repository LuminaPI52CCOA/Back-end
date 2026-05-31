package com.lumina.backend.configure;

import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenIaConfigure {

    @Bean
    public RestClient restClient() {

        return RestClient.builder()
                .baseUrl(
                        "https://api.openai.com/v1"
                )
                .build();
    }
}

