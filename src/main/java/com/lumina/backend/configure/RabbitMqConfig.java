package com.lumina.backend.configure;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_NOTIFICACOES = "lumina.notificacoes.topic";
    public static final String QUEUE_WHATSAPP = "queue.notificacao.whatsapp";
    public static final String BINDING_KEY_WHATSAPP = "notificacao.*.whatsapp";
    public static final String ROUTING_KEY_LEMBRETE_WHATSAPP = "notificacao.lembrete.whatsapp";

    public static final String EXCHANGE_DLX = "lumina.notificacoes.dlx";
    public static final String QUEUE_WHATSAPP_DLQ = "queue.notificacao.whatsapp.dlq";
    public static final String ROUTING_KEY_DLQ_WHATSAPP = "notificacao.dlq.whatsapp";

    @Bean
    public TopicExchange notificacoesTopicExchange() {
        return new TopicExchange(EXCHANGE_NOTIFICACOES, true, false);
    }

    @Bean
    public TopicExchange notificacoesDeadLetterExchange() {
        return new TopicExchange(EXCHANGE_DLX, true, false);
    }

    @Bean
    public Queue whatsappQueue() {
        return QueueBuilder.durable(QUEUE_WHATSAPP)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DLQ_WHATSAPP)
                .build();
    }

    @Bean
    public Queue whatsappDeadLetterQueue() {
        return QueueBuilder.durable(QUEUE_WHATSAPP_DLQ).build();
    }

    @Bean
    public Binding whatsappBinding(Queue whatsappQueue, TopicExchange notificacoesTopicExchange) {
        return BindingBuilder.bind(whatsappQueue)
                .to(notificacoesTopicExchange)
                .with(BINDING_KEY_WHATSAPP);
    }

    @Bean
    public Binding whatsappDlqBinding(Queue whatsappDeadLetterQueue, TopicExchange notificacoesDeadLetterExchange) {
        return BindingBuilder.bind(whatsappDeadLetterQueue)
                .to(notificacoesDeadLetterExchange)
                .with(ROUTING_KEY_DLQ_WHATSAPP);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
