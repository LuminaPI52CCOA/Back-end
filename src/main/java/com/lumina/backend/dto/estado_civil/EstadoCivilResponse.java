package com.lumina.backend.dto.estado_civil;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de resposta de estado civil")
public class EstadoCivilResponse {

    @Schema(description = "ID do estado civil", example = "1", type = "integer", format = "int32")
    private Integer idEstadoCivil;

    @Schema(description = "Descrição do estado civil", example = "Solteiro(a)", type = "string")
    private String descricao;

    public EstadoCivilResponse(){}

    public EstadoCivilResponse(Integer idEstadoCivil, String descricao) {
        this.idEstadoCivil = idEstadoCivil;
        this.descricao = descricao;
    }

    public Integer getIdEstadoCivil() {
        return idEstadoCivil;
    }

    public void setIdEstadoCivil(Integer idEstadoCivil) {
        this.idEstadoCivil = idEstadoCivil;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
