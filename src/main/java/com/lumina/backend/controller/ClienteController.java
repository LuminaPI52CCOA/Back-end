package com.lumina.backend.controller;

import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.dto.cliente.ClienteResponse;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.service.cliente.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service){
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar(){
        List<Cliente> clientes = service.listar();
        if(clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<ClienteResponse> response = ClienteMapper.toDto(clientes);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@RequestBody @Valid ClienteRequest request){
        service.cadastrar(request);
        Cliente clienteResponse = ClienteMapper.toEntity(request);
        ClienteResponse response = ClienteMapper.toDto(clienteResponse);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Integer id){
        Cliente cliente = service.buscarPorId(id);
        ClienteResponse response = ClienteMapper.toDto(cliente);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(@RequestBody @Valid ClienteRequest request,
                                                     @PathVariable Integer id){
        Cliente cliente = service.atualizar(request, id);
        ClienteResponse response = ClienteMapper.toDto(cliente);
        return ResponseEntity.ok(response);
    }
}
