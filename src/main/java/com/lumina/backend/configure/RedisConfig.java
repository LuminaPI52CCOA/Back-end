package com.lumina.backend.configure;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Ajustes do cliente Redis (Lettuce). Host, porta e senha vem de spring.data.redis.*
 * (variaveis SPRING_DATA_REDIS_HOST / _PORT / _PASSWORD).
 *
 * O Redis e apenas cache: se ele estiver fora do ar a API tem de responder pelo MySQL sem
 * ficar esperando. Por isso os tempos sao curtos e comandos sao rejeitados na hora quando
 * nao ha conexao, em vez de ficarem enfileirados ate o timeout.
 */
@Configuration
public class RedisConfig {

    @Bean
    public LettuceClientConfigurationBuilderCustomizer redisFalhaRapida(
            @Value("${lumina.redis.timeout-ms:500}") long timeoutMs) {
        Duration timeout = Duration.ofMillis(timeoutMs);
        return builder -> builder
                .commandTimeout(timeout)
                .clientOptions(ClientOptions.builder()
                        .autoReconnect(true)
                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                        .socketOptions(SocketOptions.builder().connectTimeout(timeout).build())
                        .build());
    }
}
