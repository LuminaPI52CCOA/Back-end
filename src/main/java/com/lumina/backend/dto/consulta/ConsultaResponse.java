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
public record ConsultaResponse(
        @Schema(description = "Identificador da consulta", example = "1", type = "integer", format = "int64")
        Long id,
        @Schema(description = "Cliente vinculado a consulta")
        ClienteResponse cliente,
        @Schema(description = "Usuario (dentista) responsavel pela consulta. O campo senha nunca e preenchido nesta resposta.")
        UsuarioResponse usuario,
        @Schema(description = "Data da consulta", example = "2026-05-15", type = "string", format = "date")
        LocalDate data,
        @Schema(description = "Horario de inicio da consulta", example = "09:00:00", type = "string")
        LocalTime horarioInicio,
        @Schema(description = "Horario de termino da consulta", example = "09:30:00", type = "string")
        LocalTime horarioFim,
        @Schema(description = "Duracao calculada pelo servidor, em minutos", example = "30", type = "integer")
        Integer duracaoMinutos,
        @Schema(description = "Status da consulta", example = "AGENDADA", allowableValues = {"AGENDADA", "CONFIRMADA", "CANCELADA", "CONCLUIDA"})
        String status,
        @Schema(description = "Especialidade da consulta (nula em registros anteriores a migracao)", nullable = true)
        EspecialidadeResponse especialidade,
        @Schema(description = "Observacoes adicionais", example = "Paciente relata sensibilidade ao frio.", nullable = true)
        String observacoes,
        @ArraySchema(arraySchema = @Schema(description = "Dentes selecionados no odontograma, em ordem crescente"),
                schema = @Schema(type = "integer", example = "16"))
        List<Integer> dentes,
        @Schema(description = "Agrupamento escolhido no odontograma", example = "Primeiro Molar", nullable = true)
        String tipoDente,
        @Schema(description = "Indica se o odontograma foi pulado", example = "false")
        Boolean pularOdontograma
) {
    public ConsultaResponse(Long id,
                            ClienteResponse cliente,
                            UsuarioResponse usuario,
                            LocalDate data,
                            LocalTime horarioInicio,
                            LocalTime horarioFim) {
        this(id, cliente, usuario, data, horarioInicio, horarioFim, null, null, null, null, null, null, null);
    }
}
