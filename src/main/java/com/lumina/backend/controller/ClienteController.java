package com.lumina.backend.controller;

import com.lumina.backend.dto.anamnese.AnamneseMapper;
import com.lumina.backend.dto.anamnese.AnamneseRequest;
import com.lumina.backend.dto.anamnese.AnamneseResponse;
import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.dto.cliente.ClienteResponse;
import com.lumina.backend.dto.convenio.ConvenioMapper;
import com.lumina.backend.dto.convenio.ConvenioResponse;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.service.cliente.ClienteService;
import com.lumina.backend.service.convenio.ConvenioService;
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
    private final ConvenioService convenioService;


    public ClienteController(ClienteService service, ConvenioService convenioService){
        this.service = service;
        this.convenioService = convenioService;
    }

    @GetMapping
    @Operation(summary = "Lista ou busca clientes", description = "Retorna os clientes cadastrados no sistema, permitindo filtrar por nome ou CPF.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clientes retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "204", description = "Nenhum cliente encontrado", content = @Content)
    })
    public ResponseEntity<List<ClienteResponse>> listar(
            @Parameter(description = "Nome para busca parcial de clientes", example = "Maria")
            @RequestParam(required = false) String nome,
            @Parameter(description = "CPF exato do cliente", example = "12345678901")
            @RequestParam(required = false) String cpf){
        List<Cliente> clientes = service.listarComFiltros(nome, cpf);
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
        Cliente clienteCadastrado = service.cadastrar(request);
        ClienteResponse response = ClienteMapper.toDto(clienteCadastrado);
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
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id) {
        return ResponseEntity.status(200).body(ConvenioMapper.toResponse(convenioService.listarConveniosCliente(id)));
    }


    @GetMapping("/{id}/anamneses")
    public ResponseEntity<List<AnamneseResponse>> listarAnamnese(@PathVariable Integer id){
        List<Anamnese> anamneseList = service.listarAnamnese(id);
        List<AnamneseResponse> response = AnamneseMapper.toDto(anamneseList);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/anamneses")
    public ResponseEntity<AnamneseResponse> cadastroAnamnese(@PathVariable Long id,
                                                             @RequestBody AnamneseRequest request){
        Anamnese anamnese = service.cadastrarAnamnese(id, request);
        AnamneseResponse response = AnamneseMapper.toDto(anamnese);
        return ResponseEntity.ok(response);
    }
}
