package br.gov.mt.seplag.backend.dto;

import java.util.List;

public record UploadImagemResponse(
        Long albumId,
        String mensagem,
        List<ImagemInfo> imagens
) {
    public record ImagemInfo(
            Long id,
            String nomeArquivo,
            String urlPresigned,
            String contentType,
            Long tamanhoBytes
    ) {
        /**
         * Tamanho formatado (ex: "2.5 MB")
         */
        public String tamanhoFormatado() {
            if (tamanhoBytes == null) return null;
            if (tamanhoBytes < 1024) return tamanhoBytes + " B";
            if (tamanhoBytes < 1024 * 1024) return String.format("%.1f KB", tamanhoBytes / 1024.0);
            return String.format("%.1f MB", tamanhoBytes / (1024.0 * 1024));
        }
    }

    public int totalImagens() {
        return imagens != null ? imagens.size() : 0;
    }
}
