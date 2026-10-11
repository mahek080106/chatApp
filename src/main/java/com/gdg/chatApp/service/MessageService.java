package com.gdg.chatApp.service;

import com.gdg.chatApp.dto.MessageResponse;
import com.gdg.chatApp.model.Conversation;
import com.gdg.chatApp.model.Message;
import com.gdg.chatApp.model.User;
import com.gdg.chatApp.repository.MessageRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MessageService {

    private static final int MAX_LENGTH = 2000;

    private final MessageRepository messageRepository;
    private final ConversationService conversationService;

    public MessageService(MessageRepository messageRepository,
                          ConversationService conversationService) {
        this.messageRepository = messageRepository;
        this.conversationService = conversationService;
    }

    // Previous messages of a conversation (only members can read them)
    public List<MessageResponse> getMessages(Long conversationId, User me) {
        conversationService.getForMember(conversationId, me);

        return messageRepository
            .findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
            .map(MessageResponse::from)
            .toList();
    }

    // Validate + save one message (used by the WebSocket handler)
    @Transactional
    public MessageResponse saveMessage(Conversation conversation,
                                       User sender, String content) {
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Message cannot be empty");
        }
        if (content.length() > MAX_LENGTH) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Message must be at most " + MAX_LENGTH + " characters");
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content.trim());

        return MessageResponse.from(messageRepository.save(message));
    }
}
