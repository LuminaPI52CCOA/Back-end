package com.lumina.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.data.redis.RedisHealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.data.redis.RedisReactiveHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
// O Redis e so cache: nao ha repositorios Redis, e ele fora do ar nao pode derrubar o
// /actuator/health (o ALB tiraria o backend de servico por causa de um cache).
@SpringBootApplication(exclude = {
		RedisRepositoriesAutoConfiguration.class,
		RedisHealthContributorAutoConfiguration.class,
		RedisReactiveHealthContributorAutoConfiguration.class
})
public class BackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}
