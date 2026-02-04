package com.example.albanet.ticket.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTicketRequest {
    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    @Size(max = 50)
    private String category;   // INTERNET / PHONE / TV (for now as String)

    @NotNull
    private Long customerId;

    public CreateTicketRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
}
