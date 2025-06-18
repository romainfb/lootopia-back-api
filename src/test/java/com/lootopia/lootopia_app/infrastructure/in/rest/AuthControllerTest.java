package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.AuthentificationUseCase;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {
    //TODO: Implement tests for AuthController

    private MockMvc mockMvc;

    @Mock
    private AuthentificationUseCase authService;

    @InjectMocks
    private AuthController authController;

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


}
