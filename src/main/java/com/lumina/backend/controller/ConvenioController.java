package com.lumina.backend.controller;

import com.lumina.backend.dto.convenio.ConvenioMapper;
import com.lumina.backend.dto.convenio.ConvenioRequest;
import com.lumina.backend.dto.convenio.ConvenioResponse;
import com.lumina.backend.service.Convenio.ConvenioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/convenios")
public class ConvenioController {

    private final ConvenioService convenioService;

    public ConvenioController(ConvenioService convenioService) {
        this.convenioService = convenioService;
    }

    @GetMapping
    public ResponseEntity<List<ConvenioResponse>> listar() {
        return ResponseEntity.status(200).body(ConvenioMapper.toResponse(convenioService.listar()));
    }

    @PostMapping
    public ResponseEntity<ConvenioResponse> cadastrar(
            @Valid @RequestBody ConvenioRequest convenioRequest) {
        return ResponseEntity.status(201).body(ConvenioMapper.toResponse(convenioService.cadastrar(convenioRequest)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConvenioResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ConvenioRequest convenioRequest) {

        return ResponseEntity.status(200).body(ConvenioMapper.toResponse(convenioService.atualizar(id, convenioRequest)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        convenioService.deletar(id);
        return ResponseEntity.status(204).build();
    }


}
