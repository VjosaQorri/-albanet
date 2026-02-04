package com.example.albanet.ticket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TicketViewController {

    @GetMapping("/tickets")
    public String tickets() {
        return "tickets"; // View name
    }
}
