package com.evidencevault.repository;

import com.evidencevault.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long>{
    List<AuditLog> findTop20ByOrderByTimestampDesc();
    List<AuditLog> findTop20ByActorUsernameOrderByTimestampDesc(String actorUsername);
    List<AuditLog> findAllByOrderByIdAsc();
    AuditLog findTopByOrderByIdDesc();
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampAsc(String entityType, String entityId);
}
