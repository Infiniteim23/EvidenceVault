package com.evidencevault.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

@Service
public class LocalEvidenceStorage implements EvidenceStorage {
    private final Path root;
    public LocalEvidenceStorage(@Value("${app.storage.path}") String path) {
        this.root = Paths.get(path).toAbsolutePath().normalize();
        try { Files.createDirectories(root); }
        catch (IOException e) { throw new IllegalStateException("Unable to initialize evidence storage", e); }
    }
    private Path safe(String name) {
        Path p = root.resolve(name).normalize();
        if (!root.equals(p.getParent())) throw new SecurityException("Invalid storage path");
        return p;
    }
    @Override public Path write(InputStream input, String name, long maxSize) throws IOException {
        if (input == null) throw new IllegalArgumentException("Input stream is required");
        if (maxSize <= 0) throw new IllegalArgumentException("Maximum file size must be positive");

        Path target = safe(name);
        long written = 0;
        byte[] buffer = new byte[8192];

        try (InputStream in = input;
             java.io.OutputStream out = Files.newOutputStream(
                     target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                written += read;
                if (written > maxSize) {
                    throw new IllegalArgumentException("File exceeds the configured size limit");
                }
                out.write(buffer, 0, read);
            }
        } catch (RuntimeException | IOException e) {
            Files.deleteIfExists(target);
            throw e;
        }
        return target;
    }
    @Override public File file(String name) { return safe(name).toFile(); }
    @Override public boolean exists(String name) { return Files.exists(safe(name)); }
}
