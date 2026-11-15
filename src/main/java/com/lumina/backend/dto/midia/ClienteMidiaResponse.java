package com.lumina.backend.dto.midia;

import com.lumina.backend.model.ClienteMidia;

import java.time.format.DateTimeFormatter;

public record ClienteMidiaResponse(
        Long id,
        String nomeOriginal,
        String extensao,
        String mimeType,
        Long tamanhoBytes,
        String urlVisualizacao,
        TipoResumoDto tipo,
        ConsultaResumoDto consulta,
        DentistaResumoDto dentista,
        String legenda,
        String criadoEm
) {
    public static ClienteMidiaResponse fromEntity(ClienteMidia entity, String urlVisualizacao) {
        if (entity == null) {
            return null;
        }
        TipoResumoDto tipoDto = entity.getTipoMidia() != null
                ? new TipoResumoDto(entity.getTipoMidia().getIdTipoMidia(), entity.getTipoMidia().getNome())
                : null;
        ConsultaResumoDto consultaDto = entity.getConsulta() != null
                ? new ConsultaResumoDto(
                entity.getConsulta().getIdConsulta(),
                entity.getConsulta().getData() != null ? entity.getConsulta().getData().toString() : null)
                : null;
        DentistaResumoDto dentistaDto = entity.getUsuario() != null
                ? new DentistaResumoDto(entity.getUsuario().getIdUsuario(), entity.getUsuario().getNome())
                : null;
        String criadoEmFormatado = entity.getCriadoEm() != null
                ? entity.getCriadoEm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;

        return new ClienteMidiaResponse(
                entity.getIdClienteMidia(),
                entity.getNomeOriginal(),
                entity.getExtensao(),
                entity.getMimeType(),
                entity.getTamanhoBytes(),
                urlVisualizacao,
                tipoDto,
                consultaDto,
                dentistaDto,
                entity.getLegenda(),
                criadoEmFormatado
        );
    }

    public record TipoResumoDto(Long id, String nome) {
    }

    public record ConsultaResumoDto(Long id, String data) {
    }

    public record DentistaResumoDto(Long id, String nome) {
    }
}
