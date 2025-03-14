package com.lootopia.lootopia_app.application.port.out;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

public interface KeycloakEventPort {
    void onUserEvent(Event event);
    void onAdminEvent(AdminEvent event);
}
