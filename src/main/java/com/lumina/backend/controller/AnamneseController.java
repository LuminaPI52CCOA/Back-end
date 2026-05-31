package com.lumina.backend.controller;

import com.lumina.backend.dto.anamnese.AnamneseMapper;
import com.lumina.backend.dto.anamnese.AnamneseResponse;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.service.anamnese.AnamneseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/anamneses")
public class AnamneseController {

    private final AnamneseService service;

    public AnamneseController(AnamneseService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Anamnese> upload(@RequestParam MultipartFile file
//            , @PathVariable Long id
    )
            throws IOException {
        Anamnese anamnese = service.processImage(file);
        return ResponseEntity.ok(anamnese);
    }
    @GetMapping("/{id}")
    public ResponseEntity<AnamneseResponse> buscarPorId(@PathVariable Integer id){
        Anamnese anamnese = service.buscarPorId(id);
        AnamneseResponse response = AnamneseMapper.toDto(anamnese);
        return ResponseEntity.ok(response);
    }
}
