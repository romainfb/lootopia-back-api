package com.lootopia.lootopia_app.infrastructure.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.infrastructure.in.websocket.dto.JoinMessage;
import com.lootopia.lootopia_app.infrastructure.in.websocket.dto.PositionMessage;
import com.lootopia.lootopia_app.infrastructure.in.websocket.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket handler for position tracking
 * Handles authentication and message routing
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthHandler extends TextWebSocketHandler {

    private final JwtDecoder jwtDecoder;
    private final ObjectMapper objectMapper;

    // Map to store admin sessions by huntId
    private final Map<String, Set<WebSocketSession>> adminSessionsByHuntId = new ConcurrentHashMap<>();

    // Map to store user data (role, huntId) by session
    private final Map<WebSocketSession, UserSessionData> sessionData = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connection established: {}", session.getId());

        // Extract JWT token from headers
        String token = extractToken(session);
        if (token==null) {
            closeSession(session, CloseStatus.POLICY_VIOLATION.withReason("Authentication required"));
            return;
        }

        try {
            // Validate token and extract user info
            Jwt jwt = jwtDecoder.decode(token);
            String userId = jwt.getSubject();
            boolean isAdmin = hasAdminRole(jwt);

            // Store session data
            sessionData.put(session, new UserSessionData(userId, isAdmin, null));

            log.info("User authenticated: userId={}, isAdmin={}", userId, isAdmin);
        } catch (Exception e) {
            log.error("Error authenticating WebSocket connection", e);
            closeSession(session, CloseStatus.POLICY_VIOLATION.withReason("Invalid authentication"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UserSessionData userData = sessionData.get(session);
        if (userData==null) {
            closeSession(session, CloseStatus.POLICY_VIOLATION.withReason("Session not authenticated"));
            return;
        }

        try {
            WebSocketMessage webSocketMessage = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);

            // Validate that the userId in the message matches the authenticated user
            if (!userData.userId().equals(webSocketMessage.getUserId())) {
                log.warn("UserId mismatch: {} vs {}", userData.userId(), webSocketMessage.getUserId());
                return;
            }

            switch (webSocketMessage.getType()) {
                case POSITION -> handlePositionMessage(session, (PositionMessage) webSocketMessage);
                case JOIN -> handleJoinMessage(session, (JoinMessage) webSocketMessage);
                default -> log.warn("Unknown message type: {}", webSocketMessage.getType());
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket connection closed: {}, status: {}", session.getId(), status);

        UserSessionData userData = sessionData.remove(session);
        if (userData!=null && userData.isAdmin() && userData.huntId()!=null) {
            // Remove admin session from hunt
            Set<WebSocketSession> adminSessions = adminSessionsByHuntId.get(userData.huntId());
            if (adminSessions!=null) {
                adminSessions.remove(session);
                if (adminSessions.isEmpty()) {
                    adminSessionsByHuntId.remove(userData.huntId());
                }
            }
        }
    }

    /**
     * Handle position message from player
     */
    private void handlePositionMessage(WebSocketSession session, PositionMessage positionMessage) throws IOException {
        UserSessionData userData = sessionData.get(session);
        String huntId = positionMessage.getHuntId();

        // If user has joined a hunt, use that huntId, otherwise use the one from the message
        if (userData.huntId()!=null) {
            huntId = userData.huntId();
        } else if (huntId==null) {
            log.warn("No huntId provided in position message and user has not joined a hunt");
            return;
        }

        // Forward position to admin sessions for this hunt
        Set<WebSocketSession> adminSessions = adminSessionsByHuntId.get(huntId);
        if (adminSessions!=null && !adminSessions.isEmpty()) {
            String messageJson = objectMapper.writeValueAsString(positionMessage);
            TextMessage textMessage = new TextMessage(messageJson);

            for (WebSocketSession adminSession : adminSessions) {
                if (adminSession.isOpen()) {
                    adminSession.sendMessage(textMessage);
                }
            }
        }
    }

    /**
     * Handle join message from client
     */
    private void handleJoinMessage(WebSocketSession session, JoinMessage joinMessage) {
        UserSessionData userData = sessionData.get(session);
        String huntId = joinMessage.getHuntId();

        if (huntId==null) {
            log.warn("No huntId provided in join message");
            return;
        }

        // Update session data with huntId
        sessionData.put(session, new UserSessionData(userData.userId(), userData.isAdmin(), huntId));

        // If admin, add to admin sessions for this hunt
        if (userData.isAdmin()) {
            adminSessionsByHuntId.computeIfAbsent(huntId, k -> new CopyOnWriteArraySet<>()).add(session);
            log.info("Admin joined hunt: {}", huntId);
        }
    }

    /**
     * Extract JWT token from WebSocket handshake request
     */
    private String extractToken(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        Map<String, String> headers = (Map<String, String>) attributes.get("headers");

        if (headers!=null) {
            String authHeader = headers.get("Authorization");
            if (authHeader!=null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }

        return null;
    }

    /**
     * Check if the JWT token has the admin role
     */
    private boolean hasAdminRole(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();

        // Extract roles from JWT claims
        // The exact path depends on how Keycloak is configured
        try {
            Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
            if (realmAccess!=null) {
                Set<String> roles = (Set<String>) realmAccess.get("roles");
                return roles!=null && roles.contains("ADMIN");
            }
        } catch (Exception e) {
            log.error("Error extracting roles from JWT", e);
        }

        return false;
    }

    /**
     * Close WebSocket session with reason
     */
    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException e) {
            log.error("Error closing WebSocket session", e);
        }
    }

    /**
     * Record to store user session data
     */
    private record UserSessionData(String userId, boolean isAdmin, String huntId) {
    }
}
