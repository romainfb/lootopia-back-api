package com.lootopia.lootopia_app.infrastructure.security;

import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class JwtTokenProvider {

    public static final String CLAIM_TYPE = "typ";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_EMAIL = "email";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String TYPE_AD_WATCH = "ad-watch";

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    @Getter
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;
    @Getter
    private final Duration adWatchMinDuration;
    private final Duration adWatchTokenTtl;

    public JwtTokenProvider(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.issuer:lootopia-app}") String issuer,
            @Value("${app.jwt.access-token-ttl:PT15M}") Duration accessTokenTtl,
            @Value("${app.jwt.refresh-token-ttl:P7D}") Duration refreshTokenTtl,
            @Value("${app.jwt.ad-watch-min-duration:PT15S}") Duration adWatchMinDuration,
            @Value("${app.jwt.ad-watch-token-ttl:PT5M}") Duration adWatchTokenTtl) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
        this.adWatchMinDuration = adWatchMinDuration;
        this.adWatchTokenTtl = adWatchTokenTtl;
    }

    public String generateAccessToken(UserEntity user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenTtl))
                .subject(user.getId().toString())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLES, List.of(user.getAccountType().name()))
                .build();
        return encode(claims);
    }

    public String generateAdWatchToken(UserEntity user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .notBefore(now.plus(adWatchMinDuration))
                .expiresAt(now.plus(adWatchTokenTtl))
                .subject(user.getId().toString())
                .claim(CLAIM_TYPE, TYPE_AD_WATCH)
                .build();
        return encode(claims);
    }

    public String generateRefreshToken(UserEntity user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(refreshTokenTtl))
                .subject(user.getId().toString())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .build();
        return encode(claims);
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(() -> "RS256").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
