package com.example.albanet.ticket.internal;

import com.example.albanet.ticket.internal.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    // Find all tickets
    List<TicketEntity> findAllByOrderByCreatedAtDesc();

    // Filter by team
    List<TicketEntity> findByAssignedTeamOrderByCreatedAtDesc(String assignedTeam);

    // Filter by status
    List<TicketEntity> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    // Filter by team and status
    List<TicketEntity> findByAssignedTeamAndStatusOrderByCreatedAtDesc(String assignedTeam, TicketStatus status);

    // Find tickets by assignedTo
    List<TicketEntity> findByAssignedToOrderByCreatedAtDesc(String assignedTo);

    // Find unassigned tickets (assignedTo is null)
    List<TicketEntity> findByAssignedToIsNullOrderByCreatedAtDesc();

    // Find unassigned tickets for a specific team
    List<TicketEntity> findByAssignedTeamAndAssignedToIsNullOrderByCreatedAtDesc(String assignedTeam);

    // Find unassigned tickets for a specific team (using custom query)
    @Query("SELECT t FROM TicketEntity t WHERE t.assignedTeam = :team AND (t.assignedTo IS NULL OR t.assignedTo = '') ORDER BY t.createdAt DESC")
    List<TicketEntity> findUnassignedByTeam(@Param("team") String team);

    // Find tickets by customer ID
    List<TicketEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
