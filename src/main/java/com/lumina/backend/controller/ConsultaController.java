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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lumina.backend.dto.alexa.AlexaAnamneseAlertaDto;
import com.lumina.backend.dto.alexa.AlexaConsultaDto;
import com.lumina.backend.dto.alexa.AlexaResumoDiaDto;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.alexa.AlexaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/consultas")
@PreAuthorize("hasAnyRole('ADMIN', 'DENTISTA', 'RECEPCIONISTA')")
@Tag(name = "Consultas", description = "Endpoints para agenda e gestao de consultas")
public class ConsultaController {

    private final ConsultaService consultaService;
    private final AlexaService alexaService;
    private final UsuarioRepository usuarioRepository;

    public ConsultaController(ConsultaService consultaService,
                              AlexaService alexaService,
                              UsuarioRepository usuarioRepository) {
        this.consultaService = consultaService;
        this.alexaService = alexaService;
        this.usuarioRepository = usuarioRepository;
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

    @PutMapping("/{id}")
    @Operation(summary = "Reagendar consulta", description = "Atualiza as informações de data e horário de uma consulta específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta reagendada com sucesso",
                    content = @Content(schema = @Schema(implementation = ConsultaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para reagendamento", content = @Content),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada", content = @Content)
    })
    public ResponseEntity<ConsultaResponse> reagendar(
            @Parameter(description = "ID da consulta", example = "1") @PathVariable Long id,
            @RequestBody(description = "Novos dados para a consulta", required = true,
                    content = @Content(schema = @Schema(implementation = ConsultaRequest.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody ConsultaRequest consultaRequest) {

        Consulta consultaAtualizada = consultaService.reagendar(id, consultaRequest);
        return ResponseEntity.status(200).body(ConsultaMapper.toResponse(consultaAtualizada));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela consulta", description = "Remove uma consulta da agenda pelo identificador informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Consulta cancelada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada", content = @Content)
    })
    public ResponseEntity<Void> cancelar(
            @Parameter(description = "ID da consulta", example = "1") @PathVariable Long id) {

        consultaService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/proxima")
    @Operation(summary = "Busca próxima consulta para a Alexa", description = "Retorna a próxima consulta agendada para o dentista vinculado ou autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Próxima consulta retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = AlexaConsultaDto.class)))
    })
    public ResponseEntity<AlexaConsultaDto> obterProximaConsulta(
            @RequestHeader(value = "X-Alexa-User-Id", required = false) String alexaUserId) {
        Long usuarioAuthId = obterUsuarioIdAutenticado();
        return ResponseEntity.ok(alexaService.obterProximaConsulta(alexaUserId, usuarioAuthId));
    }

    @GetMapping("/hoje")
    @Operation(summary = "Busca resumo das consultas de hoje para a Alexa", description = "Retorna o total e os horários das consultas de hoje do dentista.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo do dia retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = AlexaResumoDiaDto.class)))
    })
    public ResponseEntity<AlexaResumoDiaDto> obterConsultasHoje(
            @RequestHeader(value = "X-Alexa-User-Id", required = false) String alexaUserId) {
        Long usuarioAuthId = obterUsuarioIdAutenticado();
        return ResponseEntity.ok(alexaService.obterConsultasHoje(alexaUserId, usuarioAuthId));
    }

    @GetMapping("/proxima/anamnese")
    @Operation(summary = "Busca alertas de anamnese do próximo paciente", description = "Retorna alergias e condições médicas críticas do próximo paciente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alertas retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = AlexaAnamneseAlertaDto.class)))
    })
    public ResponseEntity<AlexaAnamneseAlertaDto> obterAlertaAnamneseProxima(
            @RequestHeader(value = "X-Alexa-User-Id", required = false) String alexaUserId) {
        Long usuarioAuthId = obterUsuarioIdAutenticado();
        return ResponseEntity.ok(alexaService.obterAlertaAnamneseProxima(alexaUserId, usuarioAuthId));
    }

    private Long obterUsuarioIdAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank() && !"anonymousUser".equalsIgnoreCase(auth.getName())) {
            return usuarioRepository.findByEmail(auth.getName())
                    .map(Usuario::getIdUsuario)
                    .orElse(null);
        }
        return null;
    }
}
