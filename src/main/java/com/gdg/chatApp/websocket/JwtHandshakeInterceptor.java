package com.gdg.chatApp.websocket;

import com.gdg.chatApp.model.User;
import com.gdg.chatApp.repository.UserRepository;
import com.gdg.chatApp.security.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

// Browsers can't set headers on a WebSocket, so the token comes in the URL:
//   ws://host/ws/chat?token=<jwt>
// We check it BEFORE the connection is opened.
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        String token = UriComponentsBuilder.fromUri(request.getURI())
            .build().getQueryParams().getFirst("token");

        if (token != null && jwtUtil.isValid(token)) {
            Optional<User> user =
                userRepository.findByEmail(jwtUtil.extractEmail(token));

            if (user.isPresent()) {
                // remember who this connection belongs to
                attributes.put("userId", user.get().getId());
                return true;
            }
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false; // connection refused
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // nothing to do
    }
}
