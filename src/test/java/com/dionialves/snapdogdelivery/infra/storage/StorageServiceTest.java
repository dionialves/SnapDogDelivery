package com.dionialves.snapdogdelivery.infra.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.dionialves.snapdogdelivery.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageServiceTest {

    @TempDir
    Path tempDir;

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new StorageService();
        ReflectionTestUtils.setField(storageService, "uploadDir", tempDir.toString());
    }

    @Test
    @DisplayName("store com arquivo JPG válido salva o arquivo e retorna path relativo")
    void store_arquivoJpg_retornaPathRelativo() throws IOException {
        var file = new MockMultipartFile(
                "imageFile",
                "foto.jpg",
                "image/jpeg",
                "conteudo-da-imagem".getBytes());

        var result = storageService.store(file);

        assertThat(result).startsWith("/").endsWith(".jpg");
        assertThat(result).contains(tempDir.getFileName().toString());

        // Verifica que o arquivo foi fisicamente criado
        var fileName = result.substring(result.lastIndexOf('/') + 1);
        assertThat(Files.exists(tempDir.resolve(fileName))).isTrue();
    }

    @Test
    @DisplayName("store com arquivo PNG preserva a extensão correta")
    void store_arquivoPng_preservaExtensao() {
        var file = new MockMultipartFile(
                "imageFile",
                "logo.png",
                "image/png",
                "dados-png".getBytes());

        var result = storageService.store(file);

        assertThat(result).endsWith(".png");
    }

    @Test
    @DisplayName("store com extensão não suportada lança BusinessException")
    void store_extensaoInvalida_lancaBusinessException() {
        var file = new MockMultipartFile(
                "imageFile",
                "malware.exe",
                "application/octet-stream",
                "binario".getBytes());

        assertThatThrownBy(() -> storageService.store(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("exe");
    }

    @Test
    @DisplayName("store com arquivo vazio lança BusinessException")
    void store_arquivoVazio_lancaBusinessException() {
        var file = new MockMultipartFile(
                "imageFile",
                "vazio.jpg",
                "image/jpeg",
                new byte[0]);

        assertThatThrownBy(() -> storageService.store(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vazio");
    }
}
