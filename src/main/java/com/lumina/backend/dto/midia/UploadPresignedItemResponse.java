package com.lumina.backend.dto.midia;

public class UploadPresignedItemResponse {

    private Long midiaId;
    private String nomeOriginal;
    private String uploadUrl;
    private String chaveS3;

    public UploadPresignedItemResponse() {
    }

    public UploadPresignedItemResponse(Long midiaId, String nomeOriginal, String uploadUrl, String chaveS3) {
        this.midiaId = midiaId;
        this.nomeOriginal = nomeOriginal;
        this.uploadUrl = uploadUrl;
        this.chaveS3 = chaveS3;
    }

    public Long getMidiaId() {
        return midiaId;
    }

    public void setMidiaId(Long midiaId) {
        this.midiaId = midiaId;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getChaveS3() {
        return chaveS3;
    }

    public void setChaveS3(String chaveS3) {
        this.chaveS3 = chaveS3;
    }
}
