package com.gdg.chatApp.dto;

import com.gdg.chatApp.model.Message;

// createdAt is a String (ISO format) so it also works with the plain
// JSON converter we use inside the WebSocket handler.
public class MessageResponse {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private String createdAt;

    public MessageResponse(Long id, Long conversationId, Long senderId,
                           String senderName, String content, String createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static MessageResponse from(Message message) {
        return new MessageResponse(
            message.getId(),
            message.getConversation().getId(),
            message.getSender().getId(),
            message.getSender().getName(),
            message.getContent(),
            message.getCreatedAt().toString()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
