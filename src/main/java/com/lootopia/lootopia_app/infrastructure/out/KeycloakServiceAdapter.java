package com.lootopia.lootopia_app.infrastructure.out;

import com.lootopia.lootopia_app.application.port.out.KeycloakPort;
import jakarta.ws.rs.NotFoundException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KeycloakServiceAdapter implements KeycloakPort {

    private static final Logger log = LoggerFactory.getLogger(KeycloakServiceAdapter.class);
    @Value("${keycloak_server_url}")
    private String authServerUrl;

    @Value("${keycloak_realm}")
    private String realm;

    @Value("${keycloak_client_id}")
    private String clientId;

    @Value("${keycloak_secret}")
    private String clientSecret;

    @Value("${keycloak_admin_username}")
    private String adminUsername;

    @Value("${keycloak_admin_password}")

    private String adminPassword;
    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    @Override
    public Optional<UserRepresentation> getUserById(String userId) {
        try {
            Keycloak keycloak = getKeycloakInstance();
            UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
            return Optional.of(user);
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteUser(String userId) {
        try {
            Keycloak keycloak = getKeycloakInstance();
            keycloak.realm(realm).users().get(userId).remove();
            return true;
        } catch (Exception e) {
            log.error("Error deleting user with ID: " + userId, e);
            return false;
        }
    }

    @Override
    public UserRepresentation updateUser(String userId, String firstName, String lastName, String email) {
        try {
            Keycloak keycloak = getKeycloakInstance();
            UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setEmailVerified(true);

            keycloak.realm(realm).users().get(userId).update(user);

            // Récupérer et retourner l'utilisateur mis à jour
            return keycloak.realm(realm).users().get(userId).toRepresentation();
        } catch (NotFoundException e) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId, e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updatePassword(String userId, String newPassword) {
        try {
            Keycloak keycloak = getKeycloakInstance();

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);

            keycloak.realm(realm).users().get(userId).resetPassword(credential);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UserRepresentation setUserEnabled(String userId, boolean enabled) {
        try {
            Keycloak keycloak = getKeycloakInstance();
            UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();

            user.setEnabled(enabled);

            keycloak.realm(realm).users().get(userId).update(user);

            // Récupérer et retourner l'utilisateur mis à jour
            return keycloak.realm(realm).users().get(userId).toRepresentation();
        } catch (NotFoundException e) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId, e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la modification du statut de l'utilisateur: " + e.getMessage(), e);
        }
    }
}
