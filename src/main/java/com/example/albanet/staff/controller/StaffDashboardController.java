package com.example.albanet.staff.controller;

import com.example.albanet.staff.internal.StaffEntity;
import com.example.albanet.staff.internal.StaffService;
import com.example.albanet.staff.internal.enums.StaffRole;
import com.example.albanet.ticket.api.TicketApi;
import com.example.albanet.ticket.api.dto.TicketDetailsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/staff")
public class StaffDashboardController {

    private final TicketApi ticketApi;
    private final StaffService staffService;

    public StaffDashboardController(TicketApi ticketApi, StaffService staffService) {
        this.ticketApi = ticketApi;
        this.staffService = staffService;
    }

    @GetMapping("/my-dashboard")
    public String myDashboard(
            @RequestParam(required = false, defaultValue = "unassigned") String view,
            Authentication authentication,
            Model model) {

        String email = authentication.getName();
        StaffEntity staff = staffService.getActiveStaffByEmail(email);

        // Determine team based on role
        String team = getTeamFromRole(staff.getRole());

        if (team == null) {
            // Admin - redirect to admin dashboard
            return "redirect:/staff/dashboard";
        }

        // SUPPORT goes directly to chat
        if (staff.getRole() == StaffRole.SUPPORT) {
            return "redirect:/staff/chat";
        }

        // Get statistics
        TicketApi.StaffStats stats = ticketApi.getStaffStats(email);
        model.addAttribute("stats", stats);

        // Get tickets based on view
        List<TicketDetailsResponse> tickets;
        if ("my-tickets".equals(view)) {
            tickets = ticketApi.getMyTickets(email);
        } else {
            // Default: unassigned tickets
            tickets = ticketApi.getUnassignedTicketsByTeam(team);
        }

        model.addAttribute("tickets", tickets);
        model.addAttribute("currentView", view);
        model.addAttribute("staffName", staff.getFirstName() + " " + staff.getLastName());
        model.addAttribute("staffRole", staff.getRole().name());
        model.addAttribute("staffTeam", team);

        return "staff/my-dashboard";
    }

    @GetMapping("/my-tickets/{id}")
    @ResponseBody
    public ResponseEntity<TicketDetailsResponse> getMyTicketDetails(
            @PathVariable Long id,
            Authentication authentication) {
        TicketDetailsResponse ticket = ticketApi.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/tickets/{id}/claim")
    @ResponseBody
    public ResponseEntity<String> claimTicket(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            ticketApi.claimTicket(id, email);
            return ResponseEntity.ok("Ticket claimed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/my-tickets/{id}/status")
    @ResponseBody
    public ResponseEntity<String> updateMyTicketStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            ticketApi.updateTicketStatus(id, status, email);
            return ResponseEntity.ok("Status updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/my-tickets/{id}/resolution")
    @ResponseBody
    public ResponseEntity<String> addResolution(
            @PathVariable Long id,
            @RequestParam String resolution,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            ticketApi.addResolution(id, resolution, email);
            return ResponseEntity.ok("Resolution added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    /**
     * Map staff role to team name
     */
    private String getTeamFromRole(StaffRole role) {
        return switch (role) {
            case IT1 -> "IT1";
            case IT2 -> "IT2";
            case FINANCE -> "FINANCE";
            case SUPPORT -> "SUPPORT";
            default -> null; // ADMIN doesn't have specific team
        };
    }
}
