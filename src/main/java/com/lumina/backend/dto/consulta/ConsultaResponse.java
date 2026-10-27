package com.lumina.backend.dto.consulta;

import com.lumina.backend.dto.cliente.ClienteResponse;
import com.lumina.backend.dto.especialidade.EspecialidadeResponse;
import com.lumina.backend.dto.usuario.UsuarioResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "Dados de resposta de consulta")
public class ConsultaResponse {
    @Schema(description = "Identificador da consulta", example = "1", type = "integer", format = "int64")
    private Long id;
    @Schema(description = "Cliente vinculado a consulta")
    private ClienteResponse cliente;
    @Schema(description = "Usuario (dentista) responsavel pela consulta. O campo senha nunca e preenchido nesta resposta.")
    private UsuarioResponse usuario;
    @Schema(description = "Data da consulta", example = "2026-05-15", type = "string", format = "date")
    private LocalDate data;
    @Schema(description = "Horario de inicio da consulta", example = "09:00:00", type = "string")
    private LocalTime horarioInicio;
    @Schema(description = "Horario de termino da consulta", example = "09:30:00", type = "string")
    private LocalTime horarioFim;
    @Schema(description = "Duracao calculada pelo servidor, em minutos", example = "30", type = "integer")
    private Integer duracaoMinutos;
    @Schema(description = "Status da consulta", example = "AGENDADA", allowableValues = {"AGENDADA", "CONFIRMADA", "CANCELADA", "CONCLUIDA"})
    private String status;
    @Schema(description = "Especialidade da consulta (nula em registros anteriores a migracao)", nullable = true)
    private EspecialidadeResponse especialidade;
    @Schema(description = "Observacoes adicionais", example = "Paciente relata sensibilidade ao frio.", nullable = true)
    private String observacoes;
    @ArraySchema(arraySchema = @Schema(description = "Dentes selecionados no odontograma, em ordem crescente"),
            schema = @Schema(type = "integer", example = "16"))
    private List<Integer> dentes;
    @Schema(description = "Agrupamento escolhido no odontograma", example = "Primeiro Molar", nullable = true)
    private String tipoDente;
    @Schema(description = "Indica se o odontograma foi pulado", example = "false")
    private Boolean pularOdontograma;

    public ConsultaResponse() {
    }

    public ConsultaResponse(Long id,
                            ClienteResponse cliente,
                            UsuarioResponse usuario,
                            LocalDate data,
                            LocalTime horarioInicio,
                            LocalTime horarioFim) {
        this.id = id;
        this.cliente = cliente;
        this.usuario = usuario;
        this.data = data;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClienteResponse getCliente() {
        return cliente;
    }

    public void setCliente(ClienteResponse cliente) {
        this.cliente = cliente;
    }

    public UsuarioResponse getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioResponse usuario) {
        this.usuario = usuario;
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

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public EspecialidadeResponse getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(EspecialidadeResponse especialidade) {
        this.especialidade = especialidade;
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
