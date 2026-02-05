package com.example.albanet.chat;

import com.example.albanet.staff.api.StaffApi;
import com.example.albanet.staff.api.dto.StaffDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/staff/chat")
public class StaffChatController {

    private final ChatService chatService;
    private final StaffApi staffApi;

    public StaffChatController(ChatService chatService, StaffApi staffApi) {
        this.chatService = chatService;
        this.staffApi = staffApi;
    }

    /**
     * Show the chat page
     */
    @GetMapping("")
    public String chatPage() {
        return "staff/chat";
    }

    /**
     * Get all active/waiting chat sessions
     */
    @GetMapping("/sessions")
    @ResponseBody
    public ResponseEntity<List<ChatSession>> getSessions() {
        return ResponseEntity.ok(chatService.getActiveSessions());
    }

    /**
     * Get chat statistics
     */
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("waitingCount", chatService.getWaitingCount());
        stats.put("totalUnread", chatService.getTotalUnreadCount());
        return ResponseEntity.ok(stats);
    }

    /**
     * Join a chat session
     */
    @PostMapping("/join/{sessionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> joinChat(
            @PathVariable Long sessionId,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            StaffDto staff = staffApi.getActiveStaffByEmail(email);

            ChatSession session = chatService.joinSession(
                sessionId,
                staff.getId(),
                staff.getFirstName() + " " + staff.getLastName()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("session", session);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Send message as staff
     */
    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam Long sessionId,
            @RequestParam String content,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            StaffDto staff = staffApi.getActiveStaffByEmail(email);

            ChatMessage message = chatService.sendMessage(
                sessionId,
                staff.getId(),
                staff.getFirstName() + " " + staff.getLastName(),
                "STAFF",
                content
            );

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
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.getMessages(sessionId));
    }

    /**
     * Mark messages as read (staff reading)
     */
    @PostMapping("/read/{sessionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long sessionId) {
        chatService.markAsRead(sessionId, "STAFF");
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Close chat session
     */
    @PostMapping("/close/{sessionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> closeChat(@PathVariable Long sessionId) {
        chatService.closeSession(sessionId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
