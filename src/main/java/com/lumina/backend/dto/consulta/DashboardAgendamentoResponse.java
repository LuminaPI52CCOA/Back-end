package com.lumina.backend.dto.consulta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Dados consolidados do dashboard de agendamento para o periodo selecionado")
public record DashboardAgendamentoResponse(
        @Schema(description = "Periodo aplicado (7d, 30d, 90d, ano)", example = "30d")
        String periodo,

        @Schema(description = "Data inicial considerada no periodo", example = "2026-09-30")
        LocalDate dataInicio,

        @Schema(description = "Data final considerada no periodo", example = "2026-10-29")
        LocalDate dataFim,

        @Schema(description = "Total de consultas realizadas (concluidas) no periodo", example = "148")
        long consultasRealizadas,

        @Schema(description = "Total de consultas realizadas no periodo anterior equivalente", example = "132")
        long consultasRealizadasPeriodoAnterior,

        @Schema(description = "Variacao percentual de consultas realizadas em relacao ao periodo anterior", example = "12.1")
        double variacaoConsultasPercentual,

        @Schema(description = "Total de novos pacientes atendidos pela primeira vez no periodo", example = "34")
        long novosPacientes,

        @Schema(description = "Total de novos pacientes no periodo anterior equivalente", example = "26")
        long novosPacientesPeriodoAnterior,

        @Schema(description = "Taxa media de ocupacao da agenda no periodo (0 a 100)", example = "89.2")
        double taxaOcupacao,

        @Schema(description = "Serie do grafico de volume de atendimentos")
        List<PontoGrafico> graficoVolume,

        @Schema(description = "Serie do grafico de taxa de confirmacao (%)")
        List<PontoGrafico> graficoConfirmacao,

        @Schema(description = "Distribuicao de atendimentos por convenio no periodo")
        List<ItemDistribuicao> graficoConvenio,

        @Schema(description = "Distribuicao de consultas por status no periodo")
        List<ItemDistribuicao> graficoStatus
) {
    @Schema(description = "Ponto da serie temporal do dashboard")
    public record PontoGrafico(
            @Schema(description = "Rotulo do eixo X (mes ou periodo)", example = "Out")
            String mes,

            @Schema(description = "Valor numerico do ponto", example = "148")
            double valor
    ) {
    }

    @Schema(description = "Item de distribuicao categorica (convenio ou status)")
    public record ItemDistribuicao(
            @Schema(description = "Nome da categoria", example = "Particular")
            String categoria,

            @Schema(description = "Quantidade de consultas", example = "60")
            long quantidade,

            @Schema(description = "Percentual sobre o total (0 a 100)", example = "33.0")
            double percentual
    ) {
    }
}
