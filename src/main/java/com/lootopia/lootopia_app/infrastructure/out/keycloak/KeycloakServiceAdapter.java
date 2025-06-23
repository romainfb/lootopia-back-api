package com.lootopia.lootopia_app.infrastructure.out.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.out.KeycloakPort;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;
import jakarta.ws.rs.NotFoundException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
public class KeycloakServiceAdapter implements KeycloakPort {

    private static final Logger log = LoggerFactory.getLogger(KeycloakServiceAdapter.class);

    @Value("${KEYCLOAK_SERVER_URL}")
    private String authServerUrl;

    @Value("${KEYCLOAK_REALM}")
    private String realm;

    @Value("${KEYCLOAK_ADMIN_USERNAME}")
    private String adminUsername;

    @Value("${KEYCLOAK_ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${KEYCLOAK_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${KEYCLOAK_REDIRECT_URI}")
    private String redirectUri;

    protected Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    protected HttpClient createHttpClient() {
        return HttpClient.newBuilder().build();
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }

    @Override
    public Optional<UserRepresentation> getUserById(String userId) {
        if (userId==null || userId.isBlank()) {
            log.error("Cannot get user with null or blank ID");
            return Optional.empty();
        }

        try {
            log.info("Getting user with ID: {}", userId);
            Keycloak keycloak = getKeycloakInstance();
            UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
            log.info("User with ID {} found: Username: {}, Email: {}",
                    userId, user.getUsername(), user.getEmail());
            return Optional.of(user);
        } catch (Exception e) {
            log.error("Error getting user with ID: {}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteUser(String userId) {
        if (userId==null || userId.isBlank()) {
            log.error("Cannot delete user with null or blank ID");
            return false;
        }

        log.info("Deleting user with ID: {}", userId);
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
    public UserRepresentation updateUser(UserUpdatedDto userDto) {
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
        if (userId==null || userId.isBlank() || newPassword==null || newPassword.isBlank()) {
            log.error("Cannot update password with null or blank user ID or password");
            return false;
        }

        try {
            Keycloak keycloak = getKeycloakInstance();

            CredentialRepresentation credential = createPasswordCredential(newPassword);
            keycloak.realm(realm).users().get(userId).resetPassword(credential);

            UserRepresentation updatedUser = keycloak.realm(realm).users().get(userId).toRepresentation();
            log.info("Updated user info for user {}: Username: {}, Email: {}",
                    userId, updatedUser.getUsername(), updatedUser.getEmail());

            keycloak.realm(realm).users().get(userId).logout();

            return true;
        } catch (Exception e) {
            log.error("Error updating password for user with ID: {}", userId, e);
            return false;
        }
    }

    private CredentialRepresentation createPasswordCredential(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        return credential;
    }

    @Override
    public AccessTokenResponse getAccessToken(String code) {
        if (code==null || code.isBlank()) {
            throw new IllegalArgumentException("Authorization code cannot be null or blank");
        }

        log.info("Échange du code d'autorisation contre un access token");

        try {
            String tokenEndpoint = getTokenEndpoint();
            String requestBody = buildTokenRequestBody(code);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenEndpoint))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = createHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            return handleTokenResponse(response);
        } catch (Exception e) {
            log.error("Erreur lors de l'échange du code contre un access token", e);
            throw new RuntimeException("Échec de l'échange du code d'autorisation: " + e.getMessage(), e);
        }
    }

    private String getTokenEndpoint() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }

    private String buildTokenRequestBody(String code) {
        return "grant_type=authorization_code" +
                "&client_id=lootopia_web" +
                "&client_secret=" + clientSecret +
                "&code=" + code +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
    }

    private AccessTokenResponse handleTokenResponse(HttpResponse<String> response) throws IOException {
        if (response.statusCode()==200) {
            ObjectMapper objectMapper = createObjectMapper();
            AccessTokenResponse tokenResponse = objectMapper.readValue(response.body(), AccessTokenResponse.class);
            log.info("Access token obtenu avec succès");
            return tokenResponse;
        } else {
            log.error("Erreur lors de l'échange du code: Status code {}, Body: {}",
                    response.statusCode(), response.body());
            throw new RuntimeException("Échec de l'échange du code: " + response.body());
        }
    }

    @Override
    public AccessTokenResponse loginUser(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username and password cannot be null or blank");
        }

        log.info("Authenticating user: {}", username);

        try {
            String tokenEndpoint = getTokenEndpoint();
            String requestBody = "grant_type=password" +
                    "&client_id=lootopia_web" +
                    "&client_secret=" + clientSecret +
                    "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
                    "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenEndpoint))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = createHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            return handleTokenResponse(response);
        } catch (Exception e) {
            log.error("Error authenticating user {}", username, e);
            throw new RuntimeException("Failed to authenticate user: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean logout(String accessToken) {
        if (accessToken==null || accessToken.isBlank()) {
            log.error("Cannot logout with null or blank access token");
            return false;
        }

        log.info("Déconnexion de l'utilisateur");

        try {
            String logoutEndpoint = getLogoutEndpoint();
            String requestBody = buildLogoutRequestBody(accessToken);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(logoutEndpoint))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = createHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode()==204;
        } catch (Exception e) {
            log.error("Erreur lors de la déconnexion de l'utilisateur", e);
            return false;
        }
    }

    private String getLogoutEndpoint() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
    }

    private String buildLogoutRequestBody(String accessToken) {
        return "client_id=lootopia_web" +
                "&client_secret=" + clientSecret +
                "&refresh_token=" + accessToken;
    }

    @Override
    public List<UserRepresentation> getAllUsers() {
        log.info("Fetching all users from Keycloak");
        try {
            Keycloak keycloak = getKeycloakInstance();
            List<UserRepresentation> users = keycloak.realm(realm).users().list();
            log.info("Successfully fetched {} users from Keycloak", users.size());
            return users;
        } catch (Exception e) {
            log.error("Error fetching all users from Keycloak", e);
            throw new RuntimeException("Failed to fetch users from Keycloak: " + e.getMessage(), e);
        }
    }
}
