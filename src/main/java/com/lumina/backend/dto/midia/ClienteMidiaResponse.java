package com.lumina.backend.dto.midia;

import com.lumina.backend.model.ClienteMidia;
import java.time.format.DateTimeFormatter;

public class ClienteMidiaResponse {

    private Long id;
    private String nomeOriginal;
    private String extensao;
    private String mimeType;
    private Long tamanhoBytes;
    private String urlVisualizacao;
    private String bucket;
    private String chaveS3;
    private TipoResumoDto tipo;
    private ConsultaResumoDto consulta;
    private DentistaResumoDto dentista;
    private String legenda;
    private String criadoEm;

    public ClienteMidiaResponse() {
    }

    public static ClienteMidiaResponse fromEntity(ClienteMidia entity, String urlVisualizacao) {
        return fromEntity(entity, urlVisualizacao, null);
    }

    public static ClienteMidiaResponse fromEntity(ClienteMidia entity, String urlVisualizacao, String bucket) {
        if (entity == null) return null;
        ClienteMidiaResponse dto = new ClienteMidiaResponse();
        dto.setId(entity.getIdClienteMidia());
        dto.setNomeOriginal(entity.getNomeOriginal());
        dto.setExtensao(entity.getExtensao());
        dto.setMimeType(entity.getMimeType());
        dto.setTamanhoBytes(entity.getTamanhoBytes());
        dto.setUrlVisualizacao(urlVisualizacao);
        dto.setBucket(bucket);
        dto.setChaveS3(entity.getChaveS3());
        dto.setLegenda(entity.getLegenda());

        if (entity.getTipoMidia() != null) {
            dto.setTipo(new TipoResumoDto(entity.getTipoMidia().getIdTipoMidia(), entity.getTipoMidia().getNome()));
        }

        if (entity.getConsulta() != null) {
            dto.setConsulta(new ConsultaResumoDto(
                    entity.getConsulta().getIdConsulta(),
                    entity.getConsulta().getData() != null ? entity.getConsulta().getData().toString() : null
            ));
        }

        if (entity.getUsuario() != null) {
            dto.setDentista(new DentistaResumoDto(
                    entity.getUsuario().getIdUsuario(),
                    entity.getUsuario().getNome()
            ));
        }

        if (entity.getCriadoEm() != null) {
            dto.setCriadoEm(entity.getCriadoEm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        return dto;
    }

    public static class TipoResumoDto {
        private Long id;
        private String nome;

        public TipoResumoDto() {}
        public TipoResumoDto(Long id, String nome) {
            this.id = id;
            this.nome = nome;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
    }

    public static class ConsultaResumoDto {
        private Long id;
        private String data;

        public ConsultaResumoDto() {}
        public ConsultaResumoDto(Long id, String data) {
            this.id = id;
            this.data = data;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    public static class DentistaResumoDto {
        private Long id;
        private String nome;

        public DentistaResumoDto() {}
        public DentistaResumoDto(Long id, String nome) {
            this.id = id;
            this.nome = nome;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getExtensao() {
        return extensao;
    }

    public void setExtensao(String extensao) {
        this.extensao = extensao;
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

    public String getUrlVisualizacao() {
        return urlVisualizacao;
    }

    public void setUrlVisualizacao(String urlVisualizacao) {
        this.urlVisualizacao = urlVisualizacao;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getChaveS3() {
        return chaveS3;
    }

    public void setChaveS3(String chaveS3) {
        this.chaveS3 = chaveS3;
    }

    public TipoResumoDto getTipo() {
        return tipo;
    }

    public void setTipo(TipoResumoDto tipo) {
        this.tipo = tipo;
    }

    public ConsultaResumoDto getConsulta() {
        return consulta;
    }

    public void setConsulta(ConsultaResumoDto consulta) {
        this.consulta = consulta;
    }

    public DentistaResumoDto getDentista() {
        return dentista;
    }

    public void setDentista(DentistaResumoDto dentista) {
        this.dentista = dentista;
    }

    public String getLegenda() {
        return legenda;
    }

    public void setLegenda(String legenda) {
        this.legenda = legenda;
    }

    public String getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(String criadoEm) {
        this.criadoEm = criadoEm;
    }
}
