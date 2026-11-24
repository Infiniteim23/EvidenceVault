package com.evidencevault.service;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class FileTypeValidatorTest {
    private final FileTypeValidator validator=new FileTypeValidator();
    @Test void acceptsRealTextContentForTxt() throws Exception {
        Path p=Files.createTempFile("evidence", ".txt");
        Files.writeString(p,"EvidenceVault test log\n");
        assertEquals("text/plain", validator.detectAndValidate(p.toFile(),"txt"));
        Files.deleteIfExists(p);
    }
    @Test void rejectsExtensionContentMismatch() throws Exception {
        Path p=Files.createTempFile("evidence", ".png");
        Files.writeString(p,"not a png");
        assertThrows(IllegalArgumentException.class, () -> validator.detectAndValidate(p.toFile(),"png"));
        Files.deleteIfExists(p);
    }
}
