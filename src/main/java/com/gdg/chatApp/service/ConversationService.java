package com.gdg.chatApp.service;

import com.gdg.chatApp.dto.ConversationResponse;
import com.gdg.chatApp.model.Conversation;
import com.gdg.chatApp.model.User;
import com.gdg.chatApp.repository.ConversationRepository;
import com.gdg.chatApp.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    // Small helper class: the conversation + whether we just created it
    public record Result(ConversationResponse conversation, boolean created) {
    }

    // Start a conversation, or return the existing one
    public Result createOrGet(User me, Long otherUserId) {
        if (me.getId().equals(otherUserId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "You cannot chat with yourself");
        }

        User other = userRepository.findById(otherUserId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));

        // smaller id always goes in user1 so a pair is stored only once
        User first = me.getId() < other.getId() ? me : other;
        User second = (first == me) ? other : me;

        Optional<Conversation> existing =
            conversationRepository.findByUser1AndUser2(first, second);

        if (existing.isPresent()) {
            return new Result(ConversationResponse.from(existing.get(), me), false);
        }

        Conversation conversation = new Conversation();
        conversation.setUser1(first);
        conversation.setUser2(second);
        Conversation saved = conversationRepository.save(conversation);

        return new Result(ConversationResponse.from(saved, me), true);
    }

    public List<ConversationResponse> getMyConversations(User me) {
        return conversationRepository.findAllForUser(me).stream()
            .map(conversation -> ConversationResponse.from(conversation, me))
            .toList();
    }

    // Returns the conversation only if "user" is one of the two members.
    // 404 if it does not exist, 403 if the user is not part of it.
    public Conversation getForMember(Long conversationId, User user) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Conversation not found"));

        boolean isMember =
            conversation.getUser1().getId().equals(user.getId()) ||
            conversation.getUser2().getId().equals(user.getId());

        if (!isMember) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You are not part of this conversation");
        }

        return conversation;
    }
}
