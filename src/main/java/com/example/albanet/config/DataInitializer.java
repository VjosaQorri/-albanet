package com.example.albanet.config;

import com.example.albanet.user.api.UserApi;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserApi userApi;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserApi userApi, PasswordEncoder passwordEncoder) {
        this.userApi = userApi;
        this.passwordEncoder = passwordEncoder;
    }

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
        if (userApi.count() == 0) {
            // Create a dummy user for testing via API
            userApi.createUser(
                "Demo",
                "User",
                "demo@albanet.com",
                passwordEncoder.encode("password123"),
                "+355 69 123 4567",
                "Rruga e Kavajes 123",
                "Tirana",
                "1001",
                "Albania"
            );

            System.out.println("✅ DUMMY USER INSERTED INTO DIRECT POSTGRESQL DATABASE:");
            System.out.println("   Email: demo@albanet.com");
            System.out.println("   Password: password123");
            System.out.println("   Name: Demo User");
            System.out.println("   Address: Rruga e Kavajes 123, Tirana 1001, Albania");
            System.out.println("   Phone: +355 69 123 4567");
        } else {
            System.out.println("✅ DIRECT POSTGRESQL DATABASE ALREADY HAS " + userApi.count() + " USER(S)");
        }
    }
}
