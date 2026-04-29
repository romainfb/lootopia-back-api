package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.AuthentificationUseCase;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.domain.AccountType;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.RegisterRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.TokenResponseDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserInfoDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import com.lootopia.lootopia_app.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthentificationUseCase {

    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final JwtDecoder jwtDecoder;

    @Override
    @Transactional
    public TokenResponseDto register(RegisterRequestDto request) {
        if (userPersistencePort.existsByEmail(request.getEmail())) {
            throw new InvalidParameterException("Email already registered");
        }
        UserEntity entity = UserEntity.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .accountType(AccountType.USER)
                .balance(0)
                .enabled(true)
                .build();
        return issueTokens(userPersistencePort.save(entity));
    }

    @Override
    public TokenResponseDto login(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (AuthenticationException e) {
            throw new InvalidParameterException("Invalid credentials");
        }
        UserEntity user = userPersistencePort.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return issueTokens(user);
    }

    @Override
    public TokenResponseDto refresh(String refreshToken) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(refreshToken);
        } catch (JwtException e) {
            throw new InvalidParameterException("Invalid refresh token");
        }
        if (!JwtTokenProvider.TYPE_REFRESH.equals(jwt.getClaimAsString(JwtTokenProvider.CLAIM_TYPE))) {
            throw new InvalidParameterException("Token is not a refresh token");
        }
        Long userId = Long.valueOf(jwt.getSubject());
        UserEntity user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (!user.isEnabled()) {
            throw new InvalidParameterException("User is disabled");
        }
        return issueTokens(user);
    }

    @Override
    public UserInfoDto getUserInfo(Long userId) {
        UserEntity user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserInfoDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

    private TokenResponseDto issueTokens(UserEntity user) {
        return TokenResponseDto.bearer(
                jwtTokenProvider.generateAccessToken(user),
                jwtTokenProvider.generateRefreshToken(user),
                jwtTokenProvider.getAccessTokenTtl().toSeconds());
    }
}
