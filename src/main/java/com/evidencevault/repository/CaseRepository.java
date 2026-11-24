package com.evidencevault.repository;

import com.evidencevault.entity.InvestigationCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;

public interface CaseRepository extends JpaRepository<InvestigationCase,Long>{
    @EntityGraph(attributePaths = {"createdBy"})
    Optional<InvestigationCase> findByCaseId(String caseId);
    boolean existsByCaseId(String caseId);
    long countByStatus(String status);
    List<InvestigationCase> findAllByOrderByCreatedAtDesc();
    List<InvestigationCase> findByCreatedByUsernameOrderByCreatedAtDesc(String username);
}
