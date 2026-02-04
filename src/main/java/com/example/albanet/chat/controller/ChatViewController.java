package com.example.albanet.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatViewController {
    @GetMapping("/chats")
    public String chats() { return "chats"; }
}
