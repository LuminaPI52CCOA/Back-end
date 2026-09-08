package com.lumina.backend.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Estrutura padronizada para retorno de erros da API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErroRespostaDto {

    @Schema(description = "Timestamp do momento do erro", example = "2026-08-30T17:45:00")
    private LocalDateTime timestamp;

    @Schema(description = "Codigo de status HTTP", example = "400")
    private Integer status;

    @Schema(description = "Descricao do status HTTP", example = "Bad Request")
    private String erro;

    @Schema(description = "Mensagem explicativa do erro", example = "Dados invalidos.")
    private String mensagem;

    @Schema(description = "URI da requisicao que gerou o erro", example = "/clientes")
    private String caminho;

    @Schema(description = "Lista detalhada de erros de validacao por campo")
    private Map<String, String> campos;

    public ErroRespostaDto() {
        this.timestamp = LocalDateTime.now();
    }

    public ErroRespostaDto(Integer status, String erro, String mensagem, String caminho) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.caminho = caminho;
    }

    public ErroRespostaDto(Integer status, String erro, String mensagem, String caminho, Map<String, String> campos) {
        this(status, erro, mensagem, caminho);
        this.campos = campos;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public String getErro() {
        return erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getCaminho() {
        return caminho;
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}
