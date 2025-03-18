package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.in.AuthentificationUseCase;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthentificationUseCase authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void exchangeCodeForToken_ShouldReturnToken_WhenValidCodeIsProvided() throws Exception {
        String code = "valid_code";
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4STYyVFlSR2tmYWMwVU15dlk1djVhaVJtaU1lYmpWTjlqQTZHTDB4ODg0In0.eyJleHAiOjE3NDIyMTU5NTcsImlhdCI6MTc0MjIxNTg5NywiYXV0aF90aW1lIjoxNzQyMjE1ODcyLCJqdGkiOiIyYzU1ZGI5ZC03MTZmLTRlM2EtODdmYS1hZmRjZDAwMGQyMjQiLCJpc3MiOiJodHRwczovL2tleWNsb2FrLWxvb3RvcGlhLnJvbWFpbmZiLmZyL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiYjM5OWVhM2MtMDkwMi00Y2Q4LTgzODMtYTliZGFiNmQyMmE5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibG9vdG9waWFfd2ViIiwic2lkIjoiZGQwMDZlY2QtMDJmYy00ZWY3LThkODQtZjhjMTIzOTFlNGU0IiwiYWNyIjoiMT7eNf9PFfbuI2hxyJ2UgMI-490zqK7YezCYmabFlaEL3Tp4_gUPIScpJWi-TnLfjXCekJM-SGyoIM5A";
        AccessTokenResponse tokenResponse = new AccessTokenResponse();
        tokenResponse.setToken(token);

        when(authService.exchangeCodeForToken(code)).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(token));  // Corrigé ici

        verify(authService, times(1)).exchangeCodeForToken(code);
    }

    @Test
    void exchangeCodeForToken_ShouldReturnBadRequest_WhenCodeIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing authorization code"));
    }

    @Test
    void logout_ShouldReturnOk_WhenValidTokenIsProvided() throws Exception {
        String token = "valid_token";
        when(authService.logout(token)).thenReturn(true);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("User successfully logged out"));

        verify(authService, times(1)).logout(token);
    }

    @Test
    void logout_ShouldReturnBadRequest_WhenAuthorizationHeaderIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Invalid token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing or invalid Authorization header"));
    }

    @Test
    void logout_ShouldReturnInternalServerError_WhenLogoutFails() throws Exception {
        String token = "valid_token";
        when(authService.logout(token)).thenReturn(false);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error during logout"));

        verify(authService, times(1)).logout(token);
    }

    @Test
    void getUserInfo_ShouldReturnUserInfo_WhenValidTokenIsProvided() throws Exception {
        String token = "valid_token";
        UserInfoDto userInfo = new UserInfoDto(
                32L,
                "QSDFAAE\\&1234",
                "thibault@gmail.com",
                "Thibault",
                "Garrigues",
                "username",
                true
        );

        when(authService.getUserInfo("Bearer " + token)).thenReturn(userInfo);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Thibault"))
                .andExpect(jsonPath("$.lastName").value("Garrigues"))
                .andExpect(jsonPath("$.id").value(32L))
                .andExpect(jsonPath("$.username").value("username"))
                .andExpect(jsonPath("$.email").value("thibault@gmail.com"));


        verify(authService, times(1)).getUserInfo("Bearer " + token);
    }

    @Test
    void getUserInfo_ShouldReturnUnauthorized_WhenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Invalid token"))
                .andExpect(status().isUnauthorized());
    }
}
