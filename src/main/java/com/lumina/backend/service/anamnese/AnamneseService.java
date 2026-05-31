package com.lumina.backend.service.anamnese;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.backend.dto.anamnese.AnamneseMapper;
import com.lumina.backend.dto.anamnese.OcrRespostaDTO;
import com.lumina.backend.exception.AnamneseVazio;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.repository.AnamneseRepository;
import com.lumina.backend.service.openIA.GeminiAIService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;
import software.amazon.awssdk.services.textract.model.Document;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class AnamneseService {

    private final AnamneseRepository repository;
    private final TextractClient textractClient;
    private final GeminiAIService geminiAIService;
    private AnamneseMapper anamneseMapper;

    public AnamneseService(AnamneseRepository repository, TextractClient textractClient, GeminiAIService geminiAIService,  AnamneseMapper anamneseMapper) {
        this.repository = repository;
    }

    public Anamnese buscarPorId(Integer id){
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Anamnese não encontrada!"));
    }
}
