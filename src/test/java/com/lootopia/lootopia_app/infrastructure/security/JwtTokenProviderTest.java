package com.lootopia.lootopia_app.infrastructure.security;

import com.lootopia.lootopia_app.domain.AccountType;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class JwtTokenProviderTest {

    private static JwtTokenProvider tokenProvider;
    private static JwtDecoder jwtDecoder;

    @BeforeAll
    static void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) pair.getPublic())
                .privateKey((RSAPrivateKey) pair.getPrivate())
                .keyID(UUID.randomUUID().toString())
                .build();

        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
        tokenProvider = new JwtTokenProvider(
                encoder,
                "test-issuer",
                Duration.ofMinutes(15),
                Duration.ofDays(7));

        jwtDecoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) pair.getPublic()).build();
    }

    private UserEntity sampleUser() {
        return UserEntity.builder()
                .id(42L)
                .email("alice@lootopia.test")
                .accountType(AccountType.ADMIN)
                .balance(0)
                .build();
    }

    @Test
    void access_token_carries_expected_claims_and_ttl() {
        String token = tokenProvider.generateAccessToken(sampleUser());

        Jwt decoded = jwtDecoder.decode(token);

        assertThat(decoded.getClaimAsString("iss")).isEqualTo("test-issuer");
        assertThat(decoded.getSubject()).isEqualTo("42");
        assertThat(decoded.<String>getClaim(JwtTokenProvider.CLAIM_TYPE)).isEqualTo(JwtTokenProvider.TYPE_ACCESS);
        assertThat(decoded.<String>getClaim(JwtTokenProvider.CLAIM_EMAIL)).isEqualTo("alice@lootopia.test");
        assertThat(decoded.<java.util.List<String>>getClaim(JwtTokenProvider.CLAIM_ROLES))
                .containsExactly("ADMIN");
        assertThat(decoded.getExpiresAt())
                .isCloseTo(Instant.now().plus(Duration.ofMinutes(15)), within(5, java.time.temporal.ChronoUnit.SECONDS));
    }

    @Test
    void refresh_token_is_typed_refresh_and_has_long_ttl() {
        String token = tokenProvider.generateRefreshToken(sampleUser());

        Jwt decoded = jwtDecoder.decode(token);

        assertThat(decoded.getSubject()).isEqualTo("42");
        assertThat(decoded.<String>getClaim(JwtTokenProvider.CLAIM_TYPE)).isEqualTo(JwtTokenProvider.TYPE_REFRESH);
        assertThat(decoded.<String>getClaim(JwtTokenProvider.CLAIM_EMAIL)).isNull();
        assertThat(decoded.<java.util.List<String>>getClaim(JwtTokenProvider.CLAIM_ROLES)).isNull();
        assertThat(decoded.getExpiresAt())
                .isCloseTo(Instant.now().plus(Duration.ofDays(7)), within(5, java.time.temporal.ChronoUnit.SECONDS));
    }

    @Test
    void access_and_refresh_tokens_differ() {
        UserEntity user = sampleUser();
        assertThat(tokenProvider.generateAccessToken(user))
                .isNotEqualTo(tokenProvider.generateRefreshToken(user));
    }
}
