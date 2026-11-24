package com.evidencevault;

import com.evidencevault.entity.Role;
import com.evidencevault.entity.User;
import com.evidencevault.repository.RoleRepository;
import com.evidencevault.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EvidenceVaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvidenceVaultApplication.class, args);
    }

    @Bean
    CommandLineRunner seed(
            RoleRepository roles,
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${app.seed.enabled:true}") boolean seedEnabled,
            @Value("${app.seed.admin-password:}") String adminPassword,
            @Value("${app.seed.investigator-password:}") String investigatorPassword) {

        return args -> {
            if (!seedEnabled) return;
            String adminPwd = adminPassword.isBlank() ? "Admin@123" : adminPassword;
            String investigatorPwd = investigatorPassword.isBlank() ? "Investigator@123" : investigatorPassword;
            Role adminRole = roles.findByName("ADMIN")
                    .orElseGet(() -> roles.save(new Role("ADMIN")));

            Role investigatorRole = roles.findByName("INVESTIGATOR")
                    .orElseGet(() -> roles.save(new Role("INVESTIGATOR")));

            createUserIfMissing(
                    users,
                    encoder,
                    "admin",
                    "admin@evidencevault.local",
                    adminPwd,
                    adminRole
            );

            createUserIfMissing(
                    users,
                    encoder,
                    "investigator",
                    "investigator@evidencevault.local",
                    investigatorPwd,
                    investigatorRole
            );
        };
    }

    private void createUserIfMissing(
            UserRepository users,
            PasswordEncoder encoder,
            String username,
            String email,
            String password,
            Role role) {

        if (users.existsByUsername(username)) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);
        users.save(user);
    }
}
