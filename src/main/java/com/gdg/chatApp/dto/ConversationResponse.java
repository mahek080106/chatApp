package com.gdg.chatApp.dto;

import com.gdg.chatApp.model.Conversation;
import com.gdg.chatApp.model.User;

import java.time.LocalDateTime;

public class ConversationResponse {

    private Long id;
    private UserResponse otherUser; // the person you are chatting with
    private LocalDateTime createdAt;

    public ConversationResponse(Long id, UserResponse otherUser,
                                LocalDateTime createdAt) {
        this.id = id;
        this.otherUser = otherUser;
        this.createdAt = createdAt;
    }

    public static ConversationResponse from(Conversation conversation, User me) {
        User other = conversation.getUser1().getId().equals(me.getId())
                ? conversation.getUser2()
                : conversation.getUser1();

        return new ConversationResponse(
            conversation.getId(),
            UserResponse.from(other),
            conversation.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public UserResponse getOtherUser() {
        return otherUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
