package com.example.albanet.staff.internal;

import com.example.albanet.staff.internal.enums.StaffRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff")
public class StaffEntity {

    // ...existing fields...

    /**
     * Force password change on next login
     * Companies use this to ensure default passwords are changed immediately
     */
    @Column(nullable = false)
    private boolean mustChangePassword = false;

    /**
     * Track password change history
     */
    @Column
    private LocalDateTime passwordChangedAt;

    /**
     * Account locked after failed login attempts
     */
    @Column(nullable = false)
    private boolean accountLocked = false;

    /**
     * Failed login attempt counter
     */
    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    // Getters and setters for new fields...

    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }

    public boolean isAccountLocked() { return accountLocked; }
    public void setAccountLocked(boolean accountLocked) { this.accountLocked = accountLocked; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
}
