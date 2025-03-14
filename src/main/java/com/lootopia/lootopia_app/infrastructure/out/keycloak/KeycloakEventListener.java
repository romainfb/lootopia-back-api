package com.lootopia.lootopia_app.infrastructure.out.keycloak;

import com.lootopia.lootopia_app.application.port.out.KeycloakEventPort;
import com.lootopia.lootopia_app.application.service.UserService;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeycloakEventListener implements EventListenerProvider, KeycloakEventPort {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakEventListener.class);
    private final UserService userService;

    public KeycloakEventListener(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType() == EventType.REGISTER) {
            String userId = event.getUserId();
            logger.info("Nouvel utilisateur enregistré avec ID Keycloak: " + userId);
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {

    }

    @Override
    public void close() {
    }

    @Override
    public void onUserEvent(Event event) {

    }

    @Override
    public void onAdminEvent(AdminEvent event) {

    }
}
