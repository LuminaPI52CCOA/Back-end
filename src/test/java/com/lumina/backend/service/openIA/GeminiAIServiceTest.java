package com.lumina.backend.service.openIA;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiAIServiceTest {

    @Mock
    private RestClient restClient;

    @InjectMocks
    private GeminiAIService geminiAIService;

    @Test
    @DisplayName("Deve limpar JSON do Gemini com markdown formatado")
    void deveLimparJsonComMarkdown() {
        String jsonComMarkdown = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "```json\\n{\\"ficha_anamnese\\": {\\"declaracao_veracidade\\": true}}\\n```"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        JsonNode resultado = geminiAIService.limparJsonGemini(jsonComMarkdown);

        assertNotNull(resultado);
        assertTrue(resultado.has("ficha_anamnese"));
        assertTrue(resultado.get("ficha_anamnese").get("declaracao_veracidade").asBoolean());
    }

    @Test
    @DisplayName("Deve lançar exceção quando resposta do Gemini for inválida")
    void deveLancarExcecaoQuandoJsonInvalido() {
        String jsonInvalido = "{\"candidates\": []}";

        assertThrows(RuntimeException.class, () -> geminiAIService.limparJsonGemini(jsonInvalido));
    }
}
