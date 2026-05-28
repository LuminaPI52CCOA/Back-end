package com.lumina.backend.dto.estado_civil;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class EstadoCivilResponse {

    private Integer idEstadoCivil;
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
