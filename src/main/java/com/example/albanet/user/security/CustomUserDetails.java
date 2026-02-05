package com.example.albanet.user.security;

import com.example.albanet.user.internal.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final UserEntity user;

    public CustomUserDetails(UserEntity user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    // Helper methods to avoid exposing UserEntity to other modules
    public Long getUserId() {
        return user.getId();
    }

    public String getFullName() {
        return user.getFirstName() + " " + user.getLastName();
    }

    public String getEmail() {
        return user.getEmail();
    }

    /**
     * @deprecated Use getUserId(), getFullName(), getEmail() instead to avoid exposing internal entity
     */
    @Deprecated
    public UserEntity getUser() {
        return user;
    }
}
