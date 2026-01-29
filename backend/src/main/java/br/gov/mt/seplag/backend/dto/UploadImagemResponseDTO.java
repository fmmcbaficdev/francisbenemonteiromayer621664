package br.gov.mt.seplag.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para resposta de upload de imagens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadImagemResponseDTO {
    private Long albumId;
    private String mensagem;
    private List<ImagemInfoDTO> imagens;
    private Integer totalImagens;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImagemInfoDTO {
        private Long id;
        private String nomeArquivo;
        private String urlPresigned;
        private String contentType;
        private Long tamanho;
    }
}
