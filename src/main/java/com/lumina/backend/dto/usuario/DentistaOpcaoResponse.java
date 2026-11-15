package com.lumina.backend.dto.usuario;

import com.lumina.backend.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta minima de dentista ativo para seletores de agendamento")
public record DentistaOpcaoResponse(
        @Schema(description = "Identificador do usuario dentista", example = "2", type = "integer", format = "int64")
        Long idUsuario,
        @Schema(description = "Nome do dentista", example = "Dra. Ana Maria Souza")
        String nome,
        @Schema(description = "Registro CRO", example = "SP-654321")
        String cro
) {
    public static DentistaOpcaoResponse of(Usuario usuario) {
        return new DentistaOpcaoResponse(usuario.getIdUsuario(), usuario.getNome(), usuario.getCro());
    }
}
