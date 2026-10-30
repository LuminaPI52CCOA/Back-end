package com.lumina.backend.dto.midia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class IniciarUploadRequest {

    @NotNull(message = "O ID do tipo de mídia é obrigatório")
    private Long tipoId;

    private Long consultaId;

    private String legenda;

    @NotEmpty(message = "A lista de arquivos não pode estar vazia")
    @Valid
    private List<UploadArquivoItemRequest> arquivos;

    public IniciarUploadRequest() {
    }

    public IniciarUploadRequest(Long tipoId, Long consultaId, String legenda, List<UploadArquivoItemRequest> arquivos) {
        this.tipoId = tipoId;
        this.consultaId = consultaId;
        this.legenda = legenda;
        this.arquivos = arquivos;
    }

    public Long getTipoId() {
        return tipoId;
    }

    public void setTipoId(Long tipoId) {
        this.tipoId = tipoId;
    }

    public Long getConsultaId() {
        return consultaId;
    }

    public void setConsultaId(Long consultaId) {
        this.consultaId = consultaId;
    }

    public String getLegenda() {
        return legenda;
    }

    public void setLegenda(String legenda) {
        this.legenda = legenda;
    }

    public List<UploadArquivoItemRequest> getArquivos() {
        return arquivos;
    }

    public void setArquivos(List<UploadArquivoItemRequest> arquivos) {
        this.arquivos = arquivos;
    }
}
