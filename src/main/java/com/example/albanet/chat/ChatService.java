package com.example.albanet.chat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    public ChatService(ChatSessionRepository sessionRepository, ChatMessageRepository messageRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * Get or create a chat session for a customer
     */
    public ChatSession getOrCreateSession(Long customerId, String customerName, String customerEmail) {
        // Check if customer already has an active/waiting session
        Optional<ChatSession> existing = sessionRepository.findByCustomerIdAndStatusIn(
            customerId, Arrays.asList("WAITING", "ACTIVE")
        );

        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new session
        ChatSession session = new ChatSession();
        session.setCustomerId(customerId);
        session.setCustomerName(customerName);
        session.setCustomerEmail(customerEmail);
        session.setStatus("WAITING");

        return sessionRepository.save(session);
    }

    /**
     * Send a message
     */
    public ChatMessage sendMessage(Long sessionId, Long senderId, String senderName, String senderType, String content) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Chat session not found"));

        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        // Update session
        session.setLastMessage(content.length() > 100 ? content.substring(0, 100) + "..." : content);
        session.setLastMessageAt(LocalDateTime.now());

        // Increment unread count if customer sends message
        if ("CUSTOMER".equals(senderType)) {
            session.setUnreadCount(session.getUnreadCount() + 1);
        }

        sessionRepository.save(session);
        return messageRepository.save(message);
    }

    /**
     * Get all messages for a session
     */
    public List<ChatMessage> getMessages(Long sessionId) {
        return messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }

    /**
     * Get all active/waiting sessions for staff dashboard
     */
    public List<ChatSession> getActiveSessions() {
        return sessionRepository.findByStatusInOrderByLastMessageAtDesc(Arrays.asList("WAITING", "ACTIVE"));
    }

    /**
     * Get waiting sessions count
     */
    public long getWaitingCount() {
        return sessionRepository.countByStatus("WAITING");
    }

    /**
     * Get total unread count
     */
    public long getTotalUnreadCount() {
        return sessionRepository.countTotalUnread();
    }

    /**
     * Staff joins a chat session
     */
    public ChatSession joinSession(Long sessionId, Long staffId, String staffName) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Chat session not found"));

        session.setStaffId(staffId);
        session.setStaffName(staffName);
        session.setStatus("ACTIVE");

        return sessionRepository.save(session);
    }

    /**
     * Mark messages as read
     */
    public void markAsRead(Long sessionId, String readerType) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Chat session not found"));

        // Mark messages as read
        String senderTypeToMark = "STAFF".equals(readerType) ? "CUSTOMER" : "STAFF";
        List<ChatMessage> unreadMessages = messageRepository.findBySessionIdAndReadFalseAndSenderTypeNot(sessionId, readerType);
        unreadMessages.forEach(msg -> msg.setRead(true));
        messageRepository.saveAll(unreadMessages);

        // Reset unread count if staff reads
        if ("STAFF".equals(readerType)) {
            session.setUnreadCount(0);
            sessionRepository.save(session);
        }
    }

    /**
     * Close a chat session
     */
    public void closeSession(Long sessionId) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Chat session not found"));

        session.setStatus("CLOSED");
        session.setClosedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }

    /**
     * Get session by ID
     */
    public ChatSession getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Chat session not found"));
    }

    /**
     * Get active session for customer
     */
    public Optional<ChatSession> getActiveSessionForCustomer(Long customerId) {
        return sessionRepository.findByCustomerIdAndStatusIn(customerId, Arrays.asList("WAITING", "ACTIVE"));
    }
}
