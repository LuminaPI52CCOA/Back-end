package com.lumina.backend.configure;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfigure {
    public static final String QUEUE = "pedidos.queue";
    public static final String EXCHANGE = "pedidos.exchange";
    public static final String ROUTING_KEY = "pedidos.routingkey";

    @Bean
    public Queue pedidosQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public DirectExchange pedidosExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding pedidosBinding(Queue pedidosQueue, DirectExchange pedidosExchange) {
        return BindingBuilder.bind(pedidosQueue)
                .to(pedidosExchange)
                .with(ROUTING_KEY);
    }
}
