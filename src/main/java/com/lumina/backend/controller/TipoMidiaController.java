package com.lumina.backend.controller;

import com.lumina.backend.dto.midia.TipoMidiaRequest;
import com.lumina.backend.dto.midia.TipoMidiaResponse;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.midia.TipoMidiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tipos-midia")
@PreAuthorize("hasAnyRole('ADMIN', 'DENTISTA')")
@Tag(name = "Tipos de Mídia", description = "Endpoints para gerenciamento de categorias de fotos e exames clínicos")
public class TipoMidiaController {

    private final TipoMidiaService tipoMidiaService;
    private final UsuarioRepository usuarioRepository;

    public TipoMidiaController(TipoMidiaService tipoMidiaService, UsuarioRepository usuarioRepository) {
        this.tipoMidiaService = tipoMidiaService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @Operation(summary = "Lista tipos de mídia disponíveis", description = "Retorna os tipos de mídia padrões globais mais os tipos personalizados criados pelo dentista autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos retornada com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhum tipo de mídia encontrado")
    })
    public ResponseEntity<List<TipoMidiaResponse>> listar() {
        Usuario usuario = getUsuarioAutenticado();
        List<TipoMidiaResponse> tipos = tipoMidiaService.listarDisponiveis(usuario);
        if (tipos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tipos);
    }

    @PostMapping
    @Operation(summary = "Cadastra novo tipo de mídia personalizado", description = "Cadastra uma categoria personalizada vinculada ao dentista autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo de mídia cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos informados")
    })
    public ResponseEntity<TipoMidiaResponse> cadastrar(@org.springframework.web.bind.annotation.RequestBody @Valid TipoMidiaRequest request) {
        Usuario usuario = getUsuarioAutenticado();
        TipoMidiaResponse response = tipoMidiaService.criarPersonalizado(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return usuarioRepository.findByEmail(auth.getName()).orElse(null);
        }
        return null;
    }
}
