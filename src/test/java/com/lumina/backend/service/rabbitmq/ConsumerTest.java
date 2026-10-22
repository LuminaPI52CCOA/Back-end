package com.lumina.backend.service.rabbitmq;

import com.lumina.backend.dto.fila.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários Consumer RabbitMQ")
class ConsumerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private Consumer consumer;

    @Test
    @DisplayName("Deve retornar null e restaurar flag de interrupção quando a thread estiver interrompida")
    void deveRetornarNullQuandoInterrompido() {
        Message pedido = new Message(1L, "Cliente", "Produto", 1);
        Thread.currentThread().interrupt();
        try {
            Message resultado = consumer.receber(pedido);
            assertNull(resultado);
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted(); // limpa o estado de interrupção da thread de teste
        }
    }

    @Test
    @DisplayName("Deve processar e retornar a mensagem recebida")
    void deveReceberMensagemComSucesso() {
        Message pedido = new Message(1L, "Cliente", "Produto", 1);

        Message resultado = consumer.receber(pedido);

        assertEquals(pedido, resultado);
    }
}
