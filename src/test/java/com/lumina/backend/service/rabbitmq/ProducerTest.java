package com.lumina.backend.service.rabbitmq;

import com.lumina.backend.configure.RabbitConfigure;
import com.lumina.backend.dto.fila.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários Producer RabbitMQ")
class ProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private Producer producer;

    @Test
    @DisplayName("Deve enviar uma mensagem para o exchange e routing key configurados")
    void deveEnviarMensagemUnica() {
        Message pedido = new Message(10L, "Maria", "Clareamento", 1);

        producer.enviar(pedido);

        verify(rabbitTemplate, times(1)).convertAndSend(
                RabbitConfigure.EXCHANGE,
                RabbitConfigure.ROUTING_KEY,
                pedido
        );
    }

    @Test
    @DisplayName("Deve enviar várias mensagens incrementando o ID do pedido")
    void deveEnviarVariasMensagens() {
        Message pedidoBase = new Message(10L, "Maria", "Clareamento", 2);

        producer.enviarVarios(pedidoBase, 3);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(rabbitTemplate, times(3)).convertAndSend(
                eq(RabbitConfigure.EXCHANGE),
                eq(RabbitConfigure.ROUTING_KEY),
                captor.capture()
        );

        List<Message> enviados = captor.getAllValues();
        assertEquals(11L, enviados.get(0).id());
        assertEquals(12L, enviados.get(1).id());
        assertEquals(13L, enviados.get(2).id());
        assertEquals("Maria", enviados.get(0).cliente());
    }
}
