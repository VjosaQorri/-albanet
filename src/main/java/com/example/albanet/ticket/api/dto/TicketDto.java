package com.example.albanet.ticket.api.dto;

public class TicketDto {
    private Long id;
    private String title;

    public TicketDto() {}
    public TicketDto(Long id, String title) { this.id = id; this.title = title; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
