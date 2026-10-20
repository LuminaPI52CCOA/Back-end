package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Alertas médicos críticos da anamnese para a Alexa")
public class AlexaAnamneseAlertaDto {

    @Schema(description = "Nome do paciente", example = "Carlos Silva")
    private String pacienteNome;

    @Schema(description = "Indica se o paciente possui restrições ou alertas médicos", example = "true")
    private boolean possuiRestricoes;

    @Schema(description = "Lista de alertas identificados", example = "[\"Alergia a Penicilina\", \"Hipertensão arterial\"]")
    private List<String> alertas;

    @Schema(description = "Texto otimizado para reprodução por voz na Alexa", example = "Atenção Doutor: O paciente Carlos Silva relatou alergia a Penicilina e histórico de hipertensão.")
    private String mensagemVoz;

    public AlexaAnamneseAlertaDto() {
    }

    public AlexaAnamneseAlertaDto(String pacienteNome, boolean possuiRestricoes, List<String> alertas, String mensagemVoz) {
        this.pacienteNome = pacienteNome;
        this.possuiRestricoes = possuiRestricoes;
        this.alertas = alertas;
        this.mensagemVoz = mensagemVoz;
    }

    public String getPacienteNome() {
        return pacienteNome;
    }

    public void setPacienteNome(String pacienteNome) {
        this.pacienteNome = pacienteNome;
    }

    public boolean isPossuiRestricoes() {
        return possuiRestricoes;
    }

    public void setPossuiRestricoes(boolean possuiRestricoes) {
        this.possuiRestricoes = possuiRestricoes;
    }

    public List<String> getAlertas() {
        return alertas;
    }

    public void setAlertas(List<String> alertas) {
        this.alertas = alertas;
    }

    public String getMensagemVoz() {
        return mensagemVoz;
    }

    public void setMensagemVoz(String mensagemVoz) {
        this.mensagemVoz = mensagemVoz;
    }
}
