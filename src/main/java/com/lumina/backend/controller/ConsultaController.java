package com.lumina.backend.controller;

import com.lumina.backend.dto.consulta.AlterarStatusConsultaRequest;
import com.lumina.backend.dto.consulta.ConsultaMapper;
import com.lumina.backend.dto.consulta.ConsultaRequest;
import com.lumina.backend.dto.consulta.ConsultaResponse;
import com.lumina.backend.dto.consulta.DashboardAgendamentoResponse;
import com.lumina.backend.dto.consulta.HorarioDisponivelResponse;
import com.lumina.backend.dto.consulta.ReagendamentoRequest;
import com.lumina.backend.exception.dto.ErroRespostaDto;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.service.consulta.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
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
    @Operation(summary = "Lista consultas", description = "Retorna as consultas cadastradas no sistema (ADMIN e RECEPCIONISTA). "
            + "Sem parametros retorna todas; com dataInicio/dataFim (inclusivos, ate 366 dias) filtra o periodo. "
            + "Consultas canceladas sao retornadas com status CANCELADA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consultas retornadas com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ConsultaResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Periodo invalido",
                    content = @Content(schema = @Schema(implementation = ErroRespostaDto.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissao", content = @Content)
    })
    public ResponseEntity<List<ConsultaResponse>> listar(
            @Parameter(description = "Data inicial (inclusiva)", example = "2026-11-02")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final (inclusiva)", example = "2026-11-08")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        if(!isRecepcionistaOuAdmin()){
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.status(200).body(ConsultaMapper.toResponse(consultaService.listar(dataInicio, dataFim)));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Obtem indicadores do dashboard de agendamento",
            description = "Retorna metricas consolidadas (consultas realizadas, novos pacientes, taxa de ocupacao) "
                    + "e series temporais (volume de atendimentos e taxa de confirmacao) para o periodo informado (7d, 30d, 90d, ano).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Indicadores retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = DashboardAgendamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Periodo invalido",
                    content = @Content(schema = @Schema(implementation = ErroRespostaDto.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissao", content = @Content)
    })
    public ResponseEntity<DashboardAgendamentoResponse> obterDashboard(
            @Parameter(description = "Periodo de analise (7d, 30d, 90d, ano)", example = "30d")
            @RequestParam(required = false, defaultValue = "30d") String periodo,
            @Parameter(description = "ID opcional do dentista para filtrar indicadores", example = "1")
            @RequestParam(required = false) Long idUsuario) {
        if (isApenasDentista() && idUsuario != null && !idUsuario.equals(obterUsuarioIdAutenticado())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(consultaService.obterDashboardAgendamento(periodo, idUsuario));
    }

    @GetMapping("/dentista/{id}")
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @Operation(summary = "Lista consultas do dentista", description = "Retorna as consultas do dentista informado. "
            + "Um DENTISTA so pode consultar a propria agenda. Aceita o mesmo filtro opcional de periodo de GET /consultas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consultas retornadas com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ConsultaResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Periodo invalido",
                    content = @Content(schema = @Schema(implementation = ErroRespostaDto.class))),
            @ApiResponse(responseCode = "403", description = "Dentista consultando agenda de outro dentista", content = @Content)
    })
    public ResponseEntity<List<ConsultaResponse>> listarConsultasDentista(
            @PathVariable Long id,
            @Parameter(description = "Data inicial (inclusiva)", example = "2026-11-02")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final (inclusiva)", example = "2026-11-08")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ){
        if(isApenasDentista() && !id.equals(obterUsuarioIdAutenticado())){
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.status(200).body(ConsultaMapper.toResponse(consultaService.listarPorDentista(id, dataInicio, dataFim)));
    }

    @GetMapping("/horarios-disponiveis")
    @Operation(summary = "Lista horarios disponiveis do dentista",
            description = "Retorna os intervalos livres (inicios a cada 15 minutos) com a duracao pedida, aplicando as mesmas "
                    + "regras do cadastro: jornada 08:00-18:00 (o termino nao passa de 18:00), horarios ja passados de hoje "
                    + "excluidos, duracoes de 15/30/45/60 minutos e conflito com consultas nao canceladas do dentista. "
                    + "Datas passadas retornam lista vazia. A disponibilidade e revalidada no POST/PUT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Intervalos disponiveis (lista possivelmente vazia)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = HorarioDisponivelResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Parametros ausentes/invalidos ou usuario que nao e dentista ativo",
                    content = @Content(schema = @Schema(implementation = ErroRespostaDto.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Dentista consultando agenda de outro dentista", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dentista nao encontrado",
                    content = @Content(schema = @Schema(implementation = ErroRespostaDto.class)))
    })
    public ResponseEntity<List<HorarioDisponivelResponse>> listarHorariosDisponiveis(
            @Parameter(description = "ID do dentista", example = "2", required = true)
            @RequestParam Long idUsuario,
            @Parameter(description = "Data desejada", example = "2026-11-03", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @Parameter(description = "Duracao em minutos (15, 30, 45 ou 60)", example = "30", required = true)
            @RequestParam Integer duracaoMinutos,
            @Parameter(description = "Consulta em reagendamento, desconsiderada na verificacao de conflitos", example = "10")
            @RequestParam(required = false) Long idConsulta) {
        if (isApenasDentista() && !idUsuario.equals(obterUsuarioIdAutenticado())) {
            return ResponseEntity.status(403).build();
        }

        List<HorarioDisponivelResponse> horarios = consultaService
                .listarHorariosDisponiveis(idUsuario, data, duracaoMinutos, idConsulta).stream()
                .map(intervalo -> new HorarioDisponivelResponse(intervalo.inicio(), intervalo.fim()))
                .toList();
        return ResponseEntity.ok(horarios);
    }

    @PostMapping
    @Operation(summary = "Cadastra consulta", description = "Cadastra uma consulta com status inicial AGENDADA (ADMIN e RECEPCIONISTA). "
            + "Valida paciente ativo, dentista ativo, especialidade, dentes do odontograma, duracao (15/30/45/60), jornada "
            + "08:00-18:00, data/horario futuros e conflito com outras consultas do dentista. Requisicoes simultaneas para o "
            + "mesmo dentista sao serializadas por bloqueio no banco.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consulta cadastrada com sucesso",
                    content = @Content(schema = @Schema(implementation = ConsultaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos (campos, duracao, jornada, horario passado, dentes, dentista/paciente inativo)",
                    content = @Content(schema = @Schema(implementation = ErroRespostaDto.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissao para cadastrar", content = @Content),
            @ApiResponse(responseCode = "404", description = "Paciente, dentista, especialidade ou dente inexistente",
                    content = @Content(schema = @Schema(implementation = ErroRespostaDto.class))),
            @ApiResponse(responseCode = "409", description = "Conflito com outra consulta do dentista",
                    content = @Content(schema = @Schema(implementation = ErroRespostaDto.class)))
    })
    public ResponseEntity<ConsultaResponse> cadastrar(
            @RequestBody(description = "Dados de cadastro da consulta", required = true,
                    content = @Content(schema = @Schema(implementation = ConsultaRequest.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody ConsultaRequest consulta) {
        if(!isRecepcionistaOuAdmin()){
            return ResponseEntity.status(403).build();
        }


        return ResponseEntity.status(201).body(ConsultaMapper.toResponse(consultaService.cadastrar(consulta)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca consulta por ID", description = "Retorna os dados de uma consulta pelo identificador informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta encontrada",
                    content = @Content(schema = @Schema(implementation = ConsultaResponse.class))),
            @ApiResponse(responseCode = "403", description = "Consulta de outro dentista", content = @Content),
            @ApiResponse(responseCode = "404", description = "Consulta nao encontrada", content = @Content)
    })
    public ResponseEntity<ConsultaResponse> buscarPorId(
            @Parameter(description = "ID da consulta", example = "1") @PathVariable Long id) {

        Consulta consulta = consultaService.buscarPorId(id);

        if(!podeAcessar(consulta)){
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.status(200).body(ConsultaMapper.toResponse(consulta));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reagendar consulta", description = "Altera data e horarios (e opcionalmente observacoes) preservando paciente, "
            + "dentista, especialidade e odontograma. Aplica as mesmas validacoes e o mesmo bloqueio do cadastro, ignorando a "
            + "propria consulta na busca de conflitos. Uma consulta CONFIRMADA volta para AGENDADA, o lembrete Alexa e "
            + "rearmado e a notificacao WhatsApp existente recebe novo horario e novo link de confirmacao.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta reagendada com sucesso",
                    content = @Content(schema = @Schema(implementation = ConsultaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para reagendamento ou consulta cancelada/concluida",
                    content = @Content(schema = @Schema(implementation = ErroRespostaDto.class))),
            @ApiResponse(responseCode = "403", description = "Consulta de outro dentista", content = @Content),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito com outra consulta do dentista",
                    content = @Content(schema = @Schema(implementation = ErroRespostaDto.class)))
    })
    public ResponseEntity<ConsultaResponse> reagendar(
            @Parameter(description = "ID da consulta", example = "1") @PathVariable Long id,
            @RequestBody(description = "Novos dados para a consulta", required = true,
                    content = @Content(schema = @Schema(implementation = ReagendamentoRequest.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody ReagendamentoRequest reagendamentoRequest) {

        Consulta consultaExistente = consultaService.buscarPorId(id);

        if(!podeAcessar(consultaExistente)){
            return ResponseEntity.status(403).build();
        }

        Consulta consultaAtualizada = consultaService.reagendar(id, reagendamentoRequest);
        return ResponseEntity.status(200).body(ConsultaMapper.toResponse(consultaAtualizada));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera status da consulta", description = "Transicoes aceitas: AGENDADA -> CONFIRMADA, CONFIRMADA -> AGENDADA, "
            + "AGENDADA/CONFIRMADA -> CONCLUIDA e AGENDADA/CONFIRMADA -> CANCELADA (mesmo efeito do DELETE). "
            + "Consultas CANCELADA/CONCLUIDA nao mudam de status. Repetir o status atual nao altera nada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status alterado",
                    content = @Content(schema = @Schema(implementation = ConsultaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Status invalido ou transicao nao permitida",
                    content = @Content(schema = @Schema(implementation = ErroRespostaDto.class))),
            @ApiResponse(responseCode = "403", description = "Consulta de outro dentista", content = @Content),
            @ApiResponse(responseCode = "404", description = "Consulta nao encontrada", content = @Content)
    })
    public ResponseEntity<ConsultaResponse> alterarStatus(
            @Parameter(description = "ID da consulta", example = "1") @PathVariable Long id,
            @RequestBody(description = "Novo status", required = true,
                    content = @Content(schema = @Schema(implementation = AlterarStatusConsultaRequest.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody AlterarStatusConsultaRequest request) {

        Consulta consulta = consultaService.buscarPorId(id);

        if(!podeAcessar(consulta)){
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(ConsultaMapper.toResponse(consultaService.alterarStatus(id, request.getStatus())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela consulta", description = "Cancelamento logico: a consulta passa a CANCELADA, permanece no historico "
            + "e libera o horario. A notificacao WhatsApp vinculada e marcada como CANCELADO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Consulta cancelada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Consulta concluida nao pode ser cancelada",
                    content = @Content(schema = @Schema(implementation = ErroRespostaDto.class))),
            @ApiResponse(responseCode = "403", description = "Consulta de outro dentista", content = @Content),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada", content = @Content)
    })
    public ResponseEntity<Void> cancelar(
            @Parameter(description = "ID da consulta", example = "1") @PathVariable Long id) {

        Consulta consulta = consultaService.buscarPorId(id);

        if(!podeAcessar(consulta)){
            return ResponseEntity.status(403).build();
        }

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

    private boolean isRecepcionistaOuAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECEPCIONISTA") || a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isApenasDentista() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DENTISTA"))
                && !isRecepcionistaOuAdmin();
    }

    /**
     * DENTISTA acessa e altera somente as proprias consultas; ADMIN e RECEPCIONISTA acessam todas.
     */
    private boolean podeAcessar(Consulta consulta) {
        if (!isApenasDentista()) {
            return true;
        }
        Long usuarioLogadoId = obterUsuarioIdAutenticado();
        return consulta.getUsuario() != null && consulta.getUsuario().getIdUsuario().equals(usuarioLogadoId);
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
