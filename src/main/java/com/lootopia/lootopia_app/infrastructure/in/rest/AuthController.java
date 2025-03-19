package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.AuthentificationUseCase;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.LoginRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthentificationUseCase authService;


    @Operation(summary = "Exchange authorization code for access token",
            description = "Receives an authorization code and exchanges it for an access token from Keycloak.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Missing or invalid code",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/callback")
    public ResponseEntity<?> exchangeCodeForToken(@RequestBody Map<String, String> body) {
        String code = body.get("code");

        if (code==null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing authorization code");
        }

        AccessTokenResponse tokenResponse = authService.exchangeCodeForToken(code);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "Logout user",
            description = "Logs out the user by invalidating their access token and refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully logged out"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Missing or invalid token",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(@AuthenticationPrincipal Jwt jwt,
                                    @RequestHeader("Refresh-Token") String refreshToken) {
        String accessToken = jwt.getTokenValue();
        log.info("Logging out user with access token: {}", accessToken);
        log.info("Using refresh token: {}", refreshToken);

        boolean success = authService.logout(refreshToken);

        if (success) {
            return ResponseEntity.ok("User successfully logged out");
        } else {
            return ResponseEntity.status(500).body("Error during logout");
        }
    }


    @Operation(summary = "Get user info",
            description = "Retrieves user information from Keycloak using an access token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User info successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(authService.getUserInfo(userId));
    }


    @Operation(summary = "Login user",
            description = "Login the user by validating username and password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully logged in"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty() ||
                    loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                return new ResponseEntity<>("Username and password must not be empty", HttpStatus.BAD_REQUEST);
            }
            AccessTokenResponse tokenResponse = authService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
