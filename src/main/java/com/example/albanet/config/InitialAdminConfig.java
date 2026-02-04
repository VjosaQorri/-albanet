package com.example.albanet.config;

import com.example.albanet.staff.internal.StaffEntity;
import com.example.albanet.staff.internal.StaffRepository;
import com.example.albanet.staff.internal.enums.StaffRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * Initializes the first admin user if none exists.
 * This is a common pattern for creating the initial system administrator.
 */
@Configuration
public class InitialAdminConfig {

    @Bean
    public CommandLineRunner createInitialAdmin(StaffRepository staffRepository,
                                                PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if any admin exists
            if (staffRepository.count() == 0) {
                StaffEntity admin = new StaffEntity();
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setEmail("admin@albanet.com");
                admin.setPassword(passwordEncoder.encode("admin123")); // Change after first login!
                admin.setPhoneNumber("+355 00 000 0000");
                admin.setEmployeeNumber("EMP-000001");
                admin.setRole(StaffRole.ADMIN);
                admin.setHiredAt(LocalDateTime.now());
                admin.setActive(true);

                staffRepository.save(admin);

                System.out.println("\n========================================");
                System.out.println("✅ INITIAL ADMIN USER CREATED");
                System.out.println("========================================");
                System.out.println("Email: admin@albanet.com");
                System.out.println("Password: admin123");
                System.out.println("⚠️  CHANGE PASSWORD AFTER FIRST LOGIN!");
                System.out.println("========================================\n");
            } else {
                System.out.println("✅ Staff database already initialized (" + staffRepository.count() + " staff members)");
            }
        };
    }
}
