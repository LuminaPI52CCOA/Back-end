package com.lumina.backend.dto.consulta;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
@Schema(description = "Dados de entrada para cadastro de consulta. A duracao e calculada no servidor a partir de "
        + "horarioInicio/horarioFim e deve ser de 15, 30, 45 ou 60 minutos, dentro da jornada 08:00-18:00.")
public record ConsultaRequest(
        @NotNull
        @Positive
        @Schema(description = "ID do cliente (paciente) da consulta", example = "1", type = "integer", format = "int64")
        Long idCliente,

        @NotNull
        @Positive
        @Schema(description = "ID do usuario dentista responsavel pela consulta", example = "2", type = "integer", format = "int64")
        Long idUsuario,

        @NotNull
        @Positive
        @Schema(description = "ID da especialidade (GET /especialidades)", example = "1", type = "integer", format = "int32")
        Integer idEspecialidade,

        @NotNull
        @Schema(description = "Data da consulta. Hoje e aceito se o inicio ainda estiver no futuro (fuso America/Sao_Paulo).",
                example = "2026-11-03", type = "string", format = "date")
        LocalDate data,

        @NotNull
        @Schema(description = "Horario de inicio (multiplo de 15 minutos)", example = "09:00:00", type = "string")
        LocalTime horarioInicio,

        @NotNull
        @Schema(description = "Horario de termino, no mesmo dia e ate 18:00", example = "09:30:00", type = "string")
        LocalTime horarioFim,

        @Size(max = 500)
        @Schema(description = "Observacoes adicionais", example = "Paciente relata sensibilidade ao frio.", maxLength = 500)
        String observacoes,

        @Size(max = 32)
        @ArraySchema(arraySchema = @Schema(description = "Dentes selecionados no odontograma (FDI 11-18, 21-28, 31-38, 41-48), sem repeticao. "
                + "Ignorado quando pularOdontograma = true."),
                schema = @Schema(type = "integer", example = "16"))
        List<Integer> dentes,

        @Size(max = 45)
        @Schema(description = "Agrupamento escolhido no odontograma. Valores aceitos: Incisivo Central, Incisivo Lateral, Canino, "
                + "Primeiro Pré-molar, Segundo Pré-molar, Primeiro Molar, Segundo Molar, Terceiro Molar (Siso).",
                example = "Primeiro Molar", nullable = true)
        String tipoDente,

        @Schema(description = "Quando true, dentes e tipoDente sao descartados", example = "false", defaultValue = "false")
        Boolean pularOdontograma
) {
    public ConsultaRequest(Long idCliente, Long idUsuario, LocalDate data, LocalTime horarioInicio, LocalTime horarioFim) {
        this(idCliente, idUsuario, null, data, horarioInicio, horarioFim, null, null, null, null);
    }
}
