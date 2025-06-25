package com.lootopia.lootopia_app.infrastructure.in.websocket.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base class for all WebSocket messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PositionMessage.class, name = "POSITION"),
        @JsonSubTypes.Type(value = JoinMessage.class, name = "JOIN")
})
public abstract class WebSocketMessage {
    private WebSocketMessageType type;
    private String userId;
}
