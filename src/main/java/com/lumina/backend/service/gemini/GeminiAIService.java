package com.lumina.backend.service.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiAIService {

    private static final String GEMINI_GENERATE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final String PROMPT_ANAMNESE_OCR = "prompts/anamnese_ocr.txt";
    private static final String PROMPT_ANAMNESE_VISION = "prompts/anamnese_vision.txt";
    private static final String OCR_PLACEHOLDER = "{{OCR_TEXT}}";

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String promptOcrTemplate;
    private final String promptVisionTemplate;

    public GeminiAIService(RestClient restClient) {
        this.restClient = restClient;
        this.objectMapper = new ObjectMapper();
        this.promptOcrTemplate = carregarPrompt(PROMPT_ANAMNESE_OCR);
        this.promptVisionTemplate = carregarPrompt(PROMPT_ANAMNESE_VISION);
    }

    public String transformarEmJson(String ocr) {
        log.trace("usando IA para Formatação do Retorno do OCR");

        String safeOcr = (ocr != null) ? ocr.replace("</dados_ocr_nao_confiaveis>", "[tag_sanitizada]") : "";
        String prompt = promptOcrTemplate.replace(OCR_PLACEHOLDER, safeOcr);

        return executarGeracaoConteudo(List.of(Map.of("text", prompt)));
    }

    public String transformarImagemEmJson(byte[] bytes, String mimeType) {
        log.trace("Usando Gemini Vision (gemini-2.5-flash) para extrair JSON diretamente da imagem/PDF da anamnese");

        String base64Data = Base64.getEncoder().encodeToString(bytes);

        return executarGeracaoConteudo(List.of(
                Map.of("text", promptVisionTemplate),
                Map.of(
                        "inline_data",
                        Map.of(
                                "mime_type", mimeType != null ? mimeType : "image/png",
                                "data", base64Data
                        )
                )
        ));
    }

    public JsonNode limparJsonGemini(String responseJson) {
        log.trace("Limpando gemini JSON");

        try {
            JsonNode root = objectMapper.readTree(responseJson);

            JsonNode textNode = root
                    .path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode() || textNode.isNull()) {
                throw new RuntimeException("Campo text não encontrado.");
            }

            String textJson = textNode.asText()
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            return objectMapper.readTree(textJson);

        } catch (JsonProcessingException | RuntimeException e) {
            throw new RuntimeException("Erro ao converter resposta Gemini", e);
        }
    }

    private String executarGeracaoConteudo(List<Map<String, Object>> parts) {
        Map<String, Object> body = Map.of("contents", List.of(Map.of("parts", parts)));

        return restClient.post()
                .uri(GEMINI_GENERATE_URL)
                .header("x-goog-api-key", apiKey)
                .body(body)
                .retrieve()
                .body(String.class);
    }

    private static String carregarPrompt(String caminho) {
        ClassPathResource resource = new ClassPathResource(caminho);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao carregar template de prompt: " + caminho, e);
        }
    }
}