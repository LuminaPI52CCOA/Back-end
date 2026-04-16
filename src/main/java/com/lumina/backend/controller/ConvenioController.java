package com.lumina.backend.controller;

import com.lumina.backend.dto.convenio.ConvenioResponse;
import com.lumina.backend.service.Convenio.ConvenioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/convenios")
public class ConvenioController {

    private final ConvenioService convenioService;

    public ConvenioController(ConvenioService convenioService) {
        this.convenioService = convenioService;
    }

    @GetMapping
    public ResponseEntity<ConvenioResponse> listar() {
        return ResponseEntity.status(200).body()
    }
}
