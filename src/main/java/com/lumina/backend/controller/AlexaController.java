package com.lumina.backend.controller;

import com.lumina.backend.dto.alexa.AlexaGerarPinResponse;
import com.lumina.backend.dto.alexa.AlexaStatusResponse;
import com.lumina.backend.dto.alexa.AlexaVincularRequest;
import com.lumina.backend.dto.alexa.AlexaVincularResponse;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.alexa.AlexaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/alexa")
@Tag(name = "Alexa Integration", description = "Endpoints para gerenciamento de pareamento e integração de voz com a Alexa")
public class AlexaController {

    private final AlexaService alexaService;
    private final UsuarioRepository usuarioRepository;

    public AlexaController(AlexaService alexaService, UsuarioRepository usuarioRepository) {
        this.alexaService = alexaService;
        this.usuarioRepository = usuarioRepository;
    }

    private Usuario resolverUsuarioLogado(Authentication auth) {
        String email = (auth != null) ? auth.getName() : null;
        if (email != null && !email.isBlank()) {
            return usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new EntidadeNaoEncontrada("Usuário autenticado não encontrado"));
        }
        throw new IllegalStateException("Sessão não identificada");
    }

    private void validarAcesso(Authentication auth, Usuario usuarioLogado, Long targetUserId) {
        boolean isAdmin = auth != null && auth.getAuthorities() != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!isAdmin && !usuarioLogado.getIdUsuario().equals(targetUserId)) {
            throw new AccessDeniedException("Dentista só pode gerenciar a própria integração Alexa");
        }
    }

    @PostMapping("/gerar-pin")
    @PreAuthorize("hasAnyRole('ADMIN', 'DENTISTA')")
    @Operation(
            summary = "Gera PIN de pareamento para a Alexa",
            description = "Gera um código temporário de 6 dígitos (válido por 10 minutos) para vincular o dispositivo Amazon Echo à conta do dentista."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PIN gerado com sucesso",
                    content = @Content(schema = @Schema(implementation = AlexaGerarPinResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dentista não encontrado", content = @Content)
    })
    public ResponseEntity<AlexaGerarPinResponse> gerarPin(
            @RequestParam(required = false) Long usuarioId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogado = resolverUsuarioLogado(auth);
        Long targetUserId = (usuarioId != null) ? usuarioId : usuarioLogado.getIdUsuario();
        validarAcesso(auth, usuarioLogado, targetUserId);

        AlexaGerarPinResponse response = alexaService.gerarPin(targetUserId);
        log.info("AUDIT: Usuario [{}] (ID: {}) gerou PIN de pareamento para o dentista ID [{}]",
                usuarioLogado.getEmail(), usuarioLogado.getIdUsuario(), targetUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DENTISTA')")
    @Operation(
            summary = "Consulta status de integração com a Alexa",
            description = "Retorna se o dentista possui um dispositivo Alexa vinculado, dados de conexão e nome do dentista."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status consultado com sucesso",
                    content = @Content(schema = @Schema(implementation = AlexaStatusResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dentista não encontrado", content = @Content)
    })
    public ResponseEntity<AlexaStatusResponse> obterStatus(
            @RequestParam(required = false) Long usuarioId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogado = resolverUsuarioLogado(auth);
        Long targetUserId = (usuarioId != null) ? usuarioId : usuarioLogado.getIdUsuario();
        validarAcesso(auth, usuarioLogado, targetUserId);

        AlexaStatusResponse response = alexaService.obterStatus(targetUserId);
        log.info("AUDIT: Usuario [{}] consultou status da Alexa do dentista ID [{}]",
                usuarioLogado.getEmail(), targetUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/desconectar")
    @PreAuthorize("hasAnyRole('ADMIN', 'DENTISTA')")
    @Operation(
            summary = "Desconecta dispositivo Alexa da conta do dentista",
            description = "Desativa o vínculo ativo do dentista com o dispositivo Amazon Echo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Dispositivo desconectado com sucesso", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Vínculo ou dentista não encontrado", content = @Content)
    })
    public ResponseEntity<Void> desconectar(
            @RequestParam(required = false) Long usuarioId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogado = resolverUsuarioLogado(auth);
        Long targetUserId = (usuarioId != null) ? usuarioId : usuarioLogado.getIdUsuario();
        validarAcesso(auth, usuarioLogado, targetUserId);

        String alexaUserId = alexaService.desconectar(targetUserId);
        log.info("AUDIT: Usuario [{}] desvinculou o dispositivo Alexa [{}] do dentista ID [{}]",
                usuarioLogado.getEmail(), alexaUserId, targetUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/vincular")
    @Operation(
            summary = "Vincula dispositivo Alexa à conta do dentista",
            description = "Recebe o código PIN gerado pelo painel web e o alexaUserId capturado pela Lambda para estabelecer o vínculo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vínculo realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AlexaVincularResponse.class))),
            @ApiResponse(responseCode = "400", description = "Código expirado ou inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Código não encontrado", content = @Content)
    })
    public ResponseEntity<AlexaVincularResponse> vincular(
            @Valid @RequestBody AlexaVincularRequest request) {

        log.info("ALEXA CONTROLLER: Recebida solicitação de vínculo para alexaUserId [{}]", request.getAlexaUserId());
        AlexaVincularResponse response = alexaService.vincular(request);
        return ResponseEntity.ok(response);
    }
}
