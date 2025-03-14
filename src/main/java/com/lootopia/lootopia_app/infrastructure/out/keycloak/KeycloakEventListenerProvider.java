package com.lootopia.lootopia_app.infrastructure.out.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.Config;

public class KeycloakEventListenerProvider implements EventListenerProvider, EventListenerProviderFactory {

    private static final Logger logger = Logger.getLogger(KeycloakEventListenerProvider.class);

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return this;
    }

    @Override
    public void init(Config.Scope scope) {
        logger.info("Initialisation de KeycloakEventListenerProvider...");
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        logger.info("KeycloakEventListenerProvider initialisé !");
    }

    @Override
    public void close() {
        logger.info("KeycloakEventListenerProvider arrêté.");
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType() == EventType.REGISTER) {
            logger.info("Nouvel utilisateur enregistré : " + event.getUserId());
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {

    }

    @Override
    public String getId() {
        return "custom-event-listener";
    }
}
