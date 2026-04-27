package com.lumina.backend.controller;

import com.lumina.backend.dto.consulta.ConsultaMapper;
import com.lumina.backend.dto.consulta.ConsultaRequest;
import com.lumina.backend.dto.consulta.ConsultaResponse;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.service.consulta.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    public ResponseEntity<List<ConsultaResponse>> listar() {
        return ResponseEntity.status(200).body(ConsultaMapper.toResponse(consultaService.listar()));
    }

    @PostMapping
    public ResponseEntity<ConsultaResponse> cadastrar(@Valid @RequestBody ConsultaRequest consulta) {
        return ResponseEntity.status(201).body(ConsultaMapper.toResponse(consultaService.cadastrar(consulta)));
    }

    @GetMapping("/{id")
    public ResponseEntity<ConsultaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.status(200).body(ConsultaMapper.toResponse(consultaService.buscarPorId(id)));
    }
}
