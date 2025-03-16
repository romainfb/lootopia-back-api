package com.lootopia.lootopia_app.infrastructure.out.keycloak;

import com.lootopia.lootopia_app.application.port.out.KeycloakPort;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserKeycloakUpdateDto;
import jakarta.ws.rs.NotFoundException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
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

    @Value("${keycloak_admin_username}")
    private String adminUsername;

    @Value("${keycloak_admin_password}")

    private String adminPassword;
    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
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
        log.info("Deleting user with ID: {}", userId);
        log.info("realm: {}", realm);
        try {
            Keycloak keycloak = getKeycloakInstance();
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            userResource.remove();
            log.info("User with ID {} deleted successfully from Keycloak.", userId);
            return true;
        } catch (NotFoundException e) {
            log.warn("User with ID {} does not exist in keycloak database.", userId);
            return false;
        } catch (Exception e) {
            log.error("Error deleting user with ID from keycloak: {}", userId, e);
            return false;
        }
    }

    @Override
    public UserRepresentation updateUser(UserKeycloakUpdateDto userDto) {
        log.info("Updating user in keycloak database with data: {}", userDto);
        try {
            Keycloak keycloak = getKeycloakInstance();
            UserRepresentation user = keycloak.realm(realm).users().get(userDto.getId()).toRepresentation();

            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setEmail(userDto.getEmail());
            user.setUsername(userDto.getUsername());
            keycloak.realm(realm).users().get(userDto.getId()).update(user);

            return keycloak.realm(realm).users().get(userDto.getId()).toRepresentation();
        } catch (NotFoundException e) {
            throw new RuntimeException("User with ID " + userDto.getId() + " does not exist in keycloak database.", e);
        } catch (Exception e) {
            throw new RuntimeException("Error while updating user in keycloak: " + e.getMessage(), e);
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

}
