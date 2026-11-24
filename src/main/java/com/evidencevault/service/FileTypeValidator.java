package com.evidencevault.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class FileTypeValidator {
    private final Tika tika = new Tika();
    private static final Map<String, Set<String>> ALLOWED = Map.of(
        "pdf", Set.of("application/pdf"),
        "txt", Set.of("text/plain"),
        "csv", Set.of("text/plain", "text/csv", "application/csv"),
        "jpg", Set.of("image/jpeg"),
        "jpeg", Set.of("image/jpeg"),
        "png", Set.of("image/png"),
        "docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        "zip", Set.of("application/zip", "application/x-zip-compressed"),
        "log", Set.of("text/plain")
    );

    public String detectAndValidate(File file, String extension) {
        final String detected;
        try {
            detected = tika.detect(file).toLowerCase(Locale.ROOT);
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Unable to inspect uploaded file content", e);
        }
        Set<String> allowed = ALLOWED.get(extension.toLowerCase(Locale.ROOT));
        if (allowed == null || !allowed.contains(detected)) {
            throw new IllegalArgumentException("File content does not match the permitted type for ." + extension + " (detected: " + detected + ")");
        }
        return detected;
    }
}
