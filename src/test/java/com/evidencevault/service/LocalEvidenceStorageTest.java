package com.evidencevault.service;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalEvidenceStorageTest {
    @Test
    void createsEvidenceWithCreateNewAndRejectsCollision() throws Exception {
        Path root = Files.createTempDirectory("evidence-storage-test");
        try {
            LocalEvidenceStorage storage = new LocalEvidenceStorage(root.toString());
            byte[] data = "forensic-evidence".getBytes(StandardCharsets.UTF_8);

            Path created = storage.write(new ByteArrayInputStream(data), "evidence.bin", 1024);
            assertArrayEquals(data, Files.readAllBytes(created));

            assertThrows(java.nio.file.FileAlreadyExistsException.class,
                    () -> storage.write(new ByteArrayInputStream(data), "evidence.bin", 1024));
        } finally {
            Files.walk(root)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try { Files.deleteIfExists(path); } catch (Exception ignored) { }
                    });
        }
    }

    @Test
    void rejectsOversizedEvidenceAndDoesNotLeavePartialFile() throws Exception {
        Path root = Files.createTempDirectory("evidence-storage-size-test");
        try {
            LocalEvidenceStorage storage = new LocalEvidenceStorage(root.toString());
            byte[] data = "1234567890".getBytes(StandardCharsets.UTF_8);

            assertThrows(IllegalArgumentException.class,
                    () -> storage.write(new ByteArrayInputStream(data), "too-large.bin", 5));
            assertFalse(Files.exists(root.resolve("too-large.bin")));
        } finally {
            Files.walk(root)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try { Files.deleteIfExists(path); } catch (Exception ignored) { }
                    });
        }
    }
}
