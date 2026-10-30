package com.lumina.backend.dto.especialidade;

import com.lumina.backend.model.Especialidade;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Especialidade odontologica cadastrada")
public class EspecialidadeResponse {

    @Schema(description = "Identificador da especialidade", example = "1", type = "integer", format = "int32")
    private Integer idEspecialidade;

    @Schema(description = "Nome da especialidade", example = "Clínica Geral")
    private String nome;

    public EspecialidadeResponse() {
    }

    public EspecialidadeResponse(Integer idEspecialidade, String nome) {
        this.idEspecialidade = idEspecialidade;
        this.nome = nome;
    }

    public static EspecialidadeResponse of(Especialidade especialidade) {
        if (especialidade == null) {
            return null;
        }
        return new EspecialidadeResponse(especialidade.getIdEspecialidade(), especialidade.getNome());
    }

    public static List<EspecialidadeResponse> of(List<Especialidade> especialidades) {
        return especialidades.stream().map(EspecialidadeResponse::of).toList();
    }

    public Integer getIdEspecialidade() {
        return idEspecialidade;
    }

    public void setIdEspecialidade(Integer idEspecialidade) {
        this.idEspecialidade = idEspecialidade;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
