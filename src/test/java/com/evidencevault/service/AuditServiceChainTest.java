package com.evidencevault.service;

import com.evidencevault.entity.AuditLog;
import com.evidencevault.repository.AuditLogRepository;
import com.evidencevault.repository.UserRepository;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditServiceChainTest {
    @Test void validChainIsAccepted() {
        AuditLogRepository repo=mock(AuditLogRepository.class); UserRepository users=mock(UserRepository.class);
        AuditService service=new AuditService(repo,users);
        AuditLog a=new AuditLog(); a.setTimestamp(java.time.LocalDateTime.now()); a.setActorUsername("admin"); a.setAction("LOGIN_SUCCESS"); a.setEntityType("AUTH"); a.setEntityId("admin"); a.setResult("SUCCESS"); a.setPreviousHash("GENESIS");
        // Generate a valid record by using the same service's log flow with a mocked save.
        when(repo.findTopByOrderByIdDesc()).thenReturn(null);
        when(repo.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));
        service.log("admin","LOGIN_SUCCESS","AUTH","admin","SUCCESS","127.0.0.1","ok");
        List<AuditLog> saved=new ArrayList<>();
        // Reconstruct from the object captured by Mockito for verification.
        var captor=org.mockito.ArgumentCaptor.forClass(AuditLog.class);
        verify(repo,atLeastOnce()).save(captor.capture());
        AuditLog record=captor.getAllValues().get(captor.getAllValues().size()-1);
        when(repo.findAllByOrderByIdAsc()).thenReturn(List.of(record));
        assertTrue(service.verifyChain().valid());
    }
    @Test void brokenPreviousLinkIsDetected() {
        AuditLogRepository repo=mock(AuditLogRepository.class); UserRepository users=mock(UserRepository.class);
        AuditService service=new AuditService(repo,users);
        AuditLog a=new AuditLog(); a.setPreviousHash("WRONG"); a.setIntegrityHash("anything"); a.setTimestamp(java.time.LocalDateTime.now()); a.setActorUsername("admin"); a.setAction("X"); a.setResult("SUCCESS");
        when(repo.findAllByOrderByIdAsc()).thenReturn(List.of(a));
        assertFalse(service.verifyChain().valid());
    }
}
