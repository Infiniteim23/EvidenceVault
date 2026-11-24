package com.evidencevault.repository;

import com.evidencevault.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    List<Evidence> findByInvestigationCaseIdOrderByUploadedAtDesc(Long caseId);
    Optional<Evidence> findByStorageFilename(String name);
    long countByStatus(String status);
    long countByInvestigationCaseCreatedByUsername(String username);
    long countByInvestigationCaseCreatedByUsernameAndStatus(String username, String status);
}
