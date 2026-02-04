package com.example.albanet.client.controller;

import com.example.albanet.catalog.internal.enums.CatalogCode;
import com.example.albanet.subscription.internal.SubscriptionPlanEntity;
import com.example.albanet.subscription.internal.SubscriptionPlanService;
import com.example.albanet.subscription.internal.UserSubscriptionEntity;
import com.example.albanet.user.internal.UserEntity;
import com.example.albanet.user.internal.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class ClientController {

    private final UserRepository userRepository;
    private final SubscriptionPlanService subscriptionPlanService;

    public ClientController(UserRepository userRepository, SubscriptionPlanService subscriptionPlanService) {
        this.userRepository = userRepository;
        this.subscriptionPlanService = subscriptionPlanService;
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

        userRepository.findByEmail(authentication.getName())
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
        List<SubscriptionPlanEntity> plans = subscriptionPlanService.getPlansByCategory("TV");
        model.addAttribute("plans", plans);

        // Check if user has active TV subscription
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                Long userId = userOpt.get().getId();
                UserSubscriptionEntity activeSubscription = subscriptionPlanService.getActiveSubscriptionByCategory(userId, "TV");
                UserSubscriptionEntity pendingSubscription = subscriptionPlanService.getPendingSubscriptionByCategory(userId, "TV");

                if (activeSubscription != null) {
                    model.addAttribute("activeSubscription", activeSubscription);
                    model.addAttribute("currentPlanCode", activeSubscription.getPlanCode().name());
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
        List<SubscriptionPlanEntity> plans = subscriptionPlanService.getPlansByCategory("MOBILE");
        model.addAttribute("plans", plans);

        // Check if user has active Mobile subscription
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                Long userId = userOpt.get().getId();
                UserSubscriptionEntity activeSubscription = subscriptionPlanService.getActiveSubscriptionByCategory(userId, "MOBILE");
                UserSubscriptionEntity pendingSubscription = subscriptionPlanService.getPendingSubscriptionByCategory(userId, "MOBILE");

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
        List<SubscriptionPlanEntity> plans = subscriptionPlanService.getPlansByCategory("INTERNET");
        model.addAttribute("plans", plans);

        // Check if user has active Internet subscription
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                Long userId = userOpt.get().getId();
                UserSubscriptionEntity activeSubscription = subscriptionPlanService.getActiveSubscriptionByCategory(userId, "INTERNET");
                UserSubscriptionEntity pendingSubscription = subscriptionPlanService.getPendingSubscriptionByCategory(userId, "INTERNET");

                if (activeSubscription != null) {
                    model.addAttribute("activeSubscription", activeSubscription);
                    model.addAttribute("currentPlanCode", activeSubscription.getPlanCode().name());
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
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            Long userId = userOpt.get().getId();
            CatalogCode code = CatalogCode.valueOf(planCode);

            subscriptionPlanService.subscribe(userId, code, duration);
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
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                Long userId = userOpt.get().getId();
                List<UserSubscriptionEntity> subscriptions = subscriptionPlanService.getUserSubscriptions(userId);
                model.addAttribute("subscriptions", subscriptions);
            }
        }

        return "client/my-subscriptions";
    }

    @DeleteMapping("/cancel-subscription/{subscriptionId}")
    public ResponseEntity<String> cancelSubscription(@PathVariable Long subscriptionId, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(403).body("User not found");
            }

            Long userId = userOpt.get().getId();
            subscriptionPlanService.cancelSubscription(subscriptionId, userId);
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
