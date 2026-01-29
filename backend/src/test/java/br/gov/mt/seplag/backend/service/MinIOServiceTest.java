package br.gov.mt.seplag.backend.service;

import io.minio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para MinIOService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MinIOService - Testes Unitários")
class MinIOServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private BucketExistsArgs.Builder bucketExistsBuilder;

    @Mock
    private MakeBucketArgs.Builder makeBucketBuilder;

    @Mock
    private PutObjectArgs.Builder putObjectBuilder;

    @Mock
    private GetPresignedObjectUrlArgs.Builder presignedUrlBuilder;

    @Mock
    private RemoveObjectArgs.Builder removeObjectBuilder;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private MinIOService minIOService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minIOService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(minIOService, "presignedUrlExpiryMinutes", 30);
    }

    @Test
    @DisplayName("Deve fazer upload de arquivo com sucesso")
    void deveFazerUploadDeArquivoComSucesso() throws Exception {
        // Arrange
        String fileName = "test-file.jpg";
        byte[] fileContent = "test content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getSize()).thenReturn((long) fileContent.length);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        // Act
        String resultado = minIOService.uploadFile(multipartFile);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.endsWith(".jpg"));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Deve gerar nome único para arquivo")
    void deveGerarNomeUnicoParaArquivo() throws Exception {
        // Arrange
        String fileName = "original.jpg";
        byte[] fileContent = "test".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getSize()).thenReturn((long) fileContent.length);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        // Act
        String resultado1 = minIOService.uploadFile(multipartFile);
        String resultado2 = minIOService.uploadFile(multipartFile);

        // Assert
        assertNotEquals(resultado1, resultado2); // Nomes devem ser diferentes
        assertTrue(resultado1.endsWith(".jpg"));
        assertTrue(resultado2.endsWith(".jpg"));
    }

    @Test
    @DisplayName("Deve gerar presigned URL com sucesso")
    void deveGerarPresignedURLComSucesso() throws Exception {
        // Arrange
        String fileName = "test-file.jpg";
        String expectedUrl = "http://minio:9000/test-bucket/test-file.jpg?X-Amz-Algorithm=...";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);

        // Act
        String url = minIOService.getPresignedUrl(fileName);

        // Assert
        assertNotNull(url);
        assertEquals(expectedUrl, url);
        verify(minioClient, times(1)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    @DisplayName("Deve deletar arquivo com sucesso")
    void deveDeletarArquivoComSucesso() throws Exception {
        // Arrange
        String fileName = "test-file.jpg";
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // Act
        assertDoesNotThrow(() -> minIOService.deleteFile(fileName));

        // Assert
        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao fazer upload com erro")
    void deveLancarExcecaoAoFazerUploadComErro() throws Exception {
        // Arrange
        String fileName = "test-file.jpg";
        byte[] fileContent = "test".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getSize()).thenReturn((long) fileContent.length);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("Erro de conexão"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> minIOService.uploadFile(multipartFile));
    }

    @Test
    @DisplayName("Deve criar bucket se não existir")
    void deveCriarBucketSeNaoExistir() throws Exception {
        // Arrange
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        doNothing().when(minioClient).makeBucket(any(MakeBucketArgs.class));

        // Act - init() é chamado automaticamente pelo Spring
        ReflectionTestUtils.invokeMethod(minIOService, "init");

        // Assert
        verify(minioClient, times(1)).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    @DisplayName("Deve não criar bucket se já existe")
    void deveNaoCriarBucketSeJaExiste() throws Exception {
        // Arrange
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // Act
        ReflectionTestUtils.invokeMethod(minIOService, "init");

        // Assert
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    @DisplayName("Deve lidar com arquivo sem extensão")
    void deveLidarComArquivoSemExtensao() throws Exception {
        // Arrange
        String fileName = "arquivo-sem-extensao";
        byte[] fileContent = "test".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getSize()).thenReturn((long) fileContent.length);
        when(multipartFile.getContentType()).thenReturn("application/octet-stream");
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        // Act
        String resultado = minIOService.uploadFile(multipartFile);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.contains(".")); // Não deve ter extensão
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }
}
