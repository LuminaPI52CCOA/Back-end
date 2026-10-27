package com.lumina.backend.dto.consulta;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "Dados de entrada para cadastro de consulta. A duracao e calculada no servidor a partir de "
        + "horarioInicio/horarioFim e deve ser de 15, 30, 45 ou 60 minutos, dentro da jornada 08:00-18:00.")
public class ConsultaRequest {

    @NotNull
    @Positive
    @Schema(description = "ID do cliente (paciente) da consulta", example = "1", type = "integer", format = "int64")
    private Long idCliente;

    @NotNull
    @Positive
    @Schema(description = "ID do usuario dentista responsavel pela consulta", example = "2", type = "integer", format = "int64")
    private Long idUsuario;

    @NotNull
    @Positive
    @Schema(description = "ID da especialidade (GET /especialidades)", example = "1", type = "integer", format = "int32")
    private Integer idEspecialidade;

    @NotNull
    @Schema(description = "Data da consulta. Hoje e aceito se o inicio ainda estiver no futuro (fuso America/Sao_Paulo).",
            example = "2026-11-03", type = "string", format = "date")
    private LocalDate data;

    @NotNull
    @Schema(description = "Horario de inicio (multiplo de 15 minutos)", example = "09:00:00", type = "string")
    private LocalTime horarioInicio;

    @NotNull
    @Schema(description = "Horario de termino, no mesmo dia e ate 18:00", example = "09:30:00", type = "string")
    private LocalTime horarioFim;

    @Size(max = 500)
    @Schema(description = "Observacoes adicionais", example = "Paciente relata sensibilidade ao frio.", maxLength = 500)
    private String observacoes;

    @Size(max = 32)
    @ArraySchema(arraySchema = @Schema(description = "Dentes selecionados no odontograma (FDI 11-18, 21-28, 31-38, 41-48), sem repeticao. "
            + "Ignorado quando pularOdontograma = true."),
            schema = @Schema(type = "integer", example = "16"))
    private List<Integer> dentes;

    @Size(max = 45)
    @Schema(description = "Agrupamento escolhido no odontograma. Valores aceitos: Incisivo Central, Incisivo Lateral, Canino, "
            + "Primeiro Pré-molar, Segundo Pré-molar, Primeiro Molar, Segundo Molar, Terceiro Molar (Siso).",
            example = "Primeiro Molar", nullable = true)
    private String tipoDente;

    @Schema(description = "Quando true, dentes e tipoDente sao descartados", example = "false", defaultValue = "false")
    private Boolean pularOdontograma;

    public ConsultaRequest() {
    }

    public ConsultaRequest(Long idCliente, Long idUsuario, LocalDate data, LocalTime horarioInicio, LocalTime horarioFim) {
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.data = data;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
    }

    public ConsultaRequest(Long idCliente, Long idUsuario, Integer idEspecialidade, LocalDate data,
                           LocalTime horarioInicio, LocalTime horarioFim, String observacoes,
                           List<Integer> dentes, String tipoDente, Boolean pularOdontograma) {
        this(idCliente, idUsuario, data, horarioInicio, horarioFim);
        this.idEspecialidade = idEspecialidade;
        this.observacoes = observacoes;
        this.dentes = dentes;
        this.tipoDente = tipoDente;
        this.pularOdontograma = pularOdontograma;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdEspecialidade() {
        return idEspecialidade;
    }

    public void setIdEspecialidade(Integer idEspecialidade) {
        this.idEspecialidade = idEspecialidade;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(LocalTime horarioFim) {
        this.horarioFim = horarioFim;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<Integer> getDentes() {
        return dentes;
    }

    public void setDentes(List<Integer> dentes) {
        this.dentes = dentes;
    }

    public String getTipoDente() {
        return tipoDente;
    }

    public void setTipoDente(String tipoDente) {
        this.tipoDente = tipoDente;
    }

    public Boolean getPularOdontograma() {
        return pularOdontograma;
    }

    public void setPularOdontograma(Boolean pularOdontograma) {
        this.pularOdontograma = pularOdontograma;
    }
}
