package com.meetingnotes.service;

import com.meetingnotes.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

/**
 * Stores uploads on the local filesystem under {@code app.storage.local-dir}.
 *
 * <p>To use S3 instead, add the AWS SDK and create an {@code S3StorageService}
 * implementing {@link StorageService}, then mark it {@code @Primary} (or use a
 * profile). No other class needs to change.
 */
@Service
public class LocalStorageService implements StorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp3", "wav", "mp4", "m4a");

    private final Path rootDir;

    public LocalStorageService(@Value("${app.storage.local-dir}") String localDir) {
        this.rootDir = Paths.get(localDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create storage directory: " + rootDir, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file was uploaded");
        }

        String original = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        String ext = StringUtils.getFilenameExtension(original);
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new BadRequestException(
                    "Unsupported file type. Allowed: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        String storageKey = UUID.randomUUID() + "." + ext.toLowerCase();
        Path target = rootDir.resolve(storageKey).normalize();

        // Guard against path traversal.
        if (!target.getParent().equals(rootDir)) {
            throw new BadRequestException("Invalid file name");
        }

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file " + original, e);
        }
        return storageKey;
    }

    @Override
    public Resource load(String storageKey) {
        try {
            Path file = rootDir.resolve(storageKey).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BadRequestException("Stored file not found: " + storageKey);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new BadRequestException("Could not read file: " + storageKey);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(rootDir.resolve(storageKey).normalize());
        } catch (IOException e) {
            // best-effort cleanup; log in a real app
        }
    }
}
