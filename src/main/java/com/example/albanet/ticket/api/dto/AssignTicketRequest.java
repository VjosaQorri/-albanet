package com.example.albanet.ticket.api.dto;

import jakarta.validation.constraints.NotNull;

public class AssignTicketRequest {
    @NotNull
    private Long ticketId;
    private Long assignee; // nullable for team-only assignment

    public AssignTicketRequest() {}

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public Long getAssignee() { return assignee; }
    public void setAssignee(Long assignee) { this.assignee = assignee; }
}
