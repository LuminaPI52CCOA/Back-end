package com.lumina.backend.controller;

import com.lumina.backend.dto.anamnese.AnamneseMapper;
import com.lumina.backend.dto.anamnese.AnamneseResponse;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.service.anamnese.AnamneseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/anamneses")
@PreAuthorize("hasAnyRole('ADMIN', 'DENTISTA')")
public class AnamneseController {

    private final AnamneseService service;

    public AnamneseController(AnamneseService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Anamnese> upload(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) Long clienteId
    ) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuario = (auth != null) ? auth.getName() : "ANONYMOUS";
        log.info("AUDIT: Usuario [{}] realizou upload de anamnese medica para o cliente ID [{}]", usuario, clienteId);

        Anamnese anamnese = (clienteId != null)
                ? service.processImage(file, clienteId)
                : service.processImage(file);
        return ResponseEntity.ok(anamnese);
    }

    public ResponseEntity<Anamnese> upload(MultipartFile file) throws IOException {
        return upload(file, null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnamneseResponse> buscarPorId(@PathVariable Integer id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuario = (auth != null) ? auth.getName() : "ANONYMOUS";
        log.info("AUDIT: Usuario [{}] consultou o prontuario de anamnese ID [{}]", usuario, id);

        Anamnese anamnese = service.buscarPorId(id);
        AnamneseResponse response = AnamneseMapper.toDto(anamnese);
        return ResponseEntity.ok(response);
    }
}
