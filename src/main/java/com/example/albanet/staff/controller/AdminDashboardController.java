package com.example.albanet.staff.controller;

import com.example.albanet.staff.api.StaffApi;
import com.example.albanet.staff.api.dto.CreateStaffRequest;
import com.example.albanet.staff.api.dto.StaffDto;
import com.example.albanet.ticket.api.TicketApi;
import com.example.albanet.ticket.api.dto.TicketDetailsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/staff")
public class AdminDashboardController {

    private final TicketApi ticketApi;
    private final StaffApi staffApi;

    public AdminDashboardController(TicketApi ticketApi, StaffApi staffApi) {
        this.ticketApi = ticketApi;
        this.staffApi = staffApi;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String status,
            Model model) {

        // Get ticket stats for the dashboard summary
        TicketApi.TicketStats stats = ticketApi.getTicketStats();
        model.addAttribute("stats", stats);

        // Get filtered tickets
        var tickets = ticketApi.getFilteredTickets(team, status);
        model.addAttribute("tickets", tickets);

        // Add current filters to model for UI state
        model.addAttribute("currentTeam", team != null ? team : "ALL");
        model.addAttribute("currentStatus", status != null ? status : "ALL");

        return "staff/dashboard";
    }

    @GetMapping("/tickets/{id}")
    @ResponseBody
    public ResponseEntity<TicketDetailsResponse> getTicketDetails(@PathVariable Long id) {
        TicketDetailsResponse ticket = ticketApi.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/staff-by-team/{team}")
    @ResponseBody
    public ResponseEntity<List<StaffDto>> getStaffByTeam(@PathVariable String team) {
        List<StaffDto> staffList = new ArrayList<>();

        if ("T1".equals(team)) {
            staffList.addAll(staffApi.getActiveStaffByRole("IT1"));
        } else if ("T2".equals(team)) {
            staffList.addAll(staffApi.getActiveStaffByRole("IT2"));
        } else if ("FINANCE".equals(team)) {
            staffList.addAll(staffApi.getActiveStaffByRole("FINANCE"));
        }

        // Also add SUPPORT who can handle any team
        staffList.addAll(staffApi.getActiveStaffByRole("SUPPORT"));

        return ResponseEntity.ok(staffList);
    }

    @PostMapping("/tickets/{id}/reassign")
    @ResponseBody
    public ResponseEntity<String> reassignTicket(
            @PathVariable Long id,
            @RequestParam String assignedTo,
            Authentication authentication) {
        String updatedBy = authentication.getName();
        ticketApi.reassignTicket(id, assignedTo, updatedBy);
        return ResponseEntity.ok("Ticket reassigned successfully");
    }

    @PostMapping("/tickets/{id}/escalate")
    @ResponseBody
    public ResponseEntity<String> escalateTicket(
            @PathVariable Long id,
            @RequestParam String team,
            Authentication authentication) {
        String updatedBy = authentication.getName();
        ticketApi.escalateTicket(id, team, updatedBy);
        return ResponseEntity.ok("Ticket escalated successfully");
    }

    @PostMapping("/tickets/{id}/priority")
    @ResponseBody
    public ResponseEntity<String> changePriority(
            @PathVariable Long id,
            @RequestParam String priority,
            Authentication authentication) {
        try {
            String updatedBy = authentication.getName();
            ticketApi.changePriority(id, priority, updatedBy);
            return ResponseEntity.ok("Priority changed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/tickets/{id}/status")
    @ResponseBody
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {
        try {
            String updatedBy = authentication.getName();
            ticketApi.updateStatus(id, status, updatedBy);
            return ResponseEntity.ok("Status updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/create-staff")
    public String showCreateStaffForm() {
        return "staff/create-staff";
    }

    @PostMapping("/create-staff")
    public String createStaff(
            @ModelAttribute CreateStaffRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            staffApi.createStaff(request);
            redirectAttributes.addFlashAttribute("success",
                "Staff member created successfully! Email: " + request.getEmail());
            return "redirect:/staff/create-staff";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/staff/create-staff";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                "Failed to create staff member. Please try again.");
            return "redirect:/staff/create-staff";
        }
    }
}
