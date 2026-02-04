package com.example.albanet.config;

import com.example.albanet.user.internal.UserEntity;
import com.example.albanet.user.internal.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // GENERATE CORRECT BCRYPT HASH FOR ADMIN PASSWORD
        String adminPassword = "admin123";
        String adminHash = passwordEncoder.encode(adminPassword);

        System.out.println("\n========================================");
        System.out.println("🔐 ADMIN PASSWORD HASH");
        System.out.println("========================================");
        System.out.println("Password: " + adminPassword);
        System.out.println("BCrypt Hash: " + adminHash);
        System.out.println("========================================");
        System.out.println("\nSQL INSERT COMMAND:");
        System.out.println("DELETE FROM staff WHERE email = 'admin@admin.com';");
        System.out.println("INSERT INTO staff (first_name, last_name, email, password, phone_number, employee_number, role, hired_at, active) VALUES");
        System.out.println("('Admin', 'Administrator', 'admin@admin.com', '" + adminHash + "', '+355 69 999 9999', 'EMP-ADMIN-001', 'ADMIN', NOW(), true);");
        System.out.println("========================================\n");

        // Check if database is empty
        if (userRepository.count() == 0) {
            // Create a dummy user for testing
            UserEntity dummyUser = new UserEntity();
            dummyUser.setFirstName("Demo");
            dummyUser.setLastName("User");
            dummyUser.setEmail("demo@albanet.com");
            dummyUser.setPassword(passwordEncoder.encode("password123"));
            dummyUser.setPhoneNumber("+355 69 123 4567");
            dummyUser.setStreet("Rruga e Kavajes 123");
            dummyUser.setCity("Tirana");
            dummyUser.setPostalCode("1001");
            dummyUser.setCountry("Albania");
            dummyUser.setActive(true);
            dummyUser.setCreatedAt(LocalDateTime.now());

            userRepository.save(dummyUser);

            System.out.println("✅ DUMMY USER INSERTED INTO DIRECT POSTGRESQL DATABASE:");
            System.out.println("   Email: demo@albanet.com");
            System.out.println("   Password: password123");
            System.out.println("   Name: Demo User");
            System.out.println("   Address: Rruga e Kavajes 123, Tirana 1001, Albania");
            System.out.println("   Phone: +355 69 123 4567");
        } else {
            System.out.println("✅ DIRECT POSTGRESQL DATABASE ALREADY HAS " + userRepository.count() + " USER(S)");
        }
    }
}
