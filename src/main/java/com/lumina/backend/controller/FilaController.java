package com.lumina.backend.controller;

import com.lumina.backend.dto.fila.Message;
import com.lumina.backend.service.rabbitmq.Producer;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menssagens")
@Tag(name = "mensagens", description = "Endpoints para envio das mensagens na fila do RabbitMQ")
public class FilaController {

    private final Producer producer;

    public FilaController(Producer producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> enviar(
            @RequestBody Message pedido) {

        producer.enviar(pedido);

        return ResponseEntity.ok(
                "Pedido enviado para o RabbitMQ!"
        );
    }

    @PostMapping("/varios/{quantidade}")
    public ResponseEntity<String> enviarVarios(
            @RequestBody  Message pedido,
            @PathVariable int quantidade) {

        producer.enviarVarios(pedido, quantidade);

        return ResponseEntity.ok(
                quantidade +
                        " pedidos enviados para o RabbitMQ!"
        );
    }

    @GetMapping
    public ResponseEntity<Message> consumer(){
        return null;
    }

}
