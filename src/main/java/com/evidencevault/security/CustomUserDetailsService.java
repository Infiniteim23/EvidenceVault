package com.evidencevault.security;

import com.evidencevault.entity.User;
import com.evidencevault.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public CustomUserDetailsService(UserRepository users){this.users=users;}
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u=users.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("Invalid credentials"));
        return org.springframework.security.core.userdetails.User.withUsername(u.getUsername())
                .password(u.getPasswordHash())
                .roles(u.getRole().getName())
                .disabled(!u.isEnabled())
                .accountLocked(u.isLocked())
                .build();
    }
}
