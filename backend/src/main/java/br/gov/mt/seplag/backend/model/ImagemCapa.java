package br.gov.mt.seplag.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * ENTIDADE: ImagemCapa
 *
 * Armazena metadados das imagens de capa dos álbuns.
 * O arquivo binário fica no MinIO (S3-compatible), aqui só os metadados.
 *
 * CAMPOS:
 * - nomeArquivo: Nome original do arquivo enviado
 * - caminhoMinIO: Object key no bucket MinIO (ex: albums/123/cover-abc.jpg)
 * - contentType: MIME type (ex: image/jpeg, image/png)
 * - tamanho: Tamanho em bytes
 * - uploadedBy: Usuário que fez o upload (auditoria)
 */
@Entity
@Table(name = "imagens_capa", indexes = {
        @Index(name = "idx_imagem_album_id", columnList = "album_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ImagemCapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String nomeArquivo;

    @Column(name = "caminho_minio", nullable = false, length = 500, unique = true)
    private String caminhoMinIO;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long tamanho;

    // ═══════════════════════════════════════════════════════════
    // AUDITORIA
    // ═══════════════════════════════════════════════════════════

    @CreatedDate
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @CreatedBy
    @Column(name = "uploaded_by", length = 100, updatable = false)
    private String uploadedBy;
}
