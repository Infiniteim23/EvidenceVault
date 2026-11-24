package com.evidencevault.service;

import com.evidencevault.entity.*;
import com.evidencevault.repository.*;
import com.evidencevault.util.CaseIdGenerator;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CaseServiceAuthorizationTest {
    @Test void investigatorCannotAccessAnotherInvestigatorsCase() {
        CaseRepository cases=mock(CaseRepository.class); UserRepository users=mock(UserRepository.class);
        AuditService audit=mock(AuditService.class); CaseIdGenerator ids=mock(CaseIdGenerator.class);
        User owner=new User(); owner.setUsername("investigator-a"); Role r=new Role("INVESTIGATOR"); owner.setRole(r);
        InvestigationCase c=new InvestigationCase(); c.setCaseId("CASE-1"); c.setCreatedBy(owner);
        when(users.findByUsername("investigator-b")).thenReturn(Optional.of(new User()));
        CaseService service=new CaseService(cases,users,ids,audit);
        assertFalse(service.canAccess(c,"investigator-b"));
    }
    @Test void adminCanAccessAnyCase() {
        CaseRepository cases=mock(CaseRepository.class); UserRepository users=mock(UserRepository.class);
        AuditService audit=mock(AuditService.class); CaseIdGenerator ids=mock(CaseIdGenerator.class);
        User owner=new User(); owner.setUsername("investigator-a"); owner.setRole(new Role("INVESTIGATOR"));
        InvestigationCase c=new InvestigationCase(); c.setCaseId("CASE-1"); c.setCreatedBy(owner);
        User admin=new User(); admin.setUsername("admin"); admin.setRole(new Role("ADMIN"));
        when(users.findByUsername("admin")).thenReturn(Optional.of(admin));
        CaseService service=new CaseService(cases,users,ids,audit);
        assertTrue(service.canAccess(c,"admin"));
    }
}
