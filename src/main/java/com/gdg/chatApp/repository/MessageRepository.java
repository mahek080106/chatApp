package com.gdg.chatApp.repository;

import com.gdg.chatApp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // oldest first, so the chat reads top to bottom
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
