package com.lootopia.lootopia_app.infrastructure.in.websocket.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Message containing a player's position
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PositionMessage extends WebSocketMessage {
    private String huntId;
    private double lat;
    private double lng;

    public PositionMessage(String userId, String huntId, double lat, double lng) {
        super(WebSocketMessageType.POSITION, userId);
        this.huntId = huntId;
        this.lat = lat;
        this.lng = lng;
    }
}
