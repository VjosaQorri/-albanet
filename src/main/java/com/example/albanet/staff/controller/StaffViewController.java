package com.example.albanet.staff.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaffViewController {

    @GetMapping("/staff/login")
    public String login() {
        return "staff/login";
    }

    // Dashboard is now handled by AdminDashboardController
}
