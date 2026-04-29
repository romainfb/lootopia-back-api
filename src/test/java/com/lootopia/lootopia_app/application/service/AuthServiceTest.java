package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.domain.AccountType;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.RegisterRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.TokenResponseDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserInfoDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import com.lootopia.lootopia_app.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtDecoder jwtDecoder;

    @InjectMocks
    private AuthService authService;

    private UserEntity sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = UserEntity.builder()
                .id(42L)
                .email("alice@test.com")
                .username("alice")
                .passwordHash("{bcrypt}HASH")
                .accountType(AccountType.USER)
                .balance(0)
                .enabled(true)
                .build();
        when(jwtTokenProvider.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));
        when(jwtTokenProvider.generateAccessToken(any(UserEntity.class))).thenReturn("ACCESS");
        when(jwtTokenProvider.generateRefreshToken(any(UserEntity.class))).thenReturn("REFRESH");
    }

    @Test
    void register_creates_user_and_issues_tokens() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .email("alice@test.com")
                .password("plaintext-password")
                .username("alice")
                .build();
        when(userPersistencePort.existsByEmail("alice@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plaintext-password")).thenReturn("{bcrypt}HASH");
        when(userPersistencePort.save(any(UserEntity.class))).thenReturn(sampleUser);

        TokenResponseDto result = authService.register(request);

        assertThat(result.getAccessToken()).isEqualTo("ACCESS");
        assertThat(result.getRefreshToken()).isEqualTo("REFRESH");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getExpiresIn()).isEqualTo(900);
    }

    @Test
    void register_throws_when_email_already_used() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .email("alice@test.com").password("p").username("alice").build();
        when(userPersistencePort.existsByEmail("alice@test.com")).thenReturn(true);

        assertThrows(InvalidParameterException.class, () -> authService.register(request));
    }

    @Test
    void login_returns_tokens_when_credentials_valid() {
        when(userPersistencePort.findByEmail("alice@test.com")).thenReturn(Optional.of(sampleUser));

        TokenResponseDto result = authService.login("alice@test.com", "plaintext");

        assertThat(result.getAccessToken()).isEqualTo("ACCESS");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_throws_when_credentials_invalid() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("nope"));
        assertThrows(InvalidParameterException.class, () -> authService.login("alice@test.com", "wrong"));
    }

    @Test
    void refresh_issues_new_tokens_for_valid_refresh_token() {
        Jwt jwt = jwtFor(JwtTokenProvider.TYPE_REFRESH);
        when(jwtDecoder.decode("REFRESH")).thenReturn(jwt);
        when(userPersistencePort.findById(42L)).thenReturn(Optional.of(sampleUser));

        TokenResponseDto result = authService.refresh("REFRESH");

        assertThat(result.getAccessToken()).isEqualTo("ACCESS");
        assertThat(result.getRefreshToken()).isEqualTo("REFRESH");
    }

    @Test
    void refresh_rejects_access_token() {
        Jwt jwt = jwtFor(JwtTokenProvider.TYPE_ACCESS);
        when(jwtDecoder.decode("ACCESS")).thenReturn(jwt);

        assertThrows(InvalidParameterException.class, () -> authService.refresh("ACCESS"));
    }

    @Test
    void refresh_rejects_invalid_token() {
        when(jwtDecoder.decode("garbage")).thenThrow(new JwtException("bad"));
        assertThrows(InvalidParameterException.class, () -> authService.refresh("garbage"));
    }

    @Test
    void refresh_rejects_disabled_user() {
        sampleUser.setEnabled(false);
        Jwt jwt = jwtFor(JwtTokenProvider.TYPE_REFRESH);
        when(jwtDecoder.decode("REFRESH")).thenReturn(jwt);
        when(userPersistencePort.findById(42L)).thenReturn(Optional.of(sampleUser));

        assertThrows(InvalidParameterException.class, () -> authService.refresh("REFRESH"));
    }

    @Test
    void getUserInfo_returns_user_when_found() {
        when(userPersistencePort.findById(42L)).thenReturn(Optional.of(sampleUser));

        UserInfoDto result = authService.getUserInfo(42L);

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getEmail()).isEqualTo("alice@test.com");
        assertThat(result.getUsername()).isEqualTo("alice");
    }

    @Test
    void getUserInfo_throws_when_not_found() {
        when(userPersistencePort.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.getUserInfo(99L));
    }

    private Jwt jwtFor(String type) {
        return new Jwt(
                "tok",
                Instant.now(),
                Instant.now().plus(Duration.ofMinutes(15)),
                Map.of("alg", "RS256"),
                Map.of(JwtTokenProvider.CLAIM_TYPE, type, "sub", "42"));
    }
}
