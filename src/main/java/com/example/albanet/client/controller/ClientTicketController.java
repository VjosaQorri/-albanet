package com.example.albanet.client.controller;

import com.example.albanet.ticket.api.dto.CreateClientTicketRequest;
import com.example.albanet.ticket.internal.ClientTicketService;
import com.example.albanet.ticket.internal.TicketEntity;
import com.example.albanet.ticket.internal.TicketRepository;
import com.example.albanet.ticket.internal.TicketMapper;
import com.example.albanet.ticket.api.dto.TicketDetailsResponse;
import com.example.albanet.ticket.internal.enums.TicketProblemType;
import com.example.albanet.user.internal.UserEntity;
import com.example.albanet.user.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ClientTicketController {

    private final ClientTicketService clientTicketService;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public ClientTicketController(ClientTicketService clientTicketService,
                                  TicketRepository ticketRepository,
                                  TicketMapper ticketMapper) {
        this.clientTicketService = clientTicketService;
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
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
        UserEntity user = userDetails.getUser();

        List<TicketEntity> customerTickets = ticketRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId());

        List<TicketDetailsResponse> tickets = customerTickets.stream()
                .map(ticketMapper::toDetailsResponse)
                .collect(Collectors.toList());

        model.addAttribute("tickets", tickets);

        // Add user data for navigation fragment
        model.addAttribute("userName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("userId", user.getId());

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
            UserEntity user = userDetails.getUser();
            model.addAttribute("userName", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("userId", user.getId());
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
            TicketProblemType[] problemTypes = clientTicketService.getProblemTypesByCategory(category);

            List<Map<String, String>> response = Arrays.stream(problemTypes)
                    .map(pt -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("value", pt.name());
                        map.put("label", pt.getDisplayName());
                        return map;
                    })
                    .collect(Collectors.toList());

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
            UserEntity user = userDetails.getUser();

            TicketEntity ticket = clientTicketService.createTicket(request, user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ticket created successfully! Our team will review it shortly.");
            response.put("ticketId", ticket.getId());
            response.put("assignedTeam", ticket.getAssignedTeam());
            response.put("priority", ticket.getPriority().name());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
