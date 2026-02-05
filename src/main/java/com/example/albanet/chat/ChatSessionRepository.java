package com.example.albanet.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    // Find active or waiting session for a customer
    Optional<ChatSession> findByCustomerIdAndStatusIn(Long customerId, List<String> statuses);

    // Find all waiting sessions (for staff to see)
    List<ChatSession> findByStatusOrderByCreatedAtAsc(String status);

    // Find all active sessions for a staff member
    List<ChatSession> findByStaffIdAndStatusOrderByLastMessageAtDesc(Long staffId, String status);

    // Find all non-closed sessions for staff dashboard
    List<ChatSession> findByStatusInOrderByLastMessageAtDesc(List<String> statuses);

    // Count waiting sessions
    long countByStatus(String status);

    // Count unread messages for staff
    @Query("SELECT COALESCE(SUM(c.unreadCount), 0) FROM ChatSession c WHERE c.status IN ('WAITING', 'ACTIVE')")
    long countTotalUnread();
}
