package com.example.albanet.staff.controller;

import com.example.albanet.staff.api.dto.CreateStaffRequest;
import com.example.albanet.staff.api.dto.StaffDto;
import com.example.albanet.staff.internal.StaffService;
import com.example.albanet.ticket.api.dto.TicketDetailsResponse;
import com.example.albanet.ticket.internal.AdminTicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/staff")
public class AdminDashboardController {

    private final AdminTicketService adminTicketService;
    private final StaffService staffService;

    public AdminDashboardController(AdminTicketService adminTicketService, StaffService staffService) {
        this.adminTicketService = adminTicketService;
        this.staffService = staffService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String status,
            Model model) {

        // Get ticket stats for the dashboard summary
        AdminTicketService.TicketStats stats = adminTicketService.getTicketStats();
        model.addAttribute("stats", stats);

        // Get filtered tickets
        var tickets = adminTicketService.getFilteredTickets(team, status);
        model.addAttribute("tickets", tickets);

        // Add current filters to model for UI state
        model.addAttribute("currentTeam", team != null ? team : "ALL");
        model.addAttribute("currentStatus", status != null ? status : "ALL");

        return "staff/dashboard";
    }

    @GetMapping("/tickets/{id}")
    @ResponseBody
    public ResponseEntity<TicketDetailsResponse> getTicketDetails(@PathVariable Long id) {
        TicketDetailsResponse ticket = adminTicketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/staff-by-team/{team}")
    @ResponseBody
    public ResponseEntity<List<StaffDto>> getStaffByTeam(@PathVariable String team) {
        // Map team to roles
        List<StaffDto> staffList = new java.util.ArrayList<>();

        if ("T1".equals(team)) {
            staffList.addAll(staffService.getActiveStaffByRole(com.example.albanet.staff.internal.enums.StaffRole.IT1)
                .stream()
                .map(this::toStaffDto)
                .collect(java.util.stream.Collectors.toList()));
        } else if ("T2".equals(team)) {
            staffList.addAll(staffService.getActiveStaffByRole(com.example.albanet.staff.internal.enums.StaffRole.IT2)
                .stream()
                .map(this::toStaffDto)
                .collect(java.util.stream.Collectors.toList()));
        } else if ("FINANCE".equals(team)) {
            staffList.addAll(staffService.getActiveStaffByRole(com.example.albanet.staff.internal.enums.StaffRole.FINANCE)
                .stream()
                .map(this::toStaffDto)
                .collect(java.util.stream.Collectors.toList()));
        }

        // Also add SUPPORT who can handle any team (but not ADMIN - admins don't finish tickets)
        staffList.addAll(staffService.getActiveStaffByRole(com.example.albanet.staff.internal.enums.StaffRole.SUPPORT)
            .stream()
            .map(this::toStaffDto)
            .collect(java.util.stream.Collectors.toList()));

        return ResponseEntity.ok(staffList);
    }

    private StaffDto toStaffDto(com.example.albanet.staff.internal.StaffEntity staff) {
        StaffDto dto = new StaffDto();
        dto.setId(staff.getId());
        dto.setFirstName(staff.getFirstName());
        dto.setLastName(staff.getLastName());
        dto.setEmail(staff.getEmail());
        dto.setEmployeeNumber(staff.getEmployeeNumber());
        dto.setRole(staff.getRole().name());
        return dto;
    }

    @PostMapping("/tickets/{id}/reassign")
    @ResponseBody
    public ResponseEntity<String> reassignTicket(
            @PathVariable Long id,
            @RequestParam String assignedTo,
            Authentication authentication) {
        String updatedBy = authentication.getName();
        adminTicketService.reassignTicket(id, assignedTo, updatedBy);
        return ResponseEntity.ok("Ticket reassigned successfully");
    }

    @PostMapping("/tickets/{id}/escalate")
    @ResponseBody
    public ResponseEntity<String> escalateTicket(
            @PathVariable Long id,
            @RequestParam String team,
            Authentication authentication) {
        String updatedBy = authentication.getName();
        adminTicketService.escalateTicket(id, team, updatedBy);
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
            System.out.println("Changing priority for ticket " + id + " to " + priority + " by " + updatedBy);
            adminTicketService.changePriority(id, priority, updatedBy);
            return ResponseEntity.ok("Priority changed successfully");
        } catch (Exception e) {
            System.err.println("Error changing priority: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("Updating status for ticket " + id + " to " + status + " by " + updatedBy);
            adminTicketService.updateStatus(id, status, updatedBy);
            return ResponseEntity.ok("Status updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating status: " + e.getMessage());
            e.printStackTrace();
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
            staffService.createStaff(request);
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
