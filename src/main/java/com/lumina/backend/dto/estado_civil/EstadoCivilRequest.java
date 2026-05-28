package com.lumina.backend.dto.estado_civil;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class EstadoCivilRequest {
    private Integer idEstadoCivil;
    @NotBlank
    private String descricao;

    public EstadoCivilRequest(){}

    public EstadoCivilRequest(Integer idEstadoCivil, String descricao) {
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
