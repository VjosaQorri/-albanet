package com.example.albanet.ticket.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketRestController {

    @GetMapping
    public List<String> list() {
        return Collections.emptyList();
    }
}
