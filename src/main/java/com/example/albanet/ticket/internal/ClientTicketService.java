package com.example.albanet.ticket.internal;

import com.example.albanet.ticket.api.dto.CreateClientTicketRequest;
import com.example.albanet.ticket.internal.enums.TicketCategory;
import com.example.albanet.ticket.internal.enums.TicketProblemType;
import com.example.albanet.ticket.internal.enums.TicketStatus;
import com.example.albanet.user.internal.UserEntity;
import com.example.albanet.user.internal.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class ClientTicketService {

    private static final Logger logger = LoggerFactory.getLogger(ClientTicketService.class);

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketSseController ticketSseController;

    public ClientTicketService(TicketRepository ticketRepository, UserRepository userRepository,
                               TicketSseController ticketSseController) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketSseController = ticketSseController;
    }

    /**
     * Create a ticket from a client with automatic team and priority assignment
     */
    public TicketEntity createTicket(CreateClientTicketRequest request, Long userId) {
        // Validate user exists
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Parse category
        TicketCategory category;
        try {
            category = TicketCategory.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category: " + request.getCategory());
        }

        // Parse problem type
        TicketProblemType problemType;
        try {
            problemType = TicketProblemType.valueOf(request.getProblemType());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid problem type: " + request.getProblemType());
        }

        // Verify problem type matches category
        if (problemType.getCategory() != category) {
            throw new RuntimeException("Problem type does not match selected category");
        }

        // Create ticket entity with automatic routing
        TicketEntity ticket = new TicketEntity();
        ticket.setTitle(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(category);
        ticket.setProblemType(problemType);
        ticket.setStatus(TicketStatus.OPEN);

        // Automatic assignment based on problem type
        ticket.setPriority(problemType.getPriority());
        ticket.setAssignedTeam(problemType.getAssignedTeam());
        ticket.setAssignedTo(null); // Unassigned initially

        ticket.setCustomerId(userId);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setCreatedBy(user.getEmail());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setUpdatedBy(user.getEmail());

        TicketEntity savedTicket = ticketRepository.save(ticket);

        // Notify staff dashboards about the new ticket
        logger.info("Ticket saved with ID: {}. Assigned team: {}. About to notify SSE controller...",
                    savedTicket.getId(), savedTicket.getAssignedTeam());

        try {
            ticketSseController.notifyNewTicket(savedTicket.getAssignedTeam());
            logger.info("SSE notification sent successfully for ticket ID: {}", savedTicket.getId());
        } catch (Exception e) {
            logger.error("Failed to send SSE notification for ticket ID: {}", savedTicket.getId(), e);
        }

        return savedTicket;
    }

    /**
     * Get available problem types for a category
     */
    public TicketProblemType[] getProblemTypesByCategory(String categoryStr) {
        try {
            TicketCategory category = TicketCategory.valueOf(categoryStr.toUpperCase());
            return TicketProblemType.getByCategory(category);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category: " + categoryStr);
        }
    }
}
