package com.example.albanet.ticket.internal;

import com.example.albanet.staff.api.StaffApi;
import com.example.albanet.ticket.api.dto.TicketDetailsResponse;
import com.example.albanet.ticket.internal.enums.TicketStatus;
import com.example.albanet.user.api.UserApi;
import com.example.albanet.user.api.dto.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminTicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final UserApi userApi;
    private final StaffApi staffApi;

    public AdminTicketService(TicketRepository ticketRepository, TicketMapper ticketMapper,
                             UserApi userApi, StaffApi staffApi) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.userApi = userApi;
        this.staffApi = staffApi;
    }

    /**
     * Get all tickets with optional filtering
     * @param team Optional filter by team (T1, T2, FINANCE)
     * @param status Optional filter by status (OPEN, IN_PROGRESS, DONE)
     * @return List of tickets matching the filters
     */
    public List<TicketDetailsResponse> getFilteredTickets(String team, String status) {
        List<TicketEntity> tickets;

        // Parse status if provided
        TicketStatus ticketStatus = null;
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
            try {
                // Handle "unassigned" as a special case for OPEN status
                if (status.equalsIgnoreCase("UNASSIGNED")) {
                    ticketStatus = TicketStatus.OPEN;
                } else {
                    ticketStatus = TicketStatus.valueOf(status.toUpperCase());
                }
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }

        // Apply filters
        if (team != null && !team.isEmpty() && !team.equalsIgnoreCase("ALL")) {
            if (ticketStatus != null) {
                // Filter by both team and status
                tickets = ticketRepository.findByAssignedTeamAndStatusOrderByCreatedAtDesc(team, ticketStatus);
            } else {
                // Filter by team only
                tickets = ticketRepository.findByAssignedTeamOrderByCreatedAtDesc(team);
            }
        } else if (ticketStatus != null) {
            // Filter by status only
            tickets = ticketRepository.findByStatusOrderByCreatedAtDesc(ticketStatus);
        } else {
            // No filters, get all tickets
            tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        }

        return tickets.stream()
                .map(ticket -> {
                    TicketDetailsResponse response = ticketMapper.toDetailsResponse(ticket);
                    // Replace email with staff name if assigned
                    if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().isEmpty()) {
                        try {
                            String staffName = staffApi.getStaffFullNameByEmail(ticket.getAssignedTo());
                            response.setAssignedTo(staffName);
                        } catch (Exception e) {
                            // If staff not found, keep email as fallback
                        }
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get ticket by ID
     */
    public TicketDetailsResponse getTicketById(Long id) {
        TicketEntity ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));

        TicketDetailsResponse response = ticketMapper.toDetailsResponse(ticket);

        // Fetch and add customer information
        Optional<UserDto> customerOpt = userApi.findById(ticket.getCustomerId());
        if (customerOpt.isPresent()) {
            UserDto customer = customerOpt.get();
            response.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
            response.setCustomerEmail(customer.getEmail());
            response.setCustomerPhone(customer.getPhoneNumber());
        }

        // Replace email with staff name if assigned
        if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().isEmpty()) {
            try {
                String staffName = staffApi.getStaffFullNameByEmail(ticket.getAssignedTo());
                response.setAssignedTo(staffName);
            } catch (Exception e) {
                // If staff not found, keep email as fallback
            }
        }

        return response;
    }

    /**
     * Get count of tickets by status
     */
    public TicketStats getTicketStats() {
        List<TicketEntity> allTickets = ticketRepository.findAll();

        long unassigned = allTickets.stream()
                .filter(t -> t.getAssignedTo() == null || t.getAssignedTo().isEmpty())
                .count();

        long inProgress = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS)
                .count();

        long done = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.DONE)
                .count();

        return new TicketStats(unassigned, inProgress, done, allTickets.size());
    }

    /**
     * Update ticket assignment
     */
    @Transactional
    public void reassignTicket(Long ticketId, String assignedTo, String updatedBy) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setAssignedTo(assignedTo);
        if (assignedTo != null && !assignedTo.isEmpty()) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        ticket.setUpdatedBy(updatedBy);
        ticket.setUpdatedAt(java.time.LocalDateTime.now());

        ticketRepository.save(ticket);
    }

    /**
     * Escalate ticket to another team
     */
    @Transactional
    public void escalateTicket(Long ticketId, String newTeam, String updatedBy) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setAssignedTeam(newTeam);
        ticket.setAssignedTo(null); // Unassign when escalating
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setUpdatedBy(updatedBy);
        ticket.setUpdatedAt(java.time.LocalDateTime.now());

        ticketRepository.save(ticket);
    }

    /**
     * Change ticket priority
     */
    @Transactional
    public void changePriority(Long ticketId, String newPriority, String updatedBy) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        // Validate priority
        try {
            com.example.albanet.ticket.internal.enums.TicketPriority priority =
                com.example.albanet.ticket.internal.enums.TicketPriority.valueOf(newPriority.toUpperCase());
            ticket.setPriority(priority);
            ticket.setUpdatedBy(updatedBy);
            ticket.setUpdatedAt(java.time.LocalDateTime.now());

            ticketRepository.save(ticket);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid priority: " + newPriority);
        }
    }

    /**
     * Update ticket status
     */
    @Transactional
    public void updateStatus(Long ticketId, String newStatus, String updatedBy) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        // Validate status
        try {
            TicketStatus status = TicketStatus.valueOf(newStatus.toUpperCase());
            ticket.setStatus(status);
            ticket.setUpdatedBy(updatedBy);
            ticket.setUpdatedAt(java.time.LocalDateTime.now());

            if (newStatus.equalsIgnoreCase("DONE")) {
                ticket.setClosedAt(java.time.LocalDateTime.now());
            }

            ticketRepository.save(ticket);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + newStatus);
        }
    }

    /**
     * Simple stats class
     */
    public static class TicketStats {
        private final long unassigned;
        private final long inProgress;
        private final long done;
        private final long total;

        public TicketStats(long unassigned, long inProgress, long done, long total) {
            this.unassigned = unassigned;
            this.inProgress = inProgress;
            this.done = done;
            this.total = total;
        }

        public long getUnassigned() { return unassigned; }
        public long getInProgress() { return inProgress; }
        public long getDone() { return done; }
        public long getTotal() { return total; }
    }
}
