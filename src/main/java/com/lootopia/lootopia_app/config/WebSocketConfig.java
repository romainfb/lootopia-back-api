package com.lootopia.lootopia_app.config;

import com.lootopia.lootopia_app.infrastructure.in.websocket.WebSocketAuthHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * Configuration for WebSocket support
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketAuthHandler webSocketAuthHandler;

    public WebSocketConfig(WebSocketAuthHandler webSocketAuthHandler) {
        this.webSocketAuthHandler = webSocketAuthHandler;
    }

    /**
     * Register WebSocket handlers
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketAuthHandler, "/ws/positions")
                .setAllowedOrigins("http://localhost:5173", "https://dev.lootopia-api.lootopia.xyz",
                        "https://dev.lootopia-web.lootopia.xyz", "https://dev.lootopia-web.lootopia.xyz/*")
                .addInterceptors(handshakeInterceptor());
    }

    /**
     * Interceptor to copy HTTP headers to WebSocket session attributes
     */
    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new HttpSessionHandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                                           org.springframework.http.server.ServerHttpResponse response,
                                           org.springframework.web.socket.WebSocketHandler wsHandler,
                                           java.util.Map<String, Object> attributes) throws Exception {
                // Copy HTTP headers to WebSocket session attributes
                attributes.put("headers", request.getHeaders().toSingleValueMap());
                return super.beforeHandshake(request, response, wsHandler, attributes);
            }
        };
    }
}
