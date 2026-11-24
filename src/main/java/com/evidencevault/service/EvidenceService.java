package com.evidencevault.service;

import com.evidencevault.entity.Evidence;
import com.evidencevault.entity.EvidenceVerification;
import com.evidencevault.repository.CaseRepository;
import com.evidencevault.repository.EvidenceRepository;
import com.evidencevault.repository.UserRepository;
import com.evidencevault.util.HashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class EvidenceService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "txt", "csv", "jpg", "jpeg", "png", "docx", "zip", "log"
    );

    private final EvidenceRepository repo;
    private final CaseRepository cases;
    private final UserRepository users;
    private final AuditService audit;
    private final IntegrityService integrity;
    private final CaseService caseService;
    private final Path root;
    private final long maxSize;
    private final EvidenceStorage storage;
    private final FileTypeValidator fileTypeValidator;
    private final MalwareScanner malwareScanner;

    public EvidenceService(
            EvidenceRepository repo,
            CaseRepository cases,
            UserRepository users,
            AuditService audit,
            IntegrityService integrity,
            CaseService caseService,
            @Value("${app.storage.path}") String path,
            @Value("${app.max-file-size}") long maxSize,
            FileTypeValidator fileTypeValidator,
            MalwareScanner malwareScanner,
            EvidenceStorage storage) {

        this.repo = repo;
        this.cases = cases;
        this.users = users;
        this.audit = audit;
        this.integrity = integrity;
        this.caseService = caseService;
        this.root = Paths.get(path).toAbsolutePath().normalize();
        this.maxSize = maxSize;
        this.fileTypeValidator = fileTypeValidator;
        this.malwareScanner = malwareScanner;
        this.storage = storage;

        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize evidence storage", e);
        }
    }

    public Evidence upload(
            String caseId,
            MultipartFile file,
            String description,
            String username,
            String ip) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file is not allowed");
        }

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File exceeds the configured size limit");
        }

        String original = Paths.get(
                Objects.requireNonNull(file.getOriginalFilename(), "file")
        ).getFileName().toString();

        if (original.isBlank()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String lower = original.toLowerCase(Locale.ROOT);
        int dot = lower.lastIndexOf('.');

        if (dot < 1 || !ALLOWED_EXTENSIONS.contains(lower.substring(dot + 1))) {
            audit.log(username, "INVALID_FILE_UPLOAD", "CASE", caseId,
                    "FAILURE", ip, "File type rejected: " + original);
            throw new IllegalArgumentException("File type is not allowed");
        }

        var investigationCase = cases.findByCaseId(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));

        caseService.ensureAccess(investigationCase, username, ip, "EVIDENCE_UPLOAD");

        if ("ARCHIVED".equalsIgnoreCase(investigationCase.getStatus())) {
            throw new IllegalStateException(
                    "Archived cases cannot accept new evidence. Restore the case first.");
        }

        var user = users.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        String storageName = UUID.randomUUID() + ".bin";
        Path target = root.resolve(storageName).normalize();

        if (!target.getParent().equals(root)) {
            throw new SecurityException("Invalid storage path");
        }

        try {
            try (var input = file.getInputStream()) {
                storage.write(input, storageName, maxSize);
            }
            String detectedMime = fileTypeValidator.detectAndValidate(target.toFile(), lower.substring(dot + 1));
            malwareScanner.scan(target.toFile());
            long actualSize = Files.size(target);
            if (actualSize > maxSize) {
                throw new IllegalArgumentException("File exceeds the configured size limit");
            }
            String hash = HashUtil.sha256(target.toFile());

            Evidence evidence = new Evidence();
            evidence.setInvestigationCase(investigationCase);
            evidence.setOriginalFilename(original);
            evidence.setStorageFilename(storageName);
            evidence.setMimeType(detectedMime);
            evidence.setFileSize(actualSize);
            evidence.setSha256Hash(hash);
            evidence.setUploadedBy(user);
            evidence.setDescription(description == null ? "" : description.trim());
            evidence.setStatus("PENDING");

            Evidence saved = repo.save(evidence);

            audit.log(
                    username,
                    "EVIDENCE_UPLOADED",
                    "EVIDENCE",
                    String.valueOf(saved.getId()),
                    "SUCCESS",
                    ip,
                    "SHA-256=" + hash
            );

            return saved;
        } catch (Exception e) {
            Files.deleteIfExists(target);
            throw e;
        }
    }

    public List<Evidence> byCase(Long caseId) {
        return repo.findByInvestigationCaseIdOrderByUploadedAtDesc(caseId);
    }

    public Evidence get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Evidence not found: " + id));
    }

    public Evidence getAuthorized(Long id, String username, String ip, String action) {
        Evidence evidence = get(id);
        caseService.ensureAccess(evidence.getInvestigationCase(), username, ip, action);
        if ("EVIDENCE_VIEWED".equals(action)) {
            audit.log(username, action, "EVIDENCE", String.valueOf(id), "SUCCESS", ip, "Evidence viewed");
        } else if ("EVIDENCE_HISTORY_VIEWED".equals(action)) {
            audit.log(username, action, "EVIDENCE", String.valueOf(id), "SUCCESS", ip, "Verification history viewed");
        } else if ("EVIDENCE_REPORT_VIEWED".equals(action)) {
            audit.log(username, action, "EVIDENCE", String.valueOf(id), "SUCCESS", ip, "Evidence report generated");
        }
        return evidence;
    }

    public com.evidencevault.entity.InvestigationCase getCaseAuthorized(String caseId, String username, String ip) {
        var c = cases.findByCaseId(caseId).orElseThrow(() -> new NoSuchElementException("Case not found: " + caseId));
        caseService.ensureAccess(c, username, ip, "CASE_VIEWED");
        return c;
    }

    public File file(Evidence evidence) {
        return storage.file(evidence.getStorageFilename());
    }

    public EvidenceVerification verify(Long id, String username, String ip) {
        Evidence evidence = getAuthorized(id, username, ip, "EVIDENCE_VERIFY");
        return integrity.verify(evidence, username, ip, file(evidence));
    }

    public List<EvidenceVerification> history(Long id) {
        return integrity.history(id);
    }

    public List<EvidenceVerification> historyAuthorized(Long id, String username, String ip) {
        getAuthorized(id, username, ip, "EVIDENCE_HISTORY_VIEWED");
        return integrity.history(id);
    }

    public void recordDownload(Evidence evidence, String username, String ip) {
        caseService.ensureAccess(evidence.getInvestigationCase(), username, ip, "EVIDENCE_DOWNLOAD");
        audit.log(
                username,
                "EVIDENCE_DOWNLOADED",
                "EVIDENCE",
                String.valueOf(evidence.getId()),
                "SUCCESS",
                ip,
                "Downloaded " + evidence.getOriginalFilename()
        );
    }
}
