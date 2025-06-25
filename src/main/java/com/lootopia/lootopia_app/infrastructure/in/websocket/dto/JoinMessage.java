package com.lootopia.lootopia_app.infrastructure.in.websocket.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Message for joining a hunt
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JoinMessage extends WebSocketMessage {
    private String huntId;

    public JoinMessage(String userId, String huntId) {
        super(WebSocketMessageType.JOIN, userId);
        this.huntId = huntId;
    }
}
