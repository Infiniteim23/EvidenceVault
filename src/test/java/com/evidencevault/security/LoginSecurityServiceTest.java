package com.evidencevault.security;

import com.evidencevault.entity.User;
import com.evidencevault.repository.UserRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginSecurityServiceTest {
    @Test void locksAfterConfiguredFailures() {
        UserRepository repo=mock(UserRepository.class);
        User user=new User(); user.setUsername("alice");
        when(repo.findByUsername("alice")).thenReturn(Optional.of(user));
        when(repo.save(any())).thenAnswer(i->i.getArgument(0));
        LoginSecurityService service=new LoginSecurityService(repo, 3, 15);
        service.recordFailure("alice"); service.recordFailure("alice"); service.recordFailure("alice");
        assertTrue(user.isLocked());
        verify(repo,times(3)).save(user);
    }
    @Test void successResetsFailureState() {
        UserRepository repo=mock(UserRepository.class);
        User user=new User(); user.setUsername("alice"); user.setFailedLoginAttempts(2);
        when(repo.findByUsername("alice")).thenReturn(Optional.of(user));
        when(repo.save(any())).thenAnswer(i->i.getArgument(0));
        LoginSecurityService service=new LoginSecurityService(repo, 5, 15);
        service.recordSuccess("alice");
        assertEquals(0,user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
    }
}
