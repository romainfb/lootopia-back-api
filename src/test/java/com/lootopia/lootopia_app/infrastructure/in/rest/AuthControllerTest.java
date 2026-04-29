package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.in.AuthentificationUseCase;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.LoginRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.RegisterRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.TokenResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthentificationUseCase authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void register_returns_tokens() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .email("a@b.c").password("password123").username("alice").build();
        when(authService.register(any(RegisterRequestDto.class)))
                .thenReturn(TokenResponseDto.bearer("ACC", "REF", 900));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("ACC"))
                .andExpect(jsonPath("$.refreshToken").value("REF"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void login_returns_tokens() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .username("a@b.c").password("password123").build();
        when(authService.login(eq("a@b.c"), eq("password123")))
                .thenReturn(TokenResponseDto.bearer("ACC", "REF", 900));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("ACC"));

        verify(authService).login("a@b.c", "password123");
    }

    @Test
    void refresh_returns_tokens() throws Exception {
        when(authService.refresh("REF"))
                .thenReturn(TokenResponseDto.bearer("ACC2", "REF2", 900));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "REF"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("ACC2"))
                .andExpect(jsonPath("$.refreshToken").value("REF2"));
    }
}
