package com.lumina.backend.controller.fila;

import com.lumina.backend.controller.FilaController;
import com.lumina.backend.dto.fila.Message;
import com.lumina.backend.service.rabbitmq.Producer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários FilaController")
class FilaControllerTest {

    @Mock
    private Producer producer;

    @InjectMocks
    private FilaController filaController;

    @Test
    @DisplayName("Deve enviar uma mensagem para o RabbitMQ e retornar 200 OK")
    void deveEnviarMensagemUnica() {
        Message message = new Message(1L, "João", "Limpeza", 1);

        ResponseEntity<String> response = filaController.enviar(message);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pedido enviado para o RabbitMQ!", response.getBody());
        verify(producer, times(1)).enviar(message);
    }

    @Test
    @DisplayName("Deve enviar várias mensagens para o RabbitMQ e retornar 200 OK")
    void deveEnviarVariasMensagens() {
        Message message = new Message(1L, "João", "Limpeza", 1);

        ResponseEntity<String> response = filaController.enviarVarios(message, 3);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("3 pedidos enviados para o RabbitMQ!", response.getBody());
        verify(producer, times(1)).enviarVarios(message, 3);
    }

    @Test
    @DisplayName("Deve retornar null no endpoint consumer")
    void deveRetornarNullNoConsumer() {
        assertNull(filaController.consumer());
    }
}
