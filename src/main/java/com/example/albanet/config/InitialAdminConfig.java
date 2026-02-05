package com.example.albanet.config;

import com.example.albanet.staff.api.StaffApi;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initializes the first admin user if none exists.
 * This is a common pattern for creating the initial system administrator.
 */
@Configuration
public class InitialAdminConfig {

    @Bean
    public CommandLineRunner createInitialAdmin(StaffApi staffApi,
                                                PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if any admin exists
            if (staffApi.count() == 0) {
                staffApi.createAdmin(
                    "System",
                    "Administrator",
                    "admin@albanet.com",
                    passwordEncoder.encode("admin123"), // Change after first login!
                    "+355 00 000 0000",
                    "EMP-000001"
                );

                System.out.println("\n========================================");
                System.out.println("✅ INITIAL ADMIN USER CREATED");
                System.out.println("========================================");
                System.out.println("Email: admin@albanet.com");
                System.out.println("Password: admin123");
                System.out.println("⚠️  CHANGE PASSWORD AFTER FIRST LOGIN!");
                System.out.println("========================================\n");
            } else {
                System.out.println("✅ Staff database already initialized (" + staffApi.count() + " staff members)");
            }
        };
    }
}
