package com.dionialves.snapdogdelivery.infra.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dionialves.snapdogdelivery.exception.BusinessException;

/**
 * Serviço responsável por armazenar arquivos de imagem de produtos.
 * Salva os arquivos no diretório configurado via {@code app.upload.dir}
 * e retorna o caminho relativo para uso em URLs.
 */
@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;

    /**
     * Armazena um arquivo de imagem e retorna o caminho relativo para acesso via HTTP.
     *
     * @param file arquivo enviado via formulário multipart
     * @return caminho relativo (ex.: "/uploads/products/abc123.jpg")
     * @throws BusinessException se o arquivo for inválido ou ocorrer erro de I/O
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo de imagem não pode ser vazio.");
        }

        var originalName = file.getOriginalFilename();
        var extension = getExtension(originalName);
        validateExtension(extension);

        var fileName = UUID.randomUUID() + "." + extension;
        var targetDir = Paths.get(uploadDir);

        try {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Imagem armazenada: {}", target);
            return "/" + uploadDir + "/" + fileName;
        } catch (IOException e) {
            throw new BusinessException("Erro ao salvar imagem: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private void validateExtension(String extension) {
        if (!extension.matches("jpg|jpeg|png|webp|gif")) {
            throw new BusinessException("Formato de imagem não suportado: " + extension + ". Use JPG, PNG, WEBP ou GIF.");
        }
    }
}
