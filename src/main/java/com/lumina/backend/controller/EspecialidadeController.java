package com.lumina.backend.controller;

import com.lumina.backend.dto.especialidade.EspecialidadeResponse;
import com.lumina.backend.model.Especialidade;
import com.lumina.backend.service.especialidade.EspecialidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/especialidades")
@PreAuthorize("hasAnyRole('ADMIN', 'DENTISTA', 'RECEPCIONISTA')")
@Tag(name = "Especialidades", description = "Especialidades odontologicas cadastradas")
public class EspecialidadeController {

    private final EspecialidadeService service;

    public EspecialidadeController(EspecialidadeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista especialidades", description = "Retorna as especialidades cadastradas, ordenadas por nome.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Especialidades retornadas com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EspecialidadeResponse.class)))),
            @ApiResponse(responseCode = "204", description = "Nenhuma especialidade cadastrada", content = @Content),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissao", content = @Content)
    })
    public ResponseEntity<List<EspecialidadeResponse>> listar() {
        List<Especialidade> especialidades = service.listar();
        if (especialidades.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(EspecialidadeResponse.of(especialidades));
    }
}
