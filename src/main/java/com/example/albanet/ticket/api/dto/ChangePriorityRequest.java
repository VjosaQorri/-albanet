package com.example.albanet.ticket.api.dto;

public class ChangePriorityRequest {
    private Long ticketId;
    private String priority;

    public ChangePriorityRequest() {}

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
