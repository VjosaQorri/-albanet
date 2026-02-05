package com.example.albanet.user.controller;

import com.example.albanet.subscription.api.SubscriptionApi;
import com.example.albanet.subscription.api.dto.SubscriptionPlanDto;
import com.example.albanet.subscription.api.dto.UserSubscriptionDto;
import com.example.albanet.user.internal.UserEntity;
import com.example.albanet.user.internal.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for client/user-facing views (home, plans, subscriptions, profile).
 * Consolidates the former "client" module functionality.
 */
@Controller
public class UserClientController {

    private final UserService userService;
    private final SubscriptionApi subscriptionApi;

    public UserClientController(UserService userService, SubscriptionApi subscriptionApi) {
        this.userService = userService;
        this.subscriptionApi = subscriptionApi;
    }

    /**
     * Add user information to model
     */
    private void addUserToModel(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            model.addAttribute("userName", "Guest");
            model.addAttribute("userEmail", "guest@example.com");
            return;
        }

        userService.findEntityByEmail(authentication.getName())
                .ifPresentOrElse(user -> {
                    model.addAttribute("userName", user.getFirstName() + " " + user.getLastName());
                    model.addAttribute("userEmail", user.getEmail());
                    model.addAttribute("userId", user.getId());
                }, () -> {
                    model.addAttribute("userName", "Guest");
                    model.addAttribute("userEmail", authentication.getName());
                });
    }

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        return "client/home";
    }

    @GetMapping("/tv")
    public String tv(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        List<SubscriptionPlanDto> plans = subscriptionApi.getPlansByCategory("TV");
        model.addAttribute("plans", plans);

        // Check if user has active TV subscription
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<UserEntity> userOpt = userService.findEntityByEmail(email);
            if (userOpt.isPresent()) {
                Long userId = userOpt.get().getId();
                UserSubscriptionDto activeSubscription = subscriptionApi.getActiveSubscriptionByCategory(userId, "TV");
                UserSubscriptionDto pendingSubscription = subscriptionApi.getPendingSubscriptionByCategory(userId, "TV");

                if (activeSubscription != null) {
                    model.addAttribute("activeSubscription", activeSubscription);
                    model.addAttribute("currentPlanCode", activeSubscription.getPlanCode());
                }
                if (pendingSubscription != null) {
                    model.addAttribute("pendingSubscription", pendingSubscription);
                    model.addAttribute("hasPendingUpgrade", true);
                    model.addAttribute("pendingPlanName", pendingSubscription.getPlanName());
                }
            }
        }

        return "client/tv-plans";
    }

    @GetMapping("/mobile")
    public String mobile(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        List<SubscriptionPlanDto> plans = subscriptionApi.getPlansByCategory("MOBILE");
        model.addAttribute("plans", plans);

        // Check if user has active Mobile subscription
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<UserEntity> userOpt = userService.findEntityByEmail(email);
            if (userOpt.isPresent()) {
                Long userId = userOpt.get().getId();
                UserSubscriptionDto activeSubscription = subscriptionApi.getActiveSubscriptionByCategory(userId, "MOBILE");
                UserSubscriptionDto pendingSubscription = subscriptionApi.getPendingSubscriptionByCategory(userId, "MOBILE");

                if (activeSubscription != null) {
                    model.addAttribute("activeSubscription", activeSubscription);
                }
                if (pendingSubscription != null) {
                    model.addAttribute("pendingSubscription", pendingSubscription);
                }
            }
        }

        return "client/mobile-plans";
    }

    @GetMapping("/internet")
    public String internet(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        List<SubscriptionPlanDto> plans = subscriptionApi.getPlansByCategory("INTERNET");
        model.addAttribute("plans", plans);

        // Check if user has active Internet subscription
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<UserEntity> userOpt = userService.findEntityByEmail(email);
            if (userOpt.isPresent()) {
                Long userId = userOpt.get().getId();
                UserSubscriptionDto activeSubscription = subscriptionApi.getActiveSubscriptionByCategory(userId, "INTERNET");
                UserSubscriptionDto pendingSubscription = subscriptionApi.getPendingSubscriptionByCategory(userId, "INTERNET");

                if (activeSubscription != null) {
                    model.addAttribute("activeSubscription", activeSubscription);
                    model.addAttribute("currentPlanCode", activeSubscription.getPlanCode());
                }
                if (pendingSubscription != null) {
                    model.addAttribute("pendingSubscription", pendingSubscription);
                    model.addAttribute("hasPendingUpgrade", true);
                    model.addAttribute("pendingPlanName", pendingSubscription.getPlanName());
                }
            }
        }

        return "client/internet-plans";
    }

    @PostMapping("/subscribe")
    @ResponseBody
    public ResponseEntity<String> subscribe(
            @RequestParam String planCode,
            @RequestParam Integer duration,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<UserEntity> userOpt = userService.findEntityByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            Long userId = userOpt.get().getId();
            subscriptionApi.subscribe(userId, planCode, duration);
            return ResponseEntity.ok("Subscription successful!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        return "client/profile";
    }

    @GetMapping("/my-subscriptions")
    public String mySubscriptions(Authentication authentication, Model model) {
        addUserToModel(authentication, model);

        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<UserEntity> userOpt = userService.findEntityByEmail(email);
            if (userOpt.isPresent()) {
                Long userId = userOpt.get().getId();
                List<UserSubscriptionDto> subscriptions = subscriptionApi.getUserSubscriptions(userId);
                model.addAttribute("subscriptions", subscriptions);
            }
        }

        return "client/my-subscriptions";
    }

    @DeleteMapping("/cancel-subscription/{subscriptionId}")
    public ResponseEntity<String> cancelSubscription(@PathVariable Long subscriptionId, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<UserEntity> userOpt = userService.findEntityByEmail(email);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(403).body("User not found");
            }

            Long userId = userOpt.get().getId();
            subscriptionApi.cancelSubscription(subscriptionId, userId);
            return ResponseEntity.ok("Subscription cancelled successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/settings")
    public String settings(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        return "client/settings";
    }
}
