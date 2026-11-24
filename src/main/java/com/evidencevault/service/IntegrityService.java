package com.evidencevault.service;

import com.evidencevault.entity.Evidence;
import com.evidencevault.entity.EvidenceVerification;
import com.evidencevault.repository.EvidenceRepository;
import com.evidencevault.repository.UserRepository;
import com.evidencevault.repository.VerificationRepository;
import com.evidencevault.util.HashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Service
public class IntegrityService {

    private final VerificationRepository verifications;
    private final EvidenceRepository evidenceRepository;
    private final AuditService audit;
    private final UserRepository users;

    public IntegrityService(
            VerificationRepository verifications,
            EvidenceRepository evidenceRepository,
            AuditService audit,
            UserRepository users) {
        this.verifications = verifications;
        this.evidenceRepository = evidenceRepository;
        this.audit = audit;
        this.users = users;
    }

    @Transactional
    public EvidenceVerification verify(
            Evidence evidence,
            String username,
            String ip,
            File file) {

        try {
            String currentHash;
            String result;

            if (file == null || !file.exists() || !file.isFile()) {
                currentHash = "FILE_MISSING";
                result = "VERIFICATION_FAILED";
            } else {
                currentHash = HashUtil.sha256(file);
                result = currentHash.equalsIgnoreCase(evidence.getSha256Hash())
                        ? "VERIFIED"
                        : "TAMPERED";
            }

            EvidenceVerification verification = new EvidenceVerification();
            verification.setEvidence(evidence);
            verification.setVerifiedBy(users.findByUsername(username).orElseThrow());
            verification.setExpectedHash(evidence.getSha256Hash());
            verification.setCalculatedHash(currentHash);
            verification.setResult(result);
            verifications.save(verification);

            evidence.setStatus(result);
            evidenceRepository.save(evidence);

            audit.log(
                    username,
                    "INTEGRITY_VERIFICATION",
                    "EVIDENCE",
                    String.valueOf(evidence.getId()),
                    result,
                    ip,
                    "Expected=" + evidence.getSha256Hash() + " Current=" + currentHash
            );

            if (!"VERIFIED".equals(result)) {
                audit.log(
                        username,
                        "INTEGRITY_FAILURE",
                        "EVIDENCE",
                        String.valueOf(evidence.getId()),
                        "FAILURE",
                        ip,
                        "Evidence integrity check result: " + result
                );
            }

            return verification;

        } catch (Exception ex) {
            throw new IllegalStateException("Verification failed", ex);
        }
    }

    public List<EvidenceVerification> history(Long id) {
        return verifications.findByEvidenceIdOrderByVerifiedAtDesc(id);
    }
}
