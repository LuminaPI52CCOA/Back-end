package com.lumina.backend.service.rabbitmq;

import com.lumina.backend.configure.RabbitConfigure;
import com.lumina.backend.dto.fila.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {
    private final RabbitTemplate rabbitTemplate;

    public Producer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Envia uma única mensagem
    public void enviar(Message pedido) {

        rabbitTemplate.convertAndSend(
                RabbitConfigure.EXCHANGE,
                RabbitConfigure.ROUTING_KEY,
                pedido
        );

        System.out.println(
                "[PRODUCER] Pedido enviado: " + pedido
        );
    }

    // Envia várias mensagens para a fila
    public void enviarVarios(Message pedido, int quantidade) {

        for (int i = 1; i <= quantidade; i++) {

            // Cria um pedido diferente para cada mensagem
            Message novoPedido = new Message(
                    pedido.id() + i,
                    pedido.cliente(),
                    pedido.produto(),
                    pedido.quantidade()
            );

            // Publica a mensagem no Exchange
            rabbitTemplate.convertAndSend(
                    RabbitConfigure.EXCHANGE,
                    RabbitConfigure.ROUTING_KEY,
                    novoPedido
            );

            System.out.println(
                    "[PRODUCER] Mensagem " + i +
                            " de " + quantidade +
                            " enviada: " + novoPedido
            );
        }
    }
}
