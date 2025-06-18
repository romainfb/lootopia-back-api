package com.lootopia.lootopia_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuration de sécurité de l'application
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Chemins accessibles sans authentification
     */
    private static final String[] PUBLIC_PATHS = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**"
    };

    /**
     * Certificat à utiliser pour la validation JWT
     */
    @Value("classpath:keys/public_key.pem")
    private Resource publicKeyResource;

    /**
     * Configure la chaîne de filtres de sécurité
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().permitAll()  // Ou .authenticated() selon les besoins
                );

        // Configuration OAuth2 avec le décodeur JWT
        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()))
        );

        return http.build();
    }

    /**
     * Crée le décodeur JWT à partir de la clé publique
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            RSAPublicKey publicKey = loadPublicKey();
            return NimbusJwtDecoder.withPublicKey(publicKey).build();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger la clé publique pour JwtDecoder", e);
        }
    }

    /**
     * Charge la clé publique à partir du certificat
     */
    private RSAPublicKey loadPublicKey() throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate;

        try (InputStream is = publicKeyResource.getInputStream()) {
            certificate = (X509Certificate) factory.generateCertificate(is);
        }

        return (RSAPublicKey) certificate.getPublicKey();
    }
}
