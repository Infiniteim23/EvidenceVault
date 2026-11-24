package com.evidencevault.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public interface EvidenceStorage {
    Path write(java.io.InputStream input, String storageFilename, long maxSize) throws IOException;
    File file(String storageFilename);
    boolean exists(String storageFilename);
}
