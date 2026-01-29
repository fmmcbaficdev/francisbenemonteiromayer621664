package br.gov.mt.seplag.backend.service;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.UUID;

/**
 * Service para operações com MinIO (S3-compatible)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MinIOService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioInternalUrl;

    @Value("${minio.public-url}")
    private String minioPublicUrl;

    @Value("${minio.presigned-url-expiry-minutes:30}")
    private int presignedUrlExpiryMinutes;

    /**
     * Criar bucket se não existir
     */
    @PostConstruct
    public void init() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!exists) {
                log.info("Criando bucket MinIO: {}", bucketName);
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("Bucket criado com sucesso");
            } else {
                log.info("Bucket já existe: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Erro ao verificar/criar bucket", e);
        }
    }

    /**
     * Upload de arquivo
     */
    public String uploadFile(MultipartFile file) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());

            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("Arquivo enviado para MinIO: {}", fileName);
            return fileName;

        } catch (Exception e) {
            log.error("Erro ao fazer upload para MinIO", e);
            throw new RuntimeException("Erro ao fazer upload: " + e.getMessage());
        }
    }

    /**
     * Gerar URL pública para download
     * Com bucket configurado como público, não precisa de presigned URL
     */
    public String getPresignedUrl(String fileName) {
        // Usar URL pública direta (bucket configurado com acesso público para download)
        return String.format("%s/%s/%s", minioPublicUrl, bucketName, fileName);
    }

    /**
     * Deletar arquivo
     */
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("Arquivo deletado do MinIO: {}", fileName);
        } catch (Exception e) {
            log.error("Erro ao deletar arquivo do MinIO", e);
            throw new RuntimeException("Erro ao deletar: " + e.getMessage());
        }
    }

    /**
     * Gerar nome único para arquivo
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
