package com.lumina.backend.dto.midia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class UploadArquivoItemRequest {

    @NotBlank(message = "O nome original do arquivo é obrigatório")
    private String nomeOriginal;

    @NotBlank(message = "O MIME type do arquivo é obrigatório")
    private String mimeType;

    @NotNull(message = "O tamanho em bytes é obrigatório")
    @Positive(message = "O tamanho do arquivo deve ser maior que zero")
    private Long tamanhoBytes;

    public UploadArquivoItemRequest() {
    }

    public UploadArquivoItemRequest(String nomeOriginal, String mimeType, Long tamanhoBytes) {
        this.nomeOriginal = nomeOriginal;
        this.mimeType = mimeType;
        this.tamanhoBytes = tamanhoBytes;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public void setTamanhoBytes(Long tamanhoBytes) {
        this.tamanhoBytes = tamanhoBytes;
    }
}
