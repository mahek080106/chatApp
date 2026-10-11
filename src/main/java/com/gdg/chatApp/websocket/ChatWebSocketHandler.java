package com.gdg.chatApp.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.chatApp.dto.ChatMessageRequest;
import com.gdg.chatApp.dto.MessageResponse;
import com.gdg.chatApp.model.Conversation;
import com.gdg.chatApp.model.User;
import com.gdg.chatApp.repository.UserRepository;
import com.gdg.chatApp.service.ConversationService;
import com.gdg.chatApp.service.MessageService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // userId -> all open connections of that user.
    // A Set because one user can have several tabs open.
    private final Map<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final MessageService messageService;

    public ChatWebSocketHandler(UserRepository userRepository,
                                ConversationService conversationService,
                                MessageService messageService) {
        this.userRepository = userRepository;
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    // A tab connected
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserId(session);
        sessions.computeIfAbsent(userId, id -> new CopyOnWriteArraySet<>())
                .add(session);
    }

    // A tab closed
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Set<WebSocketSession> userSessions = sessions.get(getUserId(session));
        if (userSessions != null) {
            userSessions.remove(session);
        }
    }

    // A message arrived from a browser
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ChatMessageRequest request =
                objectMapper.readValue(message.getPayload(), ChatMessageRequest.class);

            if (request.getConversationId() == null) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "conversationId is required");
            }

            // 1. who is sending?
            User sender = userRepository.findById(getUserId(session)).orElseThrow();

            // 2. is the sender part of this conversation?
            Conversation conversation =
                conversationService.getForMember(request.getConversationId(), sender);

            // 3. save in the database
            MessageResponse saved =
                messageService.saveMessage(conversation, sender, request.getContent());

            // 4. push to both users (all their open tabs)
            String json = objectMapper.writeValueAsString(saved);
            sendToUser(conversation.getUser1().getId(), json);
            sendToUser(conversation.getUser2().getId(), json);

        } catch (ResponseStatusException e) {
            sendError(session, e.getReason());
        } catch (JsonProcessingException e) {
            sendError(session, "Invalid message format");
        }
    }

    private void sendToUser(Long userId, String json) {
        Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions == null) {
            return; // user is offline, message is already saved in DB
        }
        for (WebSocketSession s : userSessions) {
            send(s, json);
        }
    }

    private void sendError(WebSocketSession session, String error) {
        try {
            send(session, objectMapper.writeValueAsString(Map.of("error", error)));
        } catch (JsonProcessingException ignored) {
            // cannot happen for a simple map
        }
    }

    // A session can only send one message at a time, so we lock it
    private void send(WebSocketSession session, String json) {
        if (!session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            // connection broke; it will be removed in afterConnectionClosed
        }
    }

    private Long getUserId(WebSocketSession session) {
        return (Long) session.getAttributes().get("userId");
    }
}
