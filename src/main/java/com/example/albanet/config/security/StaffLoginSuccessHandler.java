package com.example.albanet.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class StaffLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // Redirect based on role
        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/staff/dashboard");
        } else if (roles.contains("ROLE_IT1") || roles.contains("ROLE_IT2") ||
                   roles.contains("ROLE_FINANCE") || roles.contains("ROLE_SUPPORT")) {
            response.sendRedirect("/staff/my-dashboard");
        } else {
            response.sendRedirect("/staff/dashboard");
        }
    }
}
