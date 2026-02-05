package com.example.albanet.ticket.internal;

import com.example.albanet.ticket.api.TicketApi;
import com.example.albanet.ticket.api.dto.CreateClientTicketRequest;
import com.example.albanet.ticket.api.dto.TicketDetailsResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Facade service that implements TicketApi.
 * Delegates to ClientTicketService, StaffTicketService, and AdminTicketService.
 */
@Service
public class TicketApiService implements TicketApi {

    private final ClientTicketService clientTicketService;
    private final StaffTicketService staffTicketService;
    private final AdminTicketService adminTicketService;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketApiService(ClientTicketService clientTicketService,
                           StaffTicketService staffTicketService,
                           AdminTicketService adminTicketService,
                           TicketRepository ticketRepository,
                           TicketMapper ticketMapper) {
        this.clientTicketService = clientTicketService;
        this.staffTicketService = staffTicketService;
        this.adminTicketService = adminTicketService;
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    // ========== Client/User Operations ==========

    @Override
    public TicketDetailsResponse createTicket(CreateClientTicketRequest request, Long userId) {
        TicketEntity ticket = clientTicketService.createTicket(request, userId);
        return ticketMapper.toDetailsResponse(ticket);
    }

    @Override
    public List<TicketDetailsResponse> getTicketsByCustomerId(Long customerId) {
        return ticketRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(ticketMapper::toDetailsResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, String>> getProblemTypesByCategory(String category) {
        var problemTypes = clientTicketService.getProblemTypesByCategory(category);
        return Arrays.stream(problemTypes)
                .map(pt -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("value", pt.name());
                    map.put("label", pt.getDisplayName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // ========== Staff Operations ==========

    @Override
    public List<TicketDetailsResponse> getUnassignedTicketsByTeam(String team) {
        return staffTicketService.getUnassignedTicketsByTeam(team);
    }

    @Override
    public List<TicketDetailsResponse> getMyTickets(String staffEmail) {
        return staffTicketService.getMyTickets(staffEmail);
    }

    @Override
    public TicketDetailsResponse getTicketById(Long id) {
        return staffTicketService.getTicketById(id);
    }

    @Override
    public void claimTicket(Long ticketId, String staffEmail) {
        staffTicketService.claimTicket(ticketId, staffEmail);
    }

    @Override
    public void updateTicketStatus(Long ticketId, String status, String staffEmail) {
        staffTicketService.updateTicketStatus(ticketId, status, staffEmail);
    }

    @Override
    public void addResolution(Long ticketId, String resolution, String staffEmail) {
        staffTicketService.addResolution(ticketId, resolution, staffEmail);
    }

    @Override
    public StaffStats getStaffStats(String staffEmail) {
        var internalStats = staffTicketService.getStaffStats(staffEmail);
        return new StaffStats(
            internalStats.getTotalTickets(),
            internalStats.getInProgress(),
            internalStats.getCompleted()
        );
    }

    // ========== Admin Operations ==========

    @Override
    public TicketStats getTicketStats() {
        var internalStats = adminTicketService.getTicketStats();
        return new TicketStats(
            internalStats.getUnassigned(),
            internalStats.getInProgress(),
            internalStats.getDone(),
            internalStats.getTotal()
        );
    }

    @Override
    public List<TicketDetailsResponse> getFilteredTickets(String team, String status) {
        return adminTicketService.getFilteredTickets(team, status);
    }

    @Override
    public void reassignTicket(Long ticketId, String assignedTo, String updatedBy) {
        adminTicketService.reassignTicket(ticketId, assignedTo, updatedBy);
    }

    @Override
    public void escalateTicket(Long ticketId, String team, String updatedBy) {
        adminTicketService.escalateTicket(ticketId, team, updatedBy);
    }

    @Override
    public void changePriority(Long ticketId, String priority, String updatedBy) {
        adminTicketService.changePriority(ticketId, priority, updatedBy);
    }

    @Override
    public void updateStatus(Long ticketId, String status, String updatedBy) {
        adminTicketService.updateStatus(ticketId, status, updatedBy);
    }
}
