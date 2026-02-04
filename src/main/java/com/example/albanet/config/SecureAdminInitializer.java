package com.example.albanet.config;

import com.example.albanet.staff.internal.StaffEntity;
import com.example.albanet.staff.internal.StaffRepository;
import com.example.albanet.staff.internal.enums.StaffRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * Secure way to create initial admin using environment variables
 * Usage:
 * - Set ADMIN_EMAIL and ADMIN_PASSWORD environment variables
 * - Run with profile: --spring.profiles.active=init-admin
 */
@Configuration
@Profile("init-admin")
public class SecureAdminInitializer {

    @Value("${ADMIN_EMAIL:admin@albanet.com}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Bean
    public CommandLineRunner createSecureAdmin(StaffRepository staffRepository,
                                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (adminPassword.isEmpty()) {
                System.err.println("ERROR: ADMIN_PASSWORD environment variable not set!");
                return;
            }

            if (!staffRepository.existsByEmail(adminEmail)) {
                StaffEntity admin = new StaffEntity();
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setPhoneNumber("+355 00 000 0000");
                admin.setEmployeeNumber("EMP-000001");
                admin.setRole(StaffRole.ADMIN);
                admin.setHiredAt(LocalDateTime.now());
                admin.setActive(true);

                staffRepository.save(admin);

                System.out.println("\n✅ Secure admin user created: " + adminEmail);
            }
        };
    }
}
