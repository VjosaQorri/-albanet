package com.example.albanet.config;

import com.example.albanet.staff.api.StaffApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    public CommandLineRunner createSecureAdmin(StaffApi staffApi,
                                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (adminPassword.isEmpty()) {
                System.err.println("ERROR: ADMIN_PASSWORD environment variable not set!");
                return;
            }

            if (!staffApi.existsByEmail(adminEmail)) {
                staffApi.createAdmin(
                    "System",
                    "Administrator",
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    "+355 00 000 0000",
                    "EMP-000001"
                );

                System.out.println("\n✅ Secure admin user created: " + adminEmail);
            }
        };
    }
}
