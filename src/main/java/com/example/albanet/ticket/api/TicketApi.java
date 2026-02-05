package com.example.albanet.ticket.api;

import com.example.albanet.ticket.api.dto.CreateClientTicketRequest;
import com.example.albanet.ticket.api.dto.TicketDetailsResponse;

import java.util.List;
import java.util.Map;

/**
 * Public API for the Ticket module.
 * Other modules should use this interface instead of accessing internal classes directly.
 */
public interface TicketApi {

    // ========== Client/User Operations ==========

    /**
     * Create a new ticket from a client
     */
    TicketDetailsResponse createTicket(CreateClientTicketRequest request, Long userId);

    /**
     * Get all tickets for a customer
     */
    List<TicketDetailsResponse> getTicketsByCustomerId(Long customerId);

    /**
     * Get problem types for a specific category
     * Returns list of maps with "value" and "label" keys
     */
    List<Map<String, String>> getProblemTypesByCategory(String category);

    // ========== Staff Operations ==========

    /**
     * Get unassigned tickets for a specific team
     */
    List<TicketDetailsResponse> getUnassignedTicketsByTeam(String team);

    /**
     * Get tickets assigned to a specific staff member
     */
    List<TicketDetailsResponse> getMyTickets(String staffEmail);

    /**
     * Get ticket by ID
     */
    TicketDetailsResponse getTicketById(Long id);

    /**
     * Claim an unassigned ticket
     */
    void claimTicket(Long ticketId, String staffEmail);

    /**
     * Update ticket status
     */
    void updateTicketStatus(Long ticketId, String status, String staffEmail);

    /**
     * Add resolution summary to a ticket
     */
    void addResolution(Long ticketId, String resolution, String staffEmail);

    /**
     * Get ticket statistics for a staff member
     */
    StaffStats getStaffStats(String staffEmail);

    // ========== Admin Operations ==========

    /**
     * Get ticket stats for admin dashboard
     */
    TicketStats getTicketStats();

    /**
     * Get filtered tickets for admin
     */
    List<TicketDetailsResponse> getFilteredTickets(String team, String status);

    /**
     * Reassign a ticket to another staff member
     */
    void reassignTicket(Long ticketId, String assignedTo, String updatedBy);

    /**
     * Escalate a ticket to another team
     */
    void escalateTicket(Long ticketId, String team, String updatedBy);

    /**
     * Change ticket priority
     */
    void changePriority(Long ticketId, String priority, String updatedBy);

    /**
     * Update ticket status (admin version)
     */
    void updateStatus(Long ticketId, String status, String updatedBy);

    /**
     * Staff statistics
     */
    record StaffStats(long totalTickets, long inProgress, long completed) {}

    /**
     * Admin ticket statistics
     */
    record TicketStats(long unassigned, long inProgress, long done, long total) {}
}
