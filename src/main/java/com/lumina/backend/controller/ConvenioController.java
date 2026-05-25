package com.lumina.backend.controller;

import com.lumina.backend.dto.convenio.ConvenioMapper;
import com.lumina.backend.dto.convenio.ConvenioRequest;
import com.lumina.backend.dto.convenio.ConvenioResponse;
import com.lumina.backend.service.Convenio.ConvenioService;
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
@RequestMapping("/convenios")
@Tag(name = "Convenios", description = "Endpoints para cadastro e gestao de convenios")
public class ConvenioController {

    private final ConvenioService convenioService;

    public ConvenioController(ConvenioService convenioService) {
        this.convenioService = convenioService;
    }

    @GetMapping
    @Operation(summary = "Lista convenios", description = "Retorna todos os convenios cadastrados no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Convenios retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = ConvenioResponse.class)))
    })
    public ResponseEntity<List<ConvenioResponse>> listar() {
        return ResponseEntity.status(200).body(ConvenioMapper.toResponse(convenioService.listar()));
    }

    @PostMapping
    @Operation(summary = "Cadastra convenio", description = "Cadastra um novo convenio no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Convenio cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ConvenioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos para cadastro", content = @Content)
    })
    public ResponseEntity<ConvenioResponse> cadastrar(
            @RequestBody(description = "Dados de cadastro do convenio", required = true,
                    content = @Content(schema = @Schema(implementation = ConvenioRequest.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody ConvenioRequest convenioRequest) {
        return ResponseEntity.status(201).body(ConvenioMapper.toResponse(convenioService.cadastrar(convenioRequest)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza convenio", description = "Atualiza os dados de um convenio pelo identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Convenio atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ConvenioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos para atualizacao", content = @Content),
            @ApiResponse(responseCode = "404", description = "Convenio nao encontrado", content = @Content)
    })
    public ResponseEntity<ConvenioResponse> atualizar(
            @Parameter(description = "ID do convenio", example = "1") @PathVariable Long id,
            @RequestBody(description = "Dados para atualizacao do convenio", required = true,
                    content = @Content(schema = @Schema(implementation = ConvenioRequest.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody ConvenioRequest convenioRequest) {

        return ResponseEntity.status(200).body(ConvenioMapper.toResponse(convenioService.atualizar(id, convenioRequest)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui convenio", description = "Remove um convenio existente pelo identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Convenio removido com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Convenio nao encontrado", content = @Content)
    })
    public ResponseEntity<Void> deletar(@Parameter(description = "ID do convenio", example = "1") @PathVariable Long id) {
        convenioService.deletar(id);
        return ResponseEntity.status(204).build();
    }


}
