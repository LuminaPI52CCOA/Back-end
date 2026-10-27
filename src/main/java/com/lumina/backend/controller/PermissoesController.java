package com.lumina.backend.controller;

import com.lumina.backend.domain.usuario.Usuario;
import com.lumina.backend.dto.permissao.PermissaoMapper;
import com.lumina.backend.dto.permissao.PermissaoRequest;
import com.lumina.backend.dto.permissao.PermissaoResponse;
import com.lumina.backend.service.Usuario.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/permissoes")
@PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
@Tag(name = "Permissoes", description = "Endpoints exclusivos de Administrador e Recepcionista para controle de permissoes e perfis de acesso")
public class PermissoesController {

    private final UsuarioService usuarioService;

    public PermissoesController() {
        this.usuarioService = null;
    }

    @Autowired
    public PermissoesController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @Operation(
            summary = "Lista permissoes de todos os usuarios",
            description = "Retorna a lista de usuarios e seus respectivos niveis de acesso. Restrito a ADMIN e RECEPCIONISTA."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de permissoes retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = PermissaoResponse.class))),
            @ApiResponse(responseCode = "204", description = "Nenhum usuario encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado — permitido apenas para ADMIN e RECEPCIONISTA", content = @Content)
    })
    public ResponseEntity<List<PermissaoResponse>> listarPermissoes(
            @Parameter(description = "Filtrar pelo ID do perfil (1 = ADMIN, 2 = DENTISTA, 3 = RECEPCIONISTA)", example = "2")
            @RequestParam(required = false) Integer fkPerfil,
            @Parameter(description = "Filtrar por status ativo", example = "true")
            @RequestParam(required = false) Boolean ativo) {

        if (!possuiAcessoAdminOuRecepcionista()) {
            return ResponseEntity.status(403).build();
        }

        List<Usuario> usuarios = usuarioService.listar().stream()
                .filter(u -> fkPerfil == null || fkPerfil.equals(u.getFkPerfil()))
                .filter(u -> ativo == null || ativo.equals(u.getAtivo()))
                .toList();

        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(PermissaoMapper.toDto(usuarios));
    }

    @GetMapping("/dentistas")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @Operation(
            summary = "Lista dentistas para gerenciamento de permissoes",
            description = "Retorna os dentistas cadastrados e seus niveis de acesso ('todos', 'restrito', 'nenhum') para a tela de Controle de Permissoes."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de dentistas retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = PermissaoResponse.class))),
            @ApiResponse(responseCode = "204", description = "Nenhum dentista cadastrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado — permitido apenas para ADMIN e RECEPCIONISTA", content = @Content)
    })
    public ResponseEntity<List<PermissaoResponse>> listarDentistas(
            @Parameter(description = "Filtrar apenas dentistas ativos", example = "true")
            @RequestParam(required = false) Boolean apenasAtivos) {

        if (!possuiAcessoAdminOuRecepcionista()) {
            return ResponseEntity.status(403).build();
        }

        List<Usuario> dentistas = usuarioService.listarDentistas().stream()
                .filter(u -> !Boolean.TRUE.equals(apenasAtivos) || Boolean.TRUE.equals(u.getAtivo()))
                .toList();

        if (dentistas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(PermissaoMapper.toDto(dentistas));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @Operation(
            summary = "Busca permissoes de um usuario/dentista por ID",
            description = "Retorna o perfil e nivel de acesso atual do profissional correspondente ao ID informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissao encontrada",
                    content = @Content(schema = @Schema(implementation = PermissaoResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado — permitido apenas para ADMIN e RECEPCIONISTA", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content)
    })
    public ResponseEntity<PermissaoResponse> buscarPermissaoPorId(
            @Parameter(description = "ID do usuario/dentista", example = "1") @PathVariable Long id) {

        if (!possuiAcessoAdminOuRecepcionista()) {
            return ResponseEntity.status(403).build();
        }

        return usuarioService.buscarPorId(id)
                .map(PermissaoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @Operation(
            summary = "Atualiza permissoes e perfil de acesso do dentista/usuario",
            description = "Altera o nivel de acesso ('todos', 'restrito', 'nenhum'), perfil (fkPerfil) ou status ativo do usuario informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissoes atualizadas com sucesso",
                    content = @Content(schema = @Schema(implementation = PermissaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos para atualizacao de permissao", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado — permitido apenas para ADMIN e RECEPCIONISTA", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content)
    })
    public ResponseEntity<PermissaoResponse> atualizarPermissao(
            @Parameter(description = "ID do usuario/dentista", example = "1") @PathVariable Long id,
            @RequestBody(description = "Dados de permissao a serem atualizados", required = true,
                    content = @Content(schema = @Schema(implementation = PermissaoRequest.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody PermissaoRequest request) {

        if (!possuiAcessoAdminOuRecepcionista()) {
            return ResponseEntity.status(403).build();
        }

        String usuarioLogado = obterUsuarioAutenticado();
        Usuario usuarioAtualizado = usuarioService.atualizarPermissao(id, request);
        log.info("AUDIT: Usuario [{}] alterou permissoes do usuario ID [{}] (fkPerfil={}, ativo={})",
                usuarioLogado, id, usuarioAtualizado.getFkPerfil(), usuarioAtualizado.getAtivo());

        return ResponseEntity.ok(PermissaoMapper.toDto(usuarioAtualizado));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @Operation(
            summary = "Ajusta parcialmente o nivel de acesso do dentista/usuario",
            description = "Permite alterar o nivel de acesso ('todos', 'restrito', 'nenhum'), fkPerfil ou status ativo via PATCH."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nivel de acesso atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = PermissaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nivel de acesso invalido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado — permitido apenas para ADMIN e RECEPCIONISTA", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content)
    })
    public ResponseEntity<PermissaoResponse> ajustarNivelAcesso(
            @Parameter(description = "ID do usuario/dentista", example = "1") @PathVariable Long id,
            @RequestBody(description = "Nivel de acesso ou perfil a ser aplicado", required = true,
                    content = @Content(schema = @Schema(implementation = PermissaoRequest.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody PermissaoRequest request) {

        return atualizarPermissao(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @Operation(
            summary = "Ativa ou inativa o acesso de um usuario/dentista",
            description = "Altera o status de acesso (ativo = true/false) do usuario com base no ID informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status de acesso alterado com sucesso", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado — permitido apenas para ADMIN e RECEPCIONISTA", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content)
    })
    public ResponseEntity<Void> alterarStatusAcesso(
            @Parameter(description = "ID do usuario/dentista", example = "1") @PathVariable Long id,
            @Parameter(description = "Novo status de acesso (true = ativo, false = inativo)", example = "false")
            @RequestParam Boolean ativo) {

        if (!possuiAcessoAdminOuRecepcionista()) {
            return ResponseEntity.status(403).build();
        }

        String usuarioLogado = obterUsuarioAutenticado();
        usuarioService.deletar(ativo, id);
        log.info("AUDIT: Usuario [{}] alterou status de acesso do usuario ID [{}] para ativo={}",
                usuarioLogado, id, ativo);

        return ResponseEntity.noContent().build();
    }

    private boolean possuiAcessoAdminOuRecepcionista() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_RECEPCIONISTA".equals(a.getAuthority()));
    }

    private String obterUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "ANONYMOUS";
    }
}
