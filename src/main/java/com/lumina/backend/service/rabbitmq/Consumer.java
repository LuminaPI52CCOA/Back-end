package com.lumina.backend.service.rabbitmq;

import com.lumina.backend.configure.RabbitConfigure;
import com.lumina.backend.dto.fila.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class Consumer {
    private final RabbitTemplate rabbitTemplate;

    public Consumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitConfigure.QUEUE)
    public Message receber(Message pedido){
        try {

            System.out.println(
                    "[CONSUMER] Iniciando processamento: "
                            + pedido
            );
            Thread.sleep(2000);

            System.out.println(
                    "[CONSUMER] Pedido processado: "
                            + pedido
            );

            return pedido;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(
                    "[CONSUMER] Processamento interrompido."
            );
        }
        return null;
    }
}
