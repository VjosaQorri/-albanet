package com.example.albanet.chat;

import com.example.albanet.user.internal.UserEntity;
import com.example.albanet.user.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Customer: Start or get existing chat session
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startChat(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserEntity user = userDetails.getUser();

        ChatSession session = chatService.getOrCreateSession(
            user.getId(),
            user.getFirstName() + " " + user.getLastName(),
            user.getEmail()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("status", session.getStatus());
        response.put("staffName", session.getStaffName());

        return ResponseEntity.ok(response);
    }

    /**
     * Customer: Get existing session if any
     */
    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getSession(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Optional<ChatSession> session = chatService.getActiveSessionForCustomer(userDetails.getUser().getId());

        if (session.isEmpty()) {
            return ResponseEntity.ok(Map.of("hasSession", false));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("hasSession", true);
        response.put("sessionId", session.get().getId());
        response.put("status", session.get().getStatus());
        response.put("staffName", session.get().getStaffName());

        return ResponseEntity.ok(response);
    }

    /**
     * Send a message (customer or staff)
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam Long sessionId,
            @RequestParam String content,
            @RequestParam String senderType,
            @RequestParam Long senderId,
            @RequestParam String senderName) {

        try {
            ChatMessage message = chatService.sendMessage(sessionId, senderId, senderName, senderType, content);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messageId", message.getId());
            response.put("timestamp", message.getTimestamp().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get messages for a session
     */
    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.getMessages(sessionId));
    }

    /**
     * Mark messages as read
     */
    @PostMapping("/read/{sessionId}")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long sessionId,
            @RequestParam String readerType) {

        chatService.markAsRead(sessionId, readerType);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Close a session
     */
    @PostMapping("/close/{sessionId}")
    public ResponseEntity<Map<String, Object>> closeSession(@PathVariable Long sessionId) {
        chatService.closeSession(sessionId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
