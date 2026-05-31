package com.lumina.backend.dto.anamnese;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OcrRespostaDTO {

    public record DetalheRespostaDTO(
            @JsonProperty("resposta_sim") Boolean respostaSim,
            String detalhes
    ) {
        @Override
    public Boolean respostaSim() {
        return respostaSim != null ? respostaSim : false;
    }}

    public record PerguntasAnamneseDTO(
            @JsonProperty("q1_tratamento_medico") DetalheRespostaDTO q1,
            @JsonProperty("q2_dores_cabeca_face_ouvido_articulacao") DetalheRespostaDTO q2,
            @JsonProperty("q3_alergia_medicamento") DetalheRespostaDTO q3,
            @JsonProperty("q4_reacao_anestesia_local") DetalheRespostaDTO q4,
            @JsonProperty("q5_sensibilidade_dentes") DetalheRespostaDTO q5,
            @JsonProperty("q6_range_dentes_apertamento") DetalheRespostaDTO q6,
            @JsonProperty("q7_gengiva_sangra_frequencia") DetalheRespostaDTO q7,
            @JsonProperty("q8_tem_algum_habito") DetalheRespostaDTO q8,
            @JsonProperty("q9_diabetico_ou_historico_familiar") DetalheRespostaDTO q9,
            @JsonProperty("q10_sangramento_excessivo_cortes") DetalheRespostaDTO q10,
            @JsonProperty("q11_problema_cardiaco") DetalheRespostaDTO q11,
            @JsonProperty("q12_pressao_arterial_normal") DetalheRespostaDTO q12,
            @JsonProperty("q13_desmaio_ataques_epilepsia_convulsao") DetalheRespostaDTO q13,
            @JsonProperty("q14_esta_gravida") DetalheRespostaDTO q14
    ) {}

    public record FichaAnamneseWrapperDTO(
            @JsonProperty("ficha_anamnese") FichaAnamneseDTO fichaAnamnese
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FichaAnamneseDTO(
            PerguntasAnamneseDTO perguntas
    ) {}
}