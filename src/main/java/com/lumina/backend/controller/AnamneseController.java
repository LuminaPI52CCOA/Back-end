package com.lumina.backend.controller;

import com.lumina.backend.dto.anamnese.AnamneseMapper;
import com.lumina.backend.dto.anamnese.AnamneseResponse;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.service.anamnese.AnamneseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/anamneses")
public class AnamneseController {

    private final AnamneseService service;

    public AnamneseController(AnamneseService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnamneseResponse> buscarPorId(@PathVariable Integer id){
        Anamnese anamnese = service.buscarPorId(id);
        AnamneseResponse response = AnamneseMapper.toDto(anamnese);
        return ResponseEntity.ok(response);
    }
}
