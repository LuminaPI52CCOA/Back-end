package com.lumina.backend.controller;

import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.dto.cliente.ClienteResponse;
import com.lumina.backend.dto.convenio.ConvenioResponse;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.service.Convenio.ConvenioService;
import com.lumina.backend.service.cliente.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Endpoints para cadastro e gestao de clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service){
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista clientes", description = "Retorna os clientes cadastrados no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clientes retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "204", description = "Nao ha clientes cadastrados", content = @Content)
    })
    public ResponseEntity<List<ClienteResponse>> listar(){
        List<Cliente> clientes = service.listar();
        if(clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<ClienteResponse> response = ClienteMapper.toDto(clientes);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Cadastra cliente", description = "Cadastra um novo cliente a partir dos dados informados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos para cadastro", content = @Content)
    })
    public ResponseEntity<ClienteResponse> cadastrar(
            @RequestBody(description = "Dados de cadastro do cliente", required = true,
                    content = @Content(schema = @Schema(implementation = ClienteRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody @Valid ClienteRequest request){
        service.cadastrar(request);
        Cliente clienteResponse = ClienteMapper.toEntity(request);
        ClienteResponse response = ClienteMapper.toDto(clienteResponse);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca cliente por ID", description = "Retorna os dados de um cliente pelo identificador informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente nao encontrado", content = @Content)
    })
    public ResponseEntity<ClienteResponse> buscarPorId(
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id){
        Cliente cliente = service.buscarPorId(id);
        ClienteResponse response = ClienteMapper.toDto(cliente);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza cliente", description = "Atualiza os dados de um cliente pelo identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos para atualizacao", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente nao encontrado", content = @Content)
    })
    public ResponseEntity<ClienteResponse> atualizar(
            @RequestBody(description = "Dados para atualizacao do cliente", required = true,
                    content = @Content(schema = @Schema(implementation = ClienteRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody @Valid ClienteRequest request,
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id){
        Cliente cliente = service.atualizar(request, id);
        ClienteResponse response = ClienteMapper.toDto(cliente);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/convenios")
    @Operation(summary = "Lista convenios do cliente", description = "Retorna os convenios vinculados ao cliente informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Convenios do cliente retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = ConvenioResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente nao encontrado", content = @Content)
    })
    public ResponseEntity<List<ConvenioResponse>> listarConvenios(
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Integer id) {
        return ResponseEntity.status(200).body(null);
    }
}
