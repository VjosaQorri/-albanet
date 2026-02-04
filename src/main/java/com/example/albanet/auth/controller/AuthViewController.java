package com.example.albanet.auth.controller;

import com.example.albanet.auth.dto.RegisterRequest;
import com.example.albanet.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.Set;

@Controller
public class AuthViewController {

    @Autowired
    private AuthService authService;

    /**
     * Root mapping - redirects based on authentication status
     */
    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Check if it's an anonymous user
            if ("anonymousUser".equals(authentication.getPrincipal().toString())) {
                return "redirect:/login";
            }

            // Check user roles
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

            // Redirect staff to their dashboard
            if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_IT1") ||
                roles.contains("ROLE_IT2") || roles.contains("ROLE_FINANCE") ||
                roles.contains("ROLE_SUPPORT")) {
                return "redirect:/staff/dashboard";
            }

            // Redirect regular users to home
            if (roles.contains("ROLE_USER")) {
                return "redirect:/home";
            }
        }

        // Not authenticated - go to login
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid RegisterRequest registerRequest,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("registerRequest", registerRequest);
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "auth/register";
        }

        try {
            authService.registerUser(registerRequest);
            redirectAttributes.addFlashAttribute("success", "Account created successfully! Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("registerRequest", registerRequest);
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }
}
