package com.gdg.chatApp.repository;

import com.gdg.chatApp.model.Conversation;
import com.gdg.chatApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByUser1AndUser2(User user1, User user2);

    @Query("select c from Conversation c " +
           "where c.user1 = :user or c.user2 = :user " +
           "order by c.createdAt desc")
    List<Conversation> findAllForUser(@Param("user") User user);
}
