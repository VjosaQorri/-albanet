package com.example.albanet.user.controller;

import com.example.albanet.ticket.api.TicketApi;
import com.example.albanet.ticket.api.dto.CreateClientTicketRequest;
import com.example.albanet.ticket.api.dto.TicketDetailsResponse;
import com.example.albanet.user.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for user ticket operations.
 * Consolidates the former "client" module ticket functionality.
 */
@Controller
public class UserTicketController {

    private final TicketApi ticketApi;

    public UserTicketController(TicketApi ticketApi) {
        this.ticketApi = ticketApi;
    }

    /**
     * Show customer's tickets
     */
    @GetMapping("/my-tickets")
    public String showMyTickets(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<TicketDetailsResponse> tickets = ticketApi.getTicketsByCustomerId(userDetails.getUserId());
        model.addAttribute("tickets", tickets);

        // Add user data for navigation fragment
        model.addAttribute("userName", userDetails.getFullName());
        model.addAttribute("userEmail", userDetails.getEmail());
        model.addAttribute("userId", userDetails.getUserId());

        return "client/my-tickets";
    }

    /**
     * Show the create ticket page - SIMPLE VERSION
     */
    @GetMapping("/help")
    public String showHelpPage(Model model, Authentication authentication) {
        // Add user data for navigation fragment
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            model.addAttribute("userName", userDetails.getFullName());
            model.addAttribute("userEmail", userDetails.getEmail());
            model.addAttribute("userId", userDetails.getUserId());
        } else {
            // Add default values to prevent null errors in fragments
            model.addAttribute("userName", "Guest");
            model.addAttribute("userEmail", "guest@example.com");
        }
        return "client/help";
    }

    /**
     * Get problem types for a specific category (AJAX endpoint)
     */
    @GetMapping("/api/problem-types")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> getProblemTypes(@RequestParam String category) {
        try {
            List<Map<String, String>> response = ticketApi.getProblemTypesByCategory(category);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create a new ticket (AJAX endpoint)
     */
    @PostMapping("/api/create-ticket")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createTicket(
            @Valid @RequestBody CreateClientTicketRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            TicketDetailsResponse ticket = ticketApi.createTicket(request, userDetails.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ticket created successfully! Our team will review it shortly.");
            response.put("ticketId", ticket.getId());
            response.put("assignedTeam", ticket.getAssignedTeam());
            response.put("priority", ticket.getPriority());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
