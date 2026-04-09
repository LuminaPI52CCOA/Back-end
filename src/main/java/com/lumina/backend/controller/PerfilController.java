package com.lumina.backend.controller;

import com.lumina.backend.model.Perfil;
import com.lumina.backend.service.Perfil.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/perfis")
@Tag(name = "Perfis", description = "Endpoints para consulta de perfis de acesso do sistema Lumina")
public class PerfilController {

    private final PerfilService service;


    public PerfilController(PerfilService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Lista todos os perfis",
            description = "Retorna a lista de perfis cadastrados para classificacao de usuarios."
    )
    public ResponseEntity<List<Perfil>> listar(){
        if(service.listar().isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(service.listar());
    }

}
