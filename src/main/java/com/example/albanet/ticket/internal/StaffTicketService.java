package com.example.albanet.ticket.internal;

import com.example.albanet.ticket.api.dto.TicketDetailsResponse;
import com.example.albanet.ticket.internal.enums.TicketStatus;
import com.example.albanet.user.internal.UserEntity;
import com.example.albanet.user.internal.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StaffTicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final UserRepository userRepository;

    public StaffTicketService(TicketRepository ticketRepository, TicketMapper ticketMapper, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.userRepository = userRepository;
    }

    /**
     * Get unassigned tickets for a specific team
     */
    public List<TicketDetailsResponse> getUnassignedTicketsByTeam(String team) {
        List<TicketEntity> tickets = ticketRepository.findUnassignedByTeam(team);

        return tickets.stream()
                .map(ticket -> {
                    TicketDetailsResponse response = ticketMapper.toDetailsResponse(ticket);
                    enrichWithCustomerInfo(response, ticket.getCustomerId());
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get tickets assigned to a specific staff member
     */
    public List<TicketDetailsResponse> getMyTickets(String staffEmail) {
        List<TicketEntity> tickets = ticketRepository.findByAssignedToOrderByCreatedAtDesc(staffEmail);

        return tickets.stream()
                .map(ticket -> {
                    TicketDetailsResponse response = ticketMapper.toDetailsResponse(ticket);
                    enrichWithCustomerInfo(response, ticket.getCustomerId());
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get ticket by ID with customer info
     */
    public TicketDetailsResponse getTicketById(Long id) {
        TicketEntity ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));

        TicketDetailsResponse response = ticketMapper.toDetailsResponse(ticket);
        enrichWithCustomerInfo(response, ticket.getCustomerId());

        return response;
    }

    /**
     * Claim an unassigned ticket
     */
    @Transactional
    public void claimTicket(Long ticketId, String staffEmail) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().isEmpty()) {
            throw new RuntimeException("Ticket is already assigned");
        }

        ticket.setAssignedTo(staffEmail);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUpdatedBy(staffEmail);
        ticket.setUpdatedAt(java.time.LocalDateTime.now());

        ticketRepository.save(ticket);
    }

    /**
     * Update ticket status
     */
    @Transactional
    public void updateTicketStatus(Long ticketId, String status, String staffEmail) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        // Verify the ticket is assigned to this staff member
        if (!staffEmail.equals(ticket.getAssignedTo())) {
            throw new RuntimeException("You can only update your own tickets");
        }

        try {
            TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
            ticket.setStatus(ticketStatus);
            ticket.setUpdatedBy(staffEmail);
            ticket.setUpdatedAt(java.time.LocalDateTime.now());

            if (status.equalsIgnoreCase("DONE")) {
                ticket.setClosedAt(java.time.LocalDateTime.now());
            }

            ticketRepository.save(ticket);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }

    /**
     * Add resolution summary to a ticket
     */
    @Transactional
    public void addResolution(Long ticketId, String resolution, String staffEmail) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        // Verify the ticket is assigned to this staff member
        if (!staffEmail.equals(ticket.getAssignedTo())) {
            throw new RuntimeException("You can only update your own tickets");
        }

        ticket.setResolutionSummary(resolution);
        ticket.setUpdatedBy(staffEmail);
        ticket.setUpdatedAt(java.time.LocalDateTime.now());

        ticketRepository.save(ticket);
    }

    /**
     * Get ticket statistics for the staff member
     */
    public StaffStats getStaffStats(String staffEmail) {
        List<TicketEntity> myTickets = ticketRepository.findByAssignedToOrderByCreatedAtDesc(staffEmail);

        long inProgress = myTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS)
                .count();

        long completed = myTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.DONE)
                .count();

        return new StaffStats(myTickets.size(), inProgress, completed);
    }

    /**
     * Enrich response with customer information
     */
    private void enrichWithCustomerInfo(TicketDetailsResponse response, Long customerId) {
        Optional<UserEntity> customerOpt = userRepository.findById(customerId);
        if (customerOpt.isPresent()) {
            UserEntity customer = customerOpt.get();
            response.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
            response.setCustomerEmail(customer.getEmail());
            response.setCustomerPhone(customer.getPhoneNumber());
        }
    }

    /**
     * Staff statistics DTO
     */
    public static class StaffStats {
        private final long totalTickets;
        private final long inProgress;
        private final long completed;

        public StaffStats(long totalTickets, long inProgress, long completed) {
            this.totalTickets = totalTickets;
            this.inProgress = inProgress;
            this.completed = completed;
        }

        public long getTotalTickets() { return totalTickets; }
        public long getInProgress() { return inProgress; }
        public long getCompleted() { return completed; }
    }
}
