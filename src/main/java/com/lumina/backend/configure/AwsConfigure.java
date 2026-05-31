package com.lumina.backend.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
public class AwsConfigure {

    @Bean // providencia sempre que necessario
    public TextractClient textractClient() {
        return TextractClient.builder().
                region(Region.US_EAST_1)
                .build();
    }
}
