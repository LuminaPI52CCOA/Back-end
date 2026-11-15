package com.lumina.backend.dto.midia;

public record UploadPresignedItemResponse(
        Long midiaId,
        String nomeOriginal,
        String uploadUrl,
        String chaveS3
) {
}
