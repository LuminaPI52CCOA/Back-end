package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resposta contendo o status de conexão da Alexa com o dentista")
public class AlexaStatusResponse {

    @Schema(description = "Indica se o dentista possui um dispositivo Alexa conectado", example = "true")
    private Boolean conectado;

    @Schema(description = "Identificador único do dispositivo Alexa", example = "amzn1.ask.account.TESTE")
    private String alexaUserId;

    @Schema(description = "Endpoint da API da Alexa", example = "https://api.amazonalexa.com")
    private String apiEndpoint;

    @Schema(description = "Data e hora em que a conexão foi realizada", example = "2026-09-24T14:30:00")
    private LocalDateTime vinculadoEm;

    @Schema(description = "Nome do dentista associado", example = "Dr. Ricardo Alves")
    private String dentistaNome;

    public AlexaStatusResponse() {
    }

    public AlexaStatusResponse(Boolean conectado, String alexaUserId, String apiEndpoint, LocalDateTime vinculadoEm, String dentistaNome) {
        this.conectado = conectado;
        this.alexaUserId = alexaUserId;
        this.apiEndpoint = apiEndpoint;
        this.vinculadoEm = vinculadoEm;
        this.dentistaNome = dentistaNome;
    }

    public Boolean getConectado() {
        return conectado;
    }

    public void setConectado(Boolean conectado) {
        this.conectado = conectado;
    }

    public String getAlexaUserId() {
        return alexaUserId;
    }

    public void setAlexaUserId(String alexaUserId) {
        this.alexaUserId = alexaUserId;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public LocalDateTime getVinculadoEm() {
        return vinculadoEm;
    }

    public void setVinculadoEm(LocalDateTime vinculadoEm) {
        this.vinculadoEm = vinculadoEm;
    }

    public String getDentistaNome() {
        return dentistaNome;
    }

    public void setDentistaNome(String dentistaNome) {
        this.dentistaNome = dentistaNome;
    }
}
