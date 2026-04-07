package com.lumina.backend.controller;

import com.lumina.backend.model.Perfil;
import com.lumina.backend.service.Perfil.PerfilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/perfis")
public class PerfilController {

    private final PerfilService service;


    public PerfilController(PerfilService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Perfil>> listar(){
        if(service.listar().isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(service.listar());
    }

}
