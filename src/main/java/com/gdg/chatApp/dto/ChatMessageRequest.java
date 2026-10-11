package com.gdg.chatApp.dto;

// JSON the browser sends over the WebSocket:
// {"conversationId": 1, "content": "Hello!"}
public class ChatMessageRequest {

    private Long conversationId;
    private String content;

    public Long getConversationId() {
        return conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
