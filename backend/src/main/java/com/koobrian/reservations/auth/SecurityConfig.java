package com.koobrian.reservations.auth;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Signed out people must reach these to get or renew a token.
                .requestMatchers(HttpMethod.POST, "/v1/auth/google", "/v1/auth/refresh", "/v1/auth/logout").permitAll()
                // The host's uptime check.
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                // Everything else needs a valid access token.
                .anyRequest().authenticated()
            )
            // No server side sessions: every request carries its own token.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // CSRF protection is for cookie based logins. API calls use a bearer token,
            // which other sites can't attach. The one cookie (refresh) is SameSite=Strict.
            .csrf(csrf -> csrf.disable())
            // Read "Authorization: Bearer <token>" and verify it with the decoder below.
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    // The key that signs our access tokens. The same key verifies them (HMAC SHA-256).
    @Bean
    public SecretKey jwtSigningKey(@Value("${app.auth.jwt-secret}") String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    // Signs our access tokens, with the same key the decoder verifies them with.
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey));
    }

    // Checks the signature and expiry of incoming access tokens.
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSigningKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSigningKey).build();
    }
}