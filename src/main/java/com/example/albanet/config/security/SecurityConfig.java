package com.example.albanet.config.security;

import com.example.albanet.staff.security.StaffUserDetailsService;
import com.example.albanet.user.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private StaffUserDetailsService staffUserDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StaffLoginSuccessHandler staffLoginSuccessHandler;

    // Staff Security Configuration
    @Bean
    @Order(1)
    public SecurityFilterChain staffSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/staff/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/staff/login").permitAll()
                        .requestMatchers("/staff/dashboard", "/staff/create-staff").hasRole("ADMIN")
                        .requestMatchers("/staff/my-dashboard", "/staff/my-tickets/**", "/staff/tickets/*/claim").hasAnyRole("IT1", "IT2", "FINANCE", "SUPPORT")
                        .requestMatchers("/staff/chat", "/staff/chat/**").hasAnyRole("ADMIN", "SUPPORT")
                        .requestMatchers("/staff/**").hasAnyRole("ADMIN", "SUPPORT", "FINANCE", "IT1", "IT2")
                )
                .authenticationProvider(staffAuthenticationProvider())
                .formLogin(form -> form
                        .loginPage("/staff/login")
                        .loginProcessingUrl("/staff/login")
                        .usernameParameter("email")
                        .successHandler(staffLoginSuccessHandler)
                        .failureUrl("/staff/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/staff/logout")
                        .logoutSuccessUrl("/staff/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }

    // User/Client Security Configuration
    @Bean
    @Order(2)
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/css/**",
                                "/images/**",
                                "/js/**"
                        ).permitAll()
                        .requestMatchers("/home", "/tv", "/mobile", "/internet", "/help",
                                       "/profile", "/my-tickets", "/my-subscriptions",
                                       "/api/**", "/subscribe", "/cancel-subscription/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(userAuthenticationProvider())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider userAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationProvider staffAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(staffUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
}
