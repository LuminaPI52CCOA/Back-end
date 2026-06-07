package com.lumina.backend.service.openIA;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiAIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient;

    public GeminiAIService(RestClient restClient){
        this.restClient = restClient;
    }

    public String transformarEmJson(
            String ocr
    ){

        log.trace("usando IA para Formatação do Retorno do OCR");

        String prompt = """
                Você é um extrator de dados médicos de alta precisão. Sua única tarefa é converter o texto de um OCR (que pode conter erros de leitura e quebras de linha) em um JSON estrito e estruturado.
                
                REGRAS OBRIGATÓRIAS:
                - Retorne APENAS o JSON puro. Não inclua blocos de código com markdown (como ```json), sem introduções e sem explicações.
                - Detecte marcações de seleção (ex: "(X)", "[X]", "( x )", "[x]", "(*)") ao lado de "Sim" ou "Não" para definir o valor booleano.
                - Regra de Booleano: "Sim" marcado = true | "Não" marcado = false | Nenhuma marcação identificada = null.
                - Regra de Detalhes: Se o campo "detalhes" não tiver texto adicional no OCR, defina o valor dele como null (mantenha a chave no JSON).
                - NÃO invente ou assuma dados. Se a informação não estiver clara no OCR, use null.
                - A estrutura abaixo serve apenas como modelo de campos — substitua os valores internos pelos dados extraídos.
                
                ESTRUTURA ESPERADA:
                {
                  "ficha_anamnese": {
                    "metadados": {
                      "data_preenchimento": "<data no formato DD/MM/AAAA ou null>"
                    },
                    "perguntas": {
                      "q1_tratamento_medico":                      { "resposta_sim": "<bool>", "detalhes": "<texto ou null>" },
                      "q2_dores_cabeca_face_ouvido_articulacao":   { "resposta_sim": "<bool>" },
                      "q3_alergia_medicamento":                    { "resposta_sim": "<bool>", "detalhes": "<texto ou null>" },
                      "q4_reacao_anestesia_local":                 { "resposta_sim": "<bool>" },
                      "q5_sensibilidade_dentes":                   { "resposta_sim": "<bool>" },
                      "q6_range_dentes_apertamento":               { "resposta_sim": "<bool>" },
                      "q7_gengiva_sangra_frequencia":              { "resposta_sim": "<bool>" },
                      "q8_tem_algum_habito":                       { "resposta_sim": "<bool>", "detalhes": "<texto ou null>" },
                      "q9_diabetico_ou_historico_familiar":        { "resposta_sim": "<bool>" },
                      "q10_sangramento_excessivo_cortes":          { "resposta_sim": "<bool>" },
                      "q11_problema_cardiaco":                     { "resposta_sim": "<bool>", "detalhes": "<texto ou null>" },
                      "q12_pressao_arterial_normal":               { "resposta_sim": "<bool>" },
                      "q13_desmaio_ataques_epilepsia_convulsao":   { "resposta_sim": "<bool>" },
                      "q14_esta_gravida":                          { "resposta_sim": "<bool>" }
                    },
                    "declaracao_veracidade": "<bool>"
                  }
                }
                
                TEXTO OCR A PROCESSAR:
                %s
        """.formatted(ocr);

        Map<String,Object> body =
                Map.of(

                        "contents",

                        List.of(
                                Map.of(
                                        "parts",
                                        List.of(
                                                Map.of(
                                                        "text",
                                                        prompt
                                                )
                                        )
                                )
                        )
                );

        String resposta =
                restClient.post()

                        .uri(
                       "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key="
                               + apiKey)
                        .body(body)

                        .retrieve()

                        .body(String.class);

        return resposta;
    }

    public JsonNode limparJsonGemini(String responseJson) {

        log.trace("Limpando gemini JSON");

        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(responseJson);

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

            String textJson = textNode.asText();

            textJson = textJson
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```", "")
                    .trim();


            return mapper.readTree(textJson);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter resposta Gemini", e);
        }
    }
}