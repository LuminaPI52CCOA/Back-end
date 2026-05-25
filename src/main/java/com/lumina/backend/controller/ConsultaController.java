package com.lumina.backend.controller;

import com.lumina.backend.dto.consulta.ConsultaMapper;
import com.lumina.backend.dto.consulta.ConsultaRequest;
import com.lumina.backend.dto.consulta.ConsultaResponse;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.service.consulta.ConsultaService;
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
@RequestMapping("/consultas")
@Tag(name = "Consultas", description = "Endpoints para agenda e gestao de consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    @Operation(summary = "Lista consultas", description = "Retorna as consultas cadastradas no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consultas retornadas com sucesso",
                    content = @Content(schema = @Schema(implementation = ConsultaResponse.class)))
    })
    public ResponseEntity<List<ConsultaResponse>> listar() {
        return ResponseEntity.status(200).body(ConsultaMapper.toResponse(consultaService.listar()));
    }

    @PostMapping
    @Operation(summary = "Cadastra consulta", description = "Cadastra uma nova consulta com cliente, usuario e horarios informados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consulta cadastrada com sucesso",
                    content = @Content(schema = @Schema(implementation = ConsultaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos para cadastro", content = @Content)
    })
    public ResponseEntity<ConsultaResponse> cadastrar(
            @RequestBody(description = "Dados de cadastro da consulta", required = true,
                    content = @Content(schema = @Schema(implementation = ConsultaRequest.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody ConsultaRequest consulta) {
        return ResponseEntity.status(201).body(ConsultaMapper.toResponse(consultaService.cadastrar(consulta)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca consulta por ID", description = "Retorna os dados de uma consulta pelo identificador informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta encontrada",
                    content = @Content(schema = @Schema(implementation = ConsultaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Consulta nao encontrada", content = @Content)
    })
    public ResponseEntity<ConsultaResponse> buscarPorId(
            @Parameter(description = "ID da consulta", example = "1") @PathVariable Long id) {
        return ResponseEntity.status(200).body(ConsultaMapper.toResponse(consultaService.buscarPorId(id)));
    }
}
