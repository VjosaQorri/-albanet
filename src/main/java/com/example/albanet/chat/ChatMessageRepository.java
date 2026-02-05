package com.example.albanet.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByTimestampAsc(Long sessionId);

    List<ChatMessage> findBySessionIdAndReadFalseAndSenderTypeNot(Long sessionId, String senderType);
}
